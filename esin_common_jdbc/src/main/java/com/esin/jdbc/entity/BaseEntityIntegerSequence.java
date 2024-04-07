package com.esin.jdbc.entity;

public class BaseEntityIntegerSequence extends BaseEntityInteger {
    public BaseEntityIntegerSequence() {
    }

    public BaseEntityIntegerSequence(Integer id) {
        super(id);
    }

    @Override
    public String[] getIdSqlType() {
        return new String[]{"serial", "int"};
    }
}
