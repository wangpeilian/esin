package com.esin.jdbc.dialect;

import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

import java.util.List;

public interface IDialect {

    public String getIdType();

    public String getDataType(DaoFactory daoFactory, Class<? extends IEntity> clazz, Column column);

    public String getLimitSql(int offset, int length, List<Object> paramList);

}
