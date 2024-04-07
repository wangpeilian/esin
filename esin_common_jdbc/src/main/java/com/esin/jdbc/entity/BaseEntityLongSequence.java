package com.esin.jdbc.entity;

public class BaseEntityLongSequence extends BaseEntityLong {

    @Override
    public String[] getIdSqlType() {
        return new String[]{"bigserial", "bigint"};
    }
}
