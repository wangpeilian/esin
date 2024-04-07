package com.esin.jdbc.helper;

import com.esin.base.bean.EntryBean;
import com.esin.base.constants.IEnum;
import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.lazy.LazyEntityJavassist;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityHelper {
    private static final Logger logger = Logger.getLogger(EntityHelper.class);

    private final DaoFactory daoFactory;
    private final Map<String, Class<? extends IEntity>> entityNameTypeMap = new HashMap<>();
    private final Map<Class<? extends IEntity>, Map<Column, Field>> entityColumnFieldMap = new HashMap<>();
    private final Map<Class<? extends Enum<?>>, Map<String, Enum<? extends Enum<?>>>> enumValueMap = new HashMap<>();
    private final Map<Field, Column> listFieldColumnMap = new HashMap<>();

    public EntityHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public Class<? extends IEntity> getEntityType(String entityName) {
        return entityNameTypeMap.get(entityName);
    }

    private Map<Column, Field> getColumnMap(Class<? extends IEntity> clazz) {
        if (entityColumnFieldMap.get(clazz) == null) {
            entityNameTypeMap.put(clazz.getSimpleName(), clazz);
            List<EntryBean<Column, Field>> columnFieldList = new ArrayList<>();
            Set<String> fieldNameSet = new HashSet<>();
            Class _clazz = clazz;
            while (!Object.class.equals(_clazz)) {
                for (Field field : _clazz.getDeclaredFields()) {
                    if (fieldNameSet.contains(field.getName())) {
                        continue;
                    }
                    if (field.getType().isEnum()) {
                        Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) field.getType();
                        Map<String, Enum<? extends Enum<?>>> enumMap = enumValueMap.get(enumClazz);
                        if (enumMap == null) {
                            enumMap = new HashMap<>();
                            Enum[] values = enumClazz.getEnumConstants();
                            for (int i = 0; i < values.length; i++) {
                                enumMap.put(values[i].name(), values[i]);
                                enumMap.put(String.valueOf(values[i].ordinal()), values[i]);
                            }
                            enumValueMap.put(enumClazz, enumMap);
                        }
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        Class<?> targetType = Utility.getCollectionType(clazz, field);
                        if (IEntity.class.isAssignableFrom(targetType)) {
                            Column column = field.getAnnotation(Column.class);
                            if (column == null) {
                                for (Field targetField : Utility.describeFieldMap(targetType).values()) {
                                    if (targetField.getType().isAssignableFrom(clazz)) {
                                        AssertUtil.check(column == null, "Not unique many to one target.(" + clazz.getSimpleName() + "." + field.getName() + ")");
                                        column = targetField.getAnnotation(Column.class);
                                    }
                                }
                            }
                            AssertUtil.check(column != null && Utility.isNotEmpty(column.name()), "Not found many to one target.(" + clazz.getSimpleName() + "." + field.getName() + ")");
                            listFieldColumnMap.put(field, column);
                        }
                        continue;
                    }
                    fieldNameSet.add(field.getName());
                    Column column = field.getAnnotation(Column.class);
                    if (column == null || IEntity.Col_id.equals(column.name())) {
                        continue;
                    }
                    field.setAccessible(true);
                    columnFieldList.add(new EntryBean<>(column, field));
                }
                _clazz = _clazz.getSuperclass();
            }
            for (int i = 0; i < columnFieldList.size() - 1; i++) {
                for (int j = i + 1; j < columnFieldList.size(); j++) {
                    AssertUtil.check(!columnFieldList.get(i).getKey().name().equals(columnFieldList.get(j).getKey().name()),
                            "Duplicate column name.(" + clazz.getSimpleName() + "." + columnFieldList.get(i).getKey().name() + ")");
                }
            }
            columnFieldList.sort(Comparator.comparingInt(o -> o.getKey().order()));
            for (EntryBean<Column, Field> entry : columnFieldList) {
                Map<Column, Field> columnFieldMap = entityColumnFieldMap.computeIfAbsent(clazz, key -> new LinkedHashMap<>());
                columnFieldMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().name().equals(entry.getValue().getName())) {
                    AssertUtil.check(!fieldNameSet.contains(entry.getKey().name()),
                            "Confused column and field name.(" + clazz.getSimpleName() + "." + entry.getKey().name() + ")");
                }
            }
        }
        return entityColumnFieldMap.get(clazz);
    }

    public Set<Column> getColumnSet(Class<? extends IEntity> clazz) {
        return getColumnMap(clazz).keySet();
    }

    public Field getField(Class<? extends IEntity> clazz, Column column) {
        return getColumnMap(clazz).get(column);
    }

    public Column getColumn(Class<? extends IEntity> clazz, String fieldName) {
        Map<Column, Field> map = getColumnMap(clazz);
        for (Map.Entry<Column, Field> entry : map.entrySet()) {
            if (entry.getValue().getName().equals(fieldName)) {
                return entry.getKey();
            }
        }
        for (Field field : listFieldColumnMap.keySet()) {
            if (field.getName().equals(fieldName)) {
                return listFieldColumnMap.get(field);
            }
        }
        return null;
    }

    public Object getValue(IEntity target, Column column) {
        try {
            Object value = getColumnMap((Class<? extends IEntity>) target.getClass()).get(column).get(target);
            if (value != null) {
                if (value.getClass().isEnum()) {
                    if (column.enum_name()) {
                        value = ((Enum) value).name();
                    } else {
                        value = ((Enum) value).ordinal();
                    }
                } else if (Date.class.equals(value.getClass())) {
                    value = new Timestamp(((Date) value).getTime());
                }
            }
            return value;
        } catch (IllegalAccessException e) {
            logger.error(target + " - " + column.name(), e);
            return null;
        }
    }

    public <T extends IEntity> void setValue(T target, Column column, Object value, boolean lazyProxy) {
        if (value == null) {
            return;
        }
        try {
            Field field = getColumnMap((Class<? extends IEntity>) target.getClass()).get(column);
            if (field.getType().isEnum()) {
                Class<? extends Enum<?>> clazz = (Class<? extends Enum<?>>) field.getType();
                Map<String, Enum<? extends Enum<?>>> enumMap = enumValueMap.get(clazz);
                if (column.enum_name()) {
                    value = enumMap.get(value.toString());
                } else {
                    value = enumMap.get(String.valueOf(((Number) value).intValue()));
                }
                if (value instanceof IEnum) {
                    Field fieldTitle = Utility.describeFieldMap(target).get(field.getName() + "Title");
                    if (fieldTitle != null && String.class.equals(fieldTitle.getType())
                            && fieldTitle.getAnnotation(Column.class) == null) {
                        Utility.setFieldValue(fieldTitle, target, ((IEnum) value).getTitle());
                    }
                }
            } else if (IEntity.class.isAssignableFrom(field.getType())) {
                if (lazyProxy) {
//                    value = LazyEntityMock.getProxyEntity(daoFactory, (Class<? extends IEntity>) field.getType(), value);
                    value = LazyEntityJavassist.getProxyEntity(daoFactory, (Class<? extends IEntity>) field.getType(), value);
//                    value = LazyEntityCglib.getProxyEntity(daoFactory, (Class<? extends IEntity>) field.getType(), value);
                } else {
                    IEntity entity = Utility.newInstance((Class<? extends IEntity>) field.getType());
                    entity.setId(value);
                    value = entity;
                }
            }
            field.set(target, convertValue(value, field.getType()));
        } catch (IllegalAccessException e) {
            // 有可能保存好数据后，后期把代码改了，无法还原，所以直接返回null
            logger.warn(target.getClass() + " - " + column.name() + " - " + value);
        }
    }

    private Object convertValue(Object value, Class type) {
        if (Integer.class.equals(type)) {
            return ((Number) value).intValue();
        } else if (Float.class.equals(type)) {
            return ((Number) value).floatValue();
        } else if (Long.class.equals(type)) {
            return ((Number) value).longValue();
        } else if (Double.class.equals(type)) {
            return ((Number) value).doubleValue();
        } else if (Byte.class.equals(type)) {
            return ((Number) value).byteValue();
        } else {
            return value;
        }
    }
}
