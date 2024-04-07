package com.esin.jdbc.helper;

import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.define.Table;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.entity.RecordStatus;
import com.esin.jdbc.query.criterion.Criterion;
import com.esin.jdbc.query.criterion.Expression;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SqlHelper {
    private final DaoFactory daoFactory;

    public SqlHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public <T extends IEntity> Criterion getFilterExpression4query(Class<T> clazz) {
        return Expression.ne("recordStatus", RecordStatus.deleted);
    }

    public <T extends IEntity> String getFilterSql4update(Class<T> clazz) {
        return null;
    }

    public <T extends IEntity> List<String> getDefaultSetNames4update(Class<T> clazz) {
        return Collections.singletonList("updateTm");
    }

    public <T extends IEntity> void setDefaultValue4save(List<T> entityList) {
        Date date = new Date();
        for (T entity : entityList) {
            entity.setUpdateTm(date);
            if (entity.getCreateTm() == null) {
                entity.setCreateTm(entity.getUpdateTm());
            }
            if (entity.getRecordStatus() == null) {
                entity.setRecordStatus(RecordStatus.active);
            }
        }
    }

    public <T extends IEntity> List<Object[]> insert(List<T> entityList, StringBuilder sbSql) {
        Class<T> clazz = (Class<T>) entityList.get(0).getClass();
        Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
        List<Column> columnList = new ArrayList<>(columnSet.size());
        for (Column column : columnSet) {
            if (column.insert_able()) {
                columnList.add(column);
            }
        }
        List<Object[]> paramsList = new ArrayList<>(entityList.size());
        setDefaultValue4save(entityList);
        for (T entity : entityList) {
            AssertUtil.check(entity.isNullId(), "Cannot insert entity with "
                    + entity.getClass().getSimpleName() + "=" + entity.getId() + ".");
            paramsList.add(new Object[columnList.size()]);
        }
        sbSql.append("insert into ");
        sbSql.append(getTableName(clazz));
        sbSql.append("(");
        for (int i = 0; i < columnList.size(); i++) {
            if (i != 0) {
                sbSql.append(",");
            }
            Column column = columnList.get(i);
            sbSql.append(column.name());
            for (int j = 0; j < entityList.size(); j++) {
                Object value = daoFactory.getEntityHelper().getValue(entityList.get(j), column);
                if (JdbcConfig.production_mode) {
                    value = getDefaultValueIfNull(value, clazz, column);
                }
                AssertUtil.check(value != null || column.null_able(),
                        "Cannot insert null value with " + clazz.getSimpleName() + "." + column.name());
                if (value instanceof String && value.toString().length() > column.string_length()) {
                    AssertUtil.check(false, "The length of " + clazz.getSimpleName() + "." + column.name() + " is too long");
                }
                if (value != null && IEntity.class.isAssignableFrom(value.getClass())) {
                    value = ((IEntity) value).getId();
                }
                paramsList.get(j)[i] = value;
            }
        }
        sbSql.append(") values (");
        for (int i = 0; i < columnList.size(); i++) {
            if (i != 0) {
                sbSql.append(",");
            }
            sbSql.append("?");
        }
        sbSql.append(")");
        return paramsList;
    }

    public <T extends IEntity> List<Object[]> insertWithId(List<T> entityList, StringBuilder sbSql) {
        Class<T> clazz = (Class<T>) entityList.get(0).getClass();
        Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
        List<Column> columnList = new ArrayList<>(columnSet.size());
        for (Column column : columnSet) {
            if (column.insert_able()) {
                columnList.add(column);
            }
        }
        List<Object[]> paramsList = new ArrayList<>(entityList.size());
        setDefaultValue4save(entityList);
        for (T entity : entityList) {
            AssertUtil.check(!entity.isNullId(), "Cannot insert entity with "
                    + entity.getClass().getSimpleName() + "=" + entity.getId() + ".");
            paramsList.add(new Object[columnList.size() + 1]);
        }
        sbSql.append("insert into ");
        sbSql.append(getTableName(clazz));
        sbSql.append("(id");
        for (int j = 0; j < entityList.size(); j++) {
            paramsList.get(j)[0] = entityList.get(j).getId();
        }
        for (int i = 0; i < columnList.size(); i++) {
            sbSql.append(",");
            Column column = columnList.get(i);
            sbSql.append(column.name());
            for (int j = 0; j < entityList.size(); j++) {
                Object value = daoFactory.getEntityHelper().getValue(entityList.get(j), column);
                if (JdbcConfig.production_mode) {
                    value = getDefaultValueIfNull(value, clazz, column);
                }
                AssertUtil.check(value != null || column.null_able(),
                        "Cannot insert null value with " + clazz.getSimpleName() + "." + column.name());
                if (value instanceof String && value.toString().length() > column.string_length()) {
                    AssertUtil.check(false, "The length of " + clazz.getSimpleName() + "." + column.name() + " is too long");
                }
                if (value != null && IEntity.class.isAssignableFrom(value.getClass())) {
                    value = ((IEntity) value).getId();
                }
                paramsList.get(j)[i + 1] = value;
            }
        }
        sbSql.append(") values (?");
        for (int i = 0; i < columnList.size(); i++) {
            sbSql.append(",");
            sbSql.append("?");
        }
        sbSql.append(")");
        return paramsList;
    }

    public <T extends IEntity> List<Object[]> update(List<T> entityList, StringBuilder sbSql, String... names) {
        Class<T> clazz = (Class<T>) entityList.get(0).getClass();
        Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
        List<Column> columnList = new ArrayList<>(columnSet.size());
        if (Utility.isNotEmpty(names)) {
            List<String> nameList = new ArrayList<>(Arrays.asList(names));
            List<String> defaultSetNameList = getDefaultSetNames4update(clazz);
            for (Column column : columnSet) {
                if (nameList.contains(column.name())) {
                    columnList.add(column);
                    nameList.remove(column.name());
                } else if (nameList.contains(daoFactory.getEntityHelper().getField(clazz, column).getName())) {
                    columnList.add(column);
                    nameList.remove(daoFactory.getEntityHelper().getField(clazz, column).getName());
                } else if (defaultSetNameList.contains(column.name()) || defaultSetNameList.contains(daoFactory.getEntityHelper().getField(clazz, column).getName())) {
                    columnList.add(column);
                }
            }
            AssertUtil.check(Utility.isEmpty(nameList), "Cannot resolve the " + clazz.getSimpleName() + " update field names. " + nameList);
        } else {
            for (Column column : columnSet) {
                if (column.update_able()) {
                    columnList.add(column);
                }
            }
        }
        List<Object[]> paramsList = new ArrayList<>(entityList.size());
        setDefaultValue4save(entityList);
        for (T entity : entityList) {
            AssertUtil.check(entity.getId() != null, "Cannot update " + entity.getClass().getSimpleName() + " with null id.");
            paramsList.add(new Object[columnList.size() + 1]);
        }
        sbSql.append("update ");
        sbSql.append(getTableName(clazz));
        for (int i = 0; i < columnList.size(); i++) {
            if (i == 0) {
                sbSql.append(" set ");
            } else {
                sbSql.append(",");
            }
            Column column = columnList.get(i);
            sbSql.append(column.name());
            sbSql.append("=?");
            for (int j = 0; j < entityList.size(); j++) {
                Object value = daoFactory.getEntityHelper().getValue(entityList.get(j), column);
                if (JdbcConfig.production_mode) {
                    value = getDefaultValueIfNull(value, clazz, column);
                }
                AssertUtil.check(value != null || columnList.get(i).null_able(),
                        "Cannot update null value with " + clazz.getSimpleName() + "." + column.name());
                if (value instanceof String && value.toString().length() > column.string_length()) {
                    AssertUtil.check(false, "The length of " + clazz.getSimpleName() + "." + column.name() + " is too long");
                }
                if (value != null && IEntity.class.isAssignableFrom(value.getClass())) {
                    value = ((IEntity) value).getId();
                }
                paramsList.get(j)[i] = value;
            }
        }
        sbSql.append(" where ");
        sbSql.append(IEntity.Col_id);
        sbSql.append("=?");
        String filterSql = getFilterSql4update(clazz);
        if (Utility.isNotEmpty(filterSql)) {
            sbSql.append(" and ");
            sbSql.append(filterSql);
        }
        for (int i = 0; i < entityList.size(); i++) {
            paramsList.get(i)[columnList.size()] = entityList.get(i).getId();
        }
        if (Utility.isEmpty(names)) {
            entityList.forEach(t -> daoFactory.getCacheHelper().putCacheEntity(t));
        }
        return paramsList;
    }

    public Object getDefaultValueIfNull(Object value, Class clazz, Column column) {
        if (value == null && column != null && !column.null_able()) {
            Field field = daoFactory.getEntityHelper().getField(clazz, column);
            if (String.class.equals(field.getType())) {
                value = Utility.EMPTY;
            } else if (Integer.class.equals(field.getType())) {
                value = 0;
            } else if (Float.class.equals(field.getType())) {
                value = 0f;
            } else if (Long.class.equals(field.getType())) {
                value = 0L;
            } else if (Double.class.equals(field.getType())) {
                value = 0d;
            } else if (Byte.class.equals(field.getType())) {
                value = (byte) 0;
            } else if (Boolean.class.equals(field.getType())) {
                value = false;
            } else if (Date.class.equals(field.getType())) {
                value = new Date();
            } else if (field.getType().isEnum()) {
                if (column.enum_name()) {
                    value = Utility.EMPTY;
                } else {
                    value = 0;
                }
            }
        }
        return value;
    }

    // =================================================================================================================

    public <T extends IEntity> String getTableName(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            table = clazz.getSuperclass().getAnnotation(Table.class);
        }
        return table.name();
    }

    public <T extends IEntity> String createTable(Class<T> clazz) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("CREATE TABLE ");
        sbSql.append(getTableName(clazz));
        sbSql.append("(");
        sbSql.append(IEntity.Col_id);
        sbSql.append(" ");
        sbSql.append(daoFactory.getDialect().getIdType());
        for (Column column : daoFactory.getEntityHelper().getColumnSet(clazz)) {
            sbSql.append(",");
            sbSql.append(column.name());
            sbSql.append(" ");
            sbSql.append(daoFactory.getDialect().getDataType(daoFactory, clazz, column));
            if (!column.null_able()) {
                sbSql.append(" NOT NULL");
            }
        }
        sbSql.append(",PRIMARY KEY(");
        sbSql.append(IEntity.Col_id);
        sbSql.append("))");
        return sbSql.toString();
    }
}
