package com.esin.jdbc.entity;

import com.esin.base.utility.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseEntityString extends BaseEntity<String> {
    public BaseEntityString() {
    }

    public BaseEntityString(String id) {
        super(id);
    }

    @Override
    public boolean isNullId() {
        return Utility.isEmpty(getId());
    }

    @Override
    public String[] getIdSqlType() {
        return new String[]{"varchar(256)", "varchar(256)"};
    }

    @Override
    public void fillId(ResultSet resultSet) throws SQLException {
        setId(resultSet.getString(IEntity.Col_id));
    }
}
