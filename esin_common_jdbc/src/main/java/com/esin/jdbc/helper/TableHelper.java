package com.esin.jdbc.helper;

import com.esin.base.exception.LogicException;
import com.esin.base.exception.SystemException;
import com.esin.base.executor.IExecutor;
import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.ClassReaderUtil;
import com.esin.base.utility.ListUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.define.EntityRename;
import com.esin.jdbc.define.Table;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.query.ResultConvertFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableHelper {

    protected final DaoFactory daoFactory;

    public TableHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    protected List<String> getTableNameList() {
        String sql = "select tablename from pg_tables where schemaname = 'public' order by tablename";
        return daoFactory.getDaoHelper().query(ResultConvertFactory.String, sql);
    }

    protected List<String[]> getColumnDefineList(Table table) {
        String sql = "select column_name, data_type, character_maximum_length, numeric_precision," +
                " numeric_scale, is_nullable, column_default from information_schema.columns" +
                " where table_name = '" + table.name() + "' order by ordinal_position";
        List<Object[]> dataList = daoFactory.getDaoHelper().query(ResultConvertFactory.Array, sql);
        List<String[]> dataList2 = new ArrayList<>(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            dataList2.add(i, new String[]{
                    String.valueOf(dataList.get(i)[0]),
                    String.valueOf(dataList.get(i)[1]),
                    String.valueOf(dataList.get(i)[2]),
                    String.valueOf(dataList.get(i)[5])
            });
        }
        return dataList2;
    }

    protected String convertSql(String sql) {
        return sql.replace("double_precision", "double precision");
    }

    public void doDropAllDeletedTables() {
        final List<String> sqlList = new ArrayList<>();
        final List<String> tableList = getTableNameList();
        for (String tableName : tableList) {
            if (tableName.startsWith("_deleted_")) {
                sqlList.add("drop table if exists " + tableName + " cascade");
            }
        }
        executeSqlList(sqlList);
    }

    public void doDropAllTablesForInitDatabase() {
        final List<String> sqlList = new ArrayList<>();
        final List<String> tableList = getTableNameList();
        for (String tableName : tableList) {
            sqlList.add("drop table if exists " + tableName + " cascade");
        }
        executeSqlList(sqlList);
    }

    public void doRestoreTableForMisDelete() {
        final List<String> sqlList = new ArrayList<>();
        final List<String> tableList = getTableNameList();
        for (String deletedTableName : tableList) {
            if (deletedTableName.startsWith("_deleted_")) {
                String restoreTableName = deletedTableName.substring("_deleted_".length());
                sqlList.add("drop table if exists " + restoreTableName + " cascade");
                sqlList.add("alter table " + deletedTableName + " rename to " + restoreTableName);
                sqlList.add("alter table " + restoreTableName + " rename constraint pk_" + deletedTableName + " to pk_" + restoreTableName);
            }
        }
        executeSqlList(sqlList);
    }

    public void doMergeTableByEntityClass(Class<?> indicateEntityType, IExecutor initTask, Package... otherEntityPackageList) {
        final Set<Class<?>> classSet = new LinkedHashSet<>();
        Set<Package> packageSet = Collections.singleton(indicateEntityType.getPackage());
        if (Utility.isNotEmpty(otherEntityPackageList)) {
            packageSet = new HashSet<>(packageSet);
            packageSet.addAll(Arrays.asList(otherEntityPackageList));
        }
        for (Package p : packageSet) {
            for (Class<?> clazz : ClassReaderUtil.getClasses(p.getName())) {
                if (clazz.getAnnotation(Table.class) != null && IEntity.class.isAssignableFrom(clazz)) {
                    classSet.add(clazz);
                    daoFactory.getEntityHelper().getColumnSet((Class<? extends IEntity>) clazz);
                }
            }
        }
        AssertUtil.check(classSet.contains(indicateEntityType), "Not found check entity type. (" + indicateEntityType.getName() + ")");

        final List<String> tableList = new ArrayList<>();
        for (Class<?> clazz : classSet) {
            checkTableAndColumnDuplicateName(tableList, clazz);
        }

        tableList.clear();
        tableList.addAll(getTableNameList());

        final List<String> sqlList = new ArrayList<>();
        boolean is_create_new_db = !tableList.contains(indicateEntityType.getAnnotation(Table.class).name());
        if (is_create_new_db) {
            for (String tableName : tableList) {
//                sqlList.add("drop table if exists " + tableName + " cascade");
                if (!tableName.startsWith("_deleted_")) {
                    String newTableName = "_deleted_" + tableName;
                    sqlList.add("alter table " + tableName + " rename to " + newTableName);
                    sqlList.add("alter table " + newTableName + " rename constraint pk_" + tableName + " to pk_" + newTableName);
                }
            }
            tableList.clear();
        }

        if (Utility.isNotEmpty(tableList)) {
            for (Class<?> clazz : classSet) {
                generateTableRenameSql(sqlList, tableList, clazz);
            }
        }
        if (Utility.isNotEmpty(sqlList)) {
            executeSqlList(sqlList);
            sqlList.clear();
        }

        final List<String> defaultColumnList = Collections.singletonList(IEntity.Col_id);
        for (Class<?> clazz : classSet) {
            generateTableSql(sqlList, clazz, tableList, defaultColumnList);
        }
        for (String tableName : tableList) {
//            sqlList.add("drop table if exists " + tableName + " cascade");
            if (!tableName.startsWith("_deleted_")) {
                String newTableName = "_deleted_" + tableName;
                sqlList.add("alter table " + tableName + " rename to " + newTableName);
                sqlList.add("alter table " + newTableName + " rename constraint pk_" + tableName + " to pk_" + newTableName);
            }
        }
        if (Utility.isNotEmpty(sqlList)) {
            executeSqlList(sqlList);
            sqlList.clear();
        }

        if (initTask != null) {
            initTask.run();
        }
    }


    private void executeSqlList(List<String> sqlList) {
        if (Utility.isNotEmpty(sqlList)) {
            daoFactory.getDaoHelper().doTransactionTask(new Runnable() {
                @Override
                public void run() {
                    for (String sql : sqlList) {
                        if (Utility.isNotEmpty(sql)) {
                            daoFactory.getDaoHelper().execute(sql);
                        }
                    }
                }
            });
        }
    }

    private void checkTableAndColumnDuplicateName(final List<String> tableList, final Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return;
        }
        AssertUtil.check(table.name().toLowerCase().equals(table.name()), "The table name cannot has upper character. (" + table.name() + ")");
        if (tableList.contains(table.name())) {
            throw new LogicException("Duplicate table name : " + clazz.getSimpleName() + " : " + table.name());
        }
        tableList.add(table.name());
        EntityRename renameTable = clazz.getAnnotation(EntityRename.class);
        if (renameTable != null && Utility.isNotEmpty(renameTable.fromName())) {
            for (String tableName : renameTable.fromName()) {
                if (tableList.contains(tableName)) {
                    throw new LogicException("Duplicate table name : " + clazz.getSimpleName() + " : " + tableName);
                }
                tableList.add(tableName);
            }
        }

        final List<String> columnList = new ArrayList<>();
        Map<String, Field> fieldMap = Utility.describeFieldMap(clazz);
        for (Field field : fieldMap.values()) {
            if (List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                AssertUtil.check(column.name().toLowerCase().equals(column.name()), "The column name cannot has upper character. (" + table.name() + "." + column.name() + ")");
                if (columnList.contains(column.name())) {
                    throw new LogicException("Duplicate column name : " + clazz.getSimpleName() + "." + field.getName() + " : " + column.name());
                }
                columnList.add(column.name());
            }
            EntityRename renameColumn = field.getAnnotation(EntityRename.class);
            if (renameColumn != null && Utility.isNotEmpty(renameColumn.fromName())) {
                for (String columnName : renameColumn.fromName()) {
                    if (columnList.contains(columnName)) {
                        throw new LogicException("Duplicate column name : " + clazz.getSimpleName() + "." + field.getName() + " : " + columnName);
                    }
                    columnList.add(columnName);
                }
            }
        }
    }

    private void generateTableRenameSql(final List<String> sqlList, final List<String> tableList, final Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return;
        }
        if (!tableList.contains(table.name())) {
            EntityRename renameTable = clazz.getAnnotation(EntityRename.class);
            if (renameTable != null && Utility.isNotEmpty(renameTable.fromName())) {
                for (String oldTableName : renameTable.fromName()) {
                    if (tableList.contains(oldTableName)) {
                        sqlList.add("alter table " + oldTableName + " rename to " + table.name());
                        tableList.remove(oldTableName);
                        tableList.add(table.name());
                        break;
                    }
                }
            }
        }
    }

    protected void addCreateTableSql(List<String> sqlList, Class<?> clazz, Table table, List<String> defaultColumnList) {
        String idType = null;
        try {
            idType = ((IEntity) clazz.newInstance()).getIdSqlType()[0];
        } catch (IllegalAccessException | InstantiationException e) {
            throw new SystemException("entity type error : " + clazz.getName(), e);
        }
        StringBuilder sb = new StringBuilder("create table " + table.name() + " (" +
                "id " + idType + " not null,"
        );
        Map<String, Field> fieldMap = Utility.describeFieldMap(clazz);
        if (fieldMap.containsKey("recordStatus")) {
            fieldMap.put("recordStatus", fieldMap.remove("recordStatus"));
        }
        if (fieldMap.containsKey("createTm")) {
            fieldMap.put("createTm", fieldMap.remove("createTm"));
        }
        if (fieldMap.containsKey("updateTm")) {
            fieldMap.put("updateTm", fieldMap.remove("updateTm"));
        }
        fillColumnInfo(clazz, defaultColumnList, fieldMap, new LinkedHashMap<>(), new LinkedHashMap<>(), sb);
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        sqlList.add(convertSql(sb.toString()));
        sqlList.add("alter table " + table.name() + " add constraint pk_" + table.name() + " primary key(id)");
    }

    private void generateTableSql(List<String> sqlList, Class<?> clazz, List<String> tableList, List<String> defaultColumnList) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return;
        }
        if (tableList.contains(table.name())) {
            tableList.remove(table.name());
            checkTableColumn(sqlList, table, clazz, defaultColumnList);
        } else {
            addCreateTableSql(sqlList, clazz, table, defaultColumnList);
        }
    }

    protected void fillColumnInfo(Class<?> clazz, List<String> defaultColumnList, Map<String, Field> fieldMap,
                                  Map<String, Column> columnMap, Map<String, List<String>> renameMap, StringBuilder sb) {
        for (Field field : fieldMap.values()) {
            String columnName = null;
            String type = null;
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                if (Collection.class.isAssignableFrom(Utility.getFieldType(field))) {
                    continue;
                }
                if (defaultColumnList.contains(column.name())) {
                    continue;
                }
                columnName = column.name();
                columnMap.put(columnName, column);
                EntityRename rename = field.getAnnotation(EntityRename.class);
                if (rename != null && Utility.isNotEmpty(rename.fromName())) {
                    renameMap.put(columnName, Arrays.asList(rename.fromName()));
                }
                if (IEntity.class.isAssignableFrom(Utility.getFieldType(field))) {
                    String idType = null;
                    try {
                        idType = ((IEntity) Utility.getFieldType(field).newInstance()).getIdSqlType()[1];
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new SystemException("entity type error : " + clazz.getName(), e);
                    }
                    type = idType;
                } else if (field.getType().isEnum()) {
                    if (column.enum_name()) {
                        type = "varchar(" + column.string_length() + ")";
                    } else {
                        type = "smallint";
                    }
                } else if (String.class.equals(Utility.getFieldType(field))) {
                    if (column.char_length() == 0) {
                        type = "varchar(" + column.string_length() + ")";
                    } else {
                        type = "char(" + column.char_length() + ")";
                    }
                } else if (Integer.class.equals(Utility.getFieldType(field))) {
                    type = "int";
                } else if (Long.class.equals(Utility.getFieldType(field))) {
                    type = "bigint";
                } else if (Float.class.equals(Utility.getFieldType(field))) {
                    type = "real";
                } else if (Double.class.equals(Utility.getFieldType(field))) {
                    type = "double_precision";
                } else if (Boolean.class.equals(Utility.getFieldType(field))) {
                    type = "boolean";
                } else if (Date.class.equals(Utility.getFieldType(field))) {
                    type = "timestamp";
                } else if (Byte.class.equals(Utility.getFieldType(field))) {
                    type = "smallint";
                } else {
                    AssertUtil.check(false, "不支持该字段类型：" + clazz.getSimpleName() + "." + field.getName() + "->" + Utility.getFieldType(field));
                }
                if (!column.null_able()) {
                    type += " not null";
                }
                AssertUtil.check(Utility.isNotEmpty(columnName), "找不到配置的列字段名称：" + clazz.getSimpleName() + "." + field.getName());
                sb.append(columnName + " " + type + ",");
            }
        }
    }

    private void checkTableColumn(List<String> sqlList, Table table, Class<?> clazz, List<String> defaultColumnList) {
        StringBuilder sb = new StringBuilder();
        Map<String, Field> fieldMap = Utility.describeFieldMap(clazz);
        Map<String, Column> columnMap = new LinkedHashMap<>();
        Map<String, List<String>> renameMap = new LinkedHashMap<>();
        fillColumnInfo(clazz, defaultColumnList, fieldMap, columnMap, renameMap, sb);
        List<String> entityColumnList = Arrays.asList(sb.toString().split(","));
        Map<String, String> entityColumnMap = ListUtil.map(entityColumnList, new ListUtil.ConvertKey<String, String>() {
            @Override
            public String getKey(String value) {
                return value.split(" ")[0];
            }
        });
        entityColumnMap.remove("");

        List<String[]> dataList = getColumnDefineList(table);
        Map<String, String> tableColumnMap = ListUtil.map(dataList, new ListUtil.Convert<String[], String, String>() {
            @Override
            public String getKey(String[] value) {
                return value[0];
            }

            @Override
            public String getValue(String[] value) {
                String column = value[0] + " ";
                if ("integer".equals(value[1])) {
                    column += "int";
                } else if ("character varying".equals(value[1]) || "varchar".equals(value[1])) {
                    column += "varchar(" + value[2] + ")";
                } else if ("char".equals(value[1])) {
                    column += "char(" + value[2] + ")";
                } else if ("boolean".equals(value[1])) {
                    column += "boolean";
                } else if ("smallint".equals(value[1])) {
                    column += "smallint";
                } else if ("bigint".equals(value[1])) {
                    column += "bigint";
                } else if ("real".equals(value[1])) {
                    column += "real";
                } else if ("double precision".equals(value[1]) || "double".equals(value[1])) {
                    column += "double_precision";
                } else if ("uuid".equals(value[1])) {
                    column += "uuid";
                } else if ("timestamp".equals(value[1])) {
                    column += "timestamp";
                } else if ("timestamp without time zone".equals(value[1])) {
                    column += "timestamp";
                } else {
                    AssertUtil.check(false, "不支持该字段类型：" + table.name() + "." + value[0] + "->" + value[1]);
                }
                if ("NO".equals(value[3])) {
                    column += " not null";
                }
                return column;
            }
        });
        tableColumnMap.keySet().removeAll(defaultColumnList);

        for (String name : entityColumnMap.keySet()) {
            if (!tableColumnMap.containsKey(name)) {
                if (renameMap.containsKey(name)) {
                    for (String column : tableColumnMap.keySet()) {
                        if (renameMap.get(name).contains(column)) {
                            sqlList.add("alter table " + table.name() + " rename " + column + " to " + name);
                            tableColumnMap.put(name, tableColumnMap.remove(column));
                            break;
                        }
                    }
                } else {
                    // 不能直接添加非空的字段，如果表里面已经有数据，则会因为没有默认数据而报错
                    String column = entityColumnMap.get(name).replace(" not null", "");
                    column = convertSql(column);
                    sqlList.add("alter table " + table.name() + " add " + column);
                    column = column.split(" ")[0];
                    Object value = daoFactory.getSqlHelper().getDefaultValueIfNull(null, clazz, columnMap.get(column));
                    if (value != null) {
                        if (value instanceof String) {
                            value = "''";
                        } else if (value instanceof Number) {
                            value = "0";
                        } else if (value instanceof Boolean) {
                            value = "false";
                        } else if (value instanceof Date) {
                            value = "now()";
                        }
                        sqlList.add("update " + table.name() + " set " + column + " = " + value);
                    }
                }
            }
        }
        for (String name : entityColumnMap.keySet()) {
            if (tableColumnMap.containsKey(name) && !entityColumnMap.get(name).equals(tableColumnMap.get(name))) {
                String[] column_entity = entityColumnMap.get(name).split(" ", 3);
                String[] column_table = tableColumnMap.get(name).split(" ", 3);
                if (!column_entity[1].equals(column_table[1])) {
                    column_entity[1] = convertSql(column_entity[1]);
                    sqlList.add("alter table " + table.name() + " alter " + name + " type " + column_entity[1]);
                }
                if (column_entity.length == 3 && column_table.length == 2) {
                    // 不能直接修改成非空的字段，如果表里面已经有数据，则会因为没有默认数据而报错
//                    sqlList.add("alter table " + table.name() + " alter " + name + " set not null");
                }
                if (column_entity.length == 2 && column_table.length == 3) {
                    sqlList.add("alter table " + table.name() + " alter " + name + " drop not null");
                }
            }
        }
        for (String name : tableColumnMap.keySet()) {
            if (!entityColumnMap.containsKey(name)) {
                sqlList.add("alter table " + table.name() + " drop " + name);
            }
        }
    }
}
