package com.esin.jdbc.query.criterion;

import java.util.List;

public interface IStatement {

    public String getSql(List<Object> paramList);

}
