package com.esin.jdbc.query;

import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultConvertBean<T extends IEntity> implements ResultConvert<T> {
    private static final Logger logger = Logger.getLogger(ResultConvertBean.class);

    private final Class<T> clazz;
    private final Map<String, String> aliasMap;
    private boolean useCache = true;

    public ResultConvertBean(Class<T> clazz, Map<String, String> aliasMap) {
        this.clazz = clazz;
        this.aliasMap = aliasMap;
    }

    public ResultConvertBean<T> withoutCache() {
        this.useCache = false;
        return this;
    }

    @Override
    public T convert(DaoFactory daoFactory, ResultSet row, List<String> names, int index) throws SQLException {
        T entity = null;
        try {
            entity = clazz.newInstance();
            if (names.contains(IEntity.Col_id)) {
                entity.fillId(row);
                if (useCache && Utility.isEmpty(aliasMap)) {
                    T cachedValue = daoFactory.getCacheHelper().getCacheEntity(entity);
                    if (cachedValue != null) {
                        return cachedValue;
                    }
                }
            }
            Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
            for (Column column : columnSet) {
                if (names.contains(column.name())) {
                    daoFactory.getEntityHelper().setValue(entity, column, row.getObject(column.name()), false);
                } else if (names.contains(daoFactory.getEntityHelper().getField(clazz, column).getName().toLowerCase())) {
                    daoFactory.getEntityHelper().setValue(entity, column,
                            row.getObject(daoFactory.getEntityHelper().getField(clazz, column).getName().toLowerCase()), false);
                }
            }
            if (Utility.isNotEmpty(aliasMap)) {
                for (String column : aliasMap.keySet()) {
                    String name = aliasMap.get(column);
                    Field field = Utility.describeFieldMap(clazz).get(name.substring(0, name.indexOf(".")));
                    if (field != null) {
                        IEntity target = (IEntity) Utility.getFieldValue(field, entity);
                        if (target == null) {
                            target = Utility.newInstance((Class<? extends IEntity>) field.getType());
                            Utility.setFieldValue(field, entity, target);
                        }
                        field = Utility.describeFieldMap(target).get(name.substring(name.indexOf(".") + 1));
                        if (field != null) {
                            Utility.setFieldValue(field, target, row.getObject(column));
                        }
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(clazz.getName() + " - " + names + " - " + index, e);
        }
        if (useCache) {
            daoFactory.getCacheHelper().putCacheEntity(entity);
        }
        return entity;
    }
}
