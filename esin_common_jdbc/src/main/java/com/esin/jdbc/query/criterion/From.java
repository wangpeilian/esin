package com.esin.jdbc.query.criterion;

import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.EntityHelper;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class From<T extends IEntity> extends StatementName<T, From<T>> {

    private final Class<T> clazz;

    protected From(Query<T> query, Class<T> clazz) {
        super(query);
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getColumnName(String name) {
        String prefix = Utility.EMPTY;
        String suffix = Utility.EMPTY;
        if (name.contains("(")) {
            prefix = name.substring(0, name.indexOf("(") + 1);
            suffix = name.substring(name.indexOf(")"));
            name = name.substring(prefix.length(), name.length() - suffix.length());
        }
        if (name.startsWith(getQuery().getAlias() + ".")) {
            name = name.substring(2);
        }
        if (name.contains(".")) {
            String[] values = StringUtils.split(name, '.');
            Class<? extends IEntity> joinType = getJoinType(values[0]);
            if (!nameMap.containsKey(values[0])) {
                nameMap.put(values[0], getQuery().getAlias() + (nameMap.size() + 1));
            }
            String columnName = getColumnName(joinType, values[1]);
            return prefix + nameMap.get(values[0]) + "." + columnName + suffix;
        } else {
            return prefix + getQuery().getAlias() + "." + getColumnName(clazz, name) + suffix;
        }
    }

    protected String getColumnExpression(String expression) {
        if (expression.contains("{")) {
            String[] values = expression.split("\\{");
            StringBuilder sb = new StringBuilder(values[0]);
            for (int i = 1; i < values.length; i++) {
                String[] names = values[i].split("}", 2);
                sb.append(getColumnName(names[0]));
                sb.append(names[1]);
            }
            return sb.toString();
        } else {
            return getColumnName(expression);
        }
    }

    protected Column getColumn(String name) {
        if (name.contains("(")) {
            name = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
        }
        if (name.contains(".")) {
            String[] values = StringUtils.split(name, '.');
            Class<? extends IEntity> joinType = getJoinType(values[0]);
            if (!nameMap.containsKey(values[0])) {
                nameMap.put(values[0], getQuery().getAlias() + (nameMap.size() + 1));
            }
            return getColumn(joinType, values[1]);
        } else {
            return getColumn(clazz, name);
        }
    }

    private <R extends IEntity> Class<R> getJoinType(String name) {
        EntityHelper entityHelper = getQuery().getDao().getDaoFactory().getEntityHelper();
        Column column = entityHelper.getColumn(clazz, name);
        Field field = entityHelper.getField(clazz, column);
        return (Class<R>) field.getType();
    }

    private Column getColumn(Class<? extends IEntity> clazz, String name) {
        if (IEntity.Col_id.equals(name)) {
            return null;
        }
        EntityHelper entityHelper = getQuery().getDao().getDaoFactory().getEntityHelper();
        return entityHelper.getColumn(clazz, name);
    }

    protected String getColumnName(Class<? extends IEntity> clazz, String name) {
        if (IEntity.Col_id.equals(name)) {
            return name;
        }
        EntityHelper entityHelper = getQuery().getDao().getDaoFactory().getEntityHelper();
        Column column = entityHelper.getColumn(clazz, name);
        AssertUtil.check(column != null, "Column is not found.(" + clazz.getSimpleName() + "." + name + ")");
        return column.name();
    }

    @Override
    protected String getSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(" from ");
        sb.append(getQuery().getDao().getTableName(clazz));
        sb.append(" ");
        sb.append(getQuery().getAlias());
        if (Utility.isNotEmpty(nameMap)) {
            for (Map.Entry<String, String> entry : nameMap.entrySet()) {
                String tableField = entry.getKey();
                String tableAlias = entry.getValue();
                EntityHelper entityHelper = getQuery().getDao().getDaoFactory().getEntityHelper();
                Column column = entityHelper.getColumn(clazz, tableField);
                Field field = entityHelper.getField(clazz, column);
                String tableName = getQuery().getDao().getTableName((Class<? extends IEntity>) field.getType());
                sb.append(" left join ");
                sb.append(tableName);
                sb.append(" ");
                sb.append(tableAlias);
                sb.append(" on t.");
                sb.append(column.name());
                sb.append("=");
                sb.append(tableAlias);
                sb.append(".id");
            }
        }
        return sb.toString();
    }

    public String getWhereSqlForUpdate(String sql) {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if (Utility.isNotEmpty(nameMap)) {
            for (Map.Entry<String, String> entry : nameMap.entrySet()) {
                String tableField = entry.getKey();
                String tableAlias = entry.getValue();
                EntityHelper entityHelper = getQuery().getDao().getDaoFactory().getEntityHelper();
                Column column = entityHelper.getColumn(clazz, tableField);
                Field field = entityHelper.getField(clazz, column);
                String tableName = getQuery().getDao().getTableName((Class<? extends IEntity>) field.getType());
                sb1.append(", ");
                sb1.append(tableName);
                sb1.append(" ");
                sb1.append(tableAlias);
                sb2.append(" and t.");
                sb2.append(column.name());
                sb2.append("=");
                sb2.append(tableAlias);
                sb2.append(".id");
            }
        }
        sql = sql.replace("t.", "");
        if (sb1.length() > 0) {
            if (Utility.isEmpty(sb1)) {
                return " from " + sb1.substring(", ".length())
                        + " where " + sb2.substring(" and ".length());
            } else {
                return " from " + sb1.substring(", ".length())
                        + " where " + sb2.substring(" and ".length())
                        + " and (" + sql.substring(" where ".length()) + ")";
            }
        } else {
            return sql;
        }
    }
}
