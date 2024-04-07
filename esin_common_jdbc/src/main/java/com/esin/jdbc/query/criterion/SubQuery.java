package com.esin.jdbc.query.criterion;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.List;

public class SubQuery<T extends IEntity> extends Criterion.Single {

    public static <E extends IEntity> SubQuery<E> create(SqlFunc.SubQueryMethod method, Query<E> query) {
        return new SubQuery<>(method, query);
    }

    private final SqlFunc.SubQueryMethod method;

    private final Query<T> query;

    private SubQuery(SqlFunc.SubQueryMethod method, Query<T> query) {
        super(null);
        this.method = method;
        this.query = query;
    }

    @Override
    public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
        String sql = query.setAlias("t" + Math.abs(System.nanoTime())).getSql(paramList);
        String _func = method == null || SqlFunc.SubQueryMethod.Null.equals(method) ? Utility.EMPTY : method.name();
        return _func + "(" + sql + ")";
    }
}
