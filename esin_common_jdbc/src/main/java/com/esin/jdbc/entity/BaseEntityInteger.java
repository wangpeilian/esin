package com.esin.jdbc.entity;

import com.esin.base.utility.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseEntityInteger extends BaseEntity<Integer> {
    public BaseEntityInteger() {
    }

    public BaseEntityInteger(Integer id) {
        super(id);
    }

    @Override
    public boolean isNullId() {
        return Utility.isZero(getId());
    }

    @Override
    public String[] getIdSqlType() {
        return new String[]{"int", "int"};
    }

    @Override
    public void fillId(ResultSet resultSet) throws SQLException {
        setId(resultSet.getInt(IEntity.Col_id));
    }
}
