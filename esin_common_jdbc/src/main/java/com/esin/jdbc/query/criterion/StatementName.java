package com.esin.jdbc.query.criterion;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class StatementName<T extends IEntity, C extends StatementName<T, C>> extends StatementParam<T> {

    protected final Map<String, String> nameMap = new LinkedHashMap<>();

    protected StatementName(Query<T> query) {
        super(query);
    }

    @Override
    public final String getSql(List<Object> paramList) {
        return getSql();
    }

    protected abstract String getSql();

    public C addName(String name) {
        nameMap.put(name, name);
        return (C) this;
    }

    public C addName(List<String> nameList) {
        if (Utility.isNotEmpty(nameList)) {
            nameList.forEach(s -> nameMap.put(s, s));
        }
        return (C) this;
    }

    public C addName(String name, String alias) {
        nameMap.put(name, alias);
        return (C) this;
    }

    public C addName(C c) {
        if (c != null) {
            nameMap.putAll(c.nameMap);
        }
        return (C) this;
    }

    protected String getColumnName(String name) {
        return getQuery().getFrom().getColumnName(name);
    }

    protected String getSql(String prefix) {
        if (Utility.isEmpty(nameMap)) {
            return Utility.EMPTY;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String name : nameMap.keySet()) {
                if (sb.length() != 0) {
                    sb.append(",");
                }
                sb.append(getColumnName(name));
                String alias = nameMap.get(name);
                if (!name.equals(alias)) {
                    sb.append(" ");
                    sb.append(alias);
                }
            }
            return prefix + sb.toString();
        }
    }
}
