package com.esin.jdbc.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BaseEntityUUID extends BaseEntity<UUID> {
    public BaseEntityUUID() {
    }

    public BaseEntityUUID(UUID id) {
        super(id);
    }

    @Override
    public boolean isNullId() {
        return getId() == null;
    }

    @Override
    public String[] getIdSqlType() {
        return new String[]{"uuid", "uuid"};
    }

    @Override
    public void fillId(ResultSet resultSet) throws SQLException {
        setId(UUID.fromString(resultSet.getString(IEntity.Col_id)));
    }
}
