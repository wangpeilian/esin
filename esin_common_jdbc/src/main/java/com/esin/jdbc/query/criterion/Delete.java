package com.esin.jdbc.query.criterion;

import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.entity.IEntity;

import java.util.List;

public class Delete<T extends IEntity> extends Query<T> {

    public Delete(IDao dao, Class<T> clazz) {
        super(dao, clazz);
        getWhere().withoutUseDefaultFilterExpression();
    }

    public String getSql(List<Object> paramList) {
        String sql = getWhere().getSql(paramList);
        return "delete" + getFrom().getSql() + sql;
    }

}
