package com.esin.jdbc.query.criterion;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class Select<T extends IEntity> extends StatementName<T, Select<T>> {

    private final Map<String, String> aliasMap = new LinkedHashMap<>();

    private boolean distinct = false;
    private String tempTableName = Utility.EMPTY;

    protected Select(Query<T> query) {
        super(query);
    }

    public Select<T> setDistinct() {
        this.distinct = true;
        return this;
    }

    public Select<T> setTempTableName(String tempTableName) {
        this.tempTableName = tempTableName;
        return this;
    }

    public Select<T> addEntity() {
        if (nameMap.containsKey(getQuery().getAlias() + ".*")) {
            return this;
        } else {
            return addName(getQuery().getAlias() + ".*");
        }
    }

    public Select<T> addFunc(SqlFunc.Aggregate aggregate, String name) {
        return super.addName(aggregate.sqlFunc.convert(name));
    }

    public Select<T> addFunc(SqlFunc.Aggregate aggregate, String name, String alias) {
        return super.addName(aggregate.sqlFunc.convert(name), alias);
    }

    @Override
    protected String getColumnName(String name) {
        if (name.equals(getQuery().getAlias() + ".*")) {
            return name;
        } else if (name.contains(".") && !name.contains("(") && !aliasMap.containsValue(name)) {
            String alias = "col_" + (aliasMap.size() + 1);
            nameMap.put(name, alias);
            aliasMap.put(alias, name);
        }
        if (name.startsWith("distinct ")) {
            return "distinct " + super.getColumnName(name.substring("distinct ".length()));
        } else if (name.contains("distinct ")) {
            String[] values = name.split("distinct ");
            return values[0] + "distinct " + super.getColumnName(values[0] + values[1]).substring(values[0].length());
        } else {
            return super.getColumnName(name);
        }
    }

    public Map<String, String> getAliasMap() {
        if (Utility.isEmpty(aliasMap)) {
            getSql();
        }
        return aliasMap;
    }

    @Override
    protected String getSql() {
        if (Utility.isEmpty(nameMap)) {
            addEntity();
        }
        return getSql("select " + (distinct ? "distinct " : ""))
                + (Utility.isEmpty(tempTableName) ? Utility.EMPTY : ("into " + tempTableName + " "));
    }
}
