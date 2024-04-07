package com.esin.jdbc.dialect;

import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

import java.util.Date;

public abstract class BaseDialect implements IDialect {

    public final String getDataType(DaoFactory daoFactory, Class<? extends IEntity> clazz, Column column) {
        Class type = daoFactory.getEntityHelper().getField(clazz, column).getType();
        if (String.class.equals(type)) {
            if (column.char_length() == 0) {
                return "VARCHAR(" + column.string_length() + ")";
            } else {
                return "CHAR(" + column.char_length() + ")";
            }
        } else if (IEntity.class.isAssignableFrom(type)) {
            return "INTEGER";
        } else if (Integer.class.equals(type)) {
            return "INTEGER";
        } else if (Float.class.equals(type)) {
            return "REAL";
        } else if (Long.class.equals(type)) {
            return "BIGINT";
        } else if (Double.class.equals(type)) {
            return getDoubleType();
        } else if (Byte.class.equals(type)) {
            return getByteType();
        } else if (type.isEnum()) {
            return column.enum_name() ? "VARCHAR(" + column.string_length() + ")" : getByteType();
        } else if (Boolean.class.equals(type)) {
            return "BOOLEAN";
        } else if (Date.class.equals(type)) {
            return getDateType();
        } else {
            throw new IllegalArgumentException("Cannot support " + type);
        }
    }

    protected abstract String getDoubleType();

    protected abstract String getByteType();

    protected abstract String getDateType();

}
