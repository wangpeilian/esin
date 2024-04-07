package com.esin.jdbc.query.criterion;

import com.esin.jdbc.entity.IEntity;

public class GroupBy<T extends IEntity> extends StatementName<T, GroupBy<T>> {
    protected GroupBy(Query<T> query) {
        super(query);
    }

    @Override
    protected String getSql() {
        return getSql(" group by ");
    }
}