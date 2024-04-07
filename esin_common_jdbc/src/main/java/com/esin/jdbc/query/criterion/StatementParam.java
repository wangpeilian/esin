package com.esin.jdbc.query.criterion;

import com.esin.jdbc.entity.IEntity;

public abstract class StatementParam<T extends IEntity> implements IStatement {

    private final Query<T> query;

    protected StatementParam(Query<T> query) {
        this.query = query;
    }

    public Query<T> getQuery() {
        return query;
    }
}
