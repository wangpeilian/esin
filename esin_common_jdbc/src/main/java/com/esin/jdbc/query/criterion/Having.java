package com.esin.jdbc.query.criterion;

import com.esin.jdbc.entity.IEntity;

import java.util.List;

public class Having<T extends IEntity> extends Where<T> {

    protected Having(Query<T> query) {
        super(query);
        withoutUseDefaultFilterExpression();
    }

    @Override
    public String getSql(List<Object> paramList) {
        return getSql(" having ", paramList);
    }
}
