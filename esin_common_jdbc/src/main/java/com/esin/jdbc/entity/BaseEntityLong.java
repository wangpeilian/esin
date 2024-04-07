package com.esin.jdbc.entity;

import com.esin.base.utility.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseEntityLong extends BaseEntity<Long> {
    public BaseEntityLong() {
    }

    public BaseEntityLong(Long id) {
        super(id);
    }

    @Override
    public boolean isNullId() {
        return Utility.isZero(getId());
    }

    @Override
    public String[] getIdSqlType() {
        return new String[]{"bigint", "bigint"};
    }

    @Override
    public void fillId(ResultSet resultSet) throws SQLException {
        setId(resultSet.getLong(IEntity.Col_id));
    }
}
