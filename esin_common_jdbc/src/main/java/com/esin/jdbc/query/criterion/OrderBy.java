package com.esin.jdbc.query.criterion;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.query.OrderByBean;

import java.util.LinkedHashMap;
import java.util.List;

public class OrderBy<T extends IEntity> extends StatementName<T, OrderBy<T>> {
    protected OrderBy(Query<T> query) {
        super(query);
    }

    @Override
    protected String getSql() {
        return getSql(" order by ");
    }

    @Override
    protected String getColumnName(String name) {
        if (getQuery().getSelect().nameMap.containsValue(name)) {
            return name;
        } else {
            return getQuery().getFrom().getColumnExpression(name);
        }
    }

    @Override
    public OrderBy<T> addName(String name) {
        return asc(name);
    }

    public OrderBy<T> asc(String name) {
        return super.addName(name, "asc");
    }

    public OrderBy<T> desc(String name) {
        return super.addName(name, "desc");
    }

    public OrderBy<T> add(LinkedHashMap<String, Boolean> orderMap) {
        if (Utility.isNotEmpty(orderMap)) {
            for (String name : orderMap.keySet()) {
                if (Boolean.FALSE.equals(orderMap.get(name))) {
                    desc(name);
                } else {
                    asc(name);
                }
            }
        }
        return this;
    }

    public OrderBy<T> add(OrderByBean... orderList) {
        if (Utility.isNotEmpty(orderList)) {
            for (OrderByBean orderBy : orderList) {
                add(orderBy.getOrderMap());
            }
        }
        return this;
    }

    public OrderBy<T> add(List<OrderBy<T>> orderByList) {
        if (Utility.isNotEmpty(orderByList)) {
            for (OrderBy<T> orderBy : orderByList) {
                addName(orderBy);
            }
        }
        return this;
    }
}
