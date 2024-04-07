package com.esin.jdbc.query.criterion;

import com.esin.jdbc.entity.IEntity;

import java.util.List;

public class Limit<T extends IEntity> extends StatementParam<T> {

    private int offset;
    private int length;

    protected Limit(Query<T> query) {
        super(query);
    }

    public Limit<T> setLimit(int offset, int length) {
        this.offset = offset;
        this.length = length;
        return this;
    }

    public Limit<T> setLimit(int length) {
        return setLimit(0, length);
    }

    public Limit<T> setLimit(Limit<T> limit) {
        if (limit != null) {
            setLimit(limit.offset, limit.length);
        }
        return this;
    }

    @Override
    public String getSql(List<Object> paramList) {
        return getQuery().getDao().getDaoFactory().getDialect().getLimitSql(offset, length, paramList);
    }
}
