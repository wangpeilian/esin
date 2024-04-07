package com.esin.jdbc.query;

import com.esin.jdbc.helper.DaoFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultConvert<T> {

    public default boolean unique() {
        return false;
    }

    public T convert(DaoFactory daoFactory, ResultSet row, List<String> names, int index) throws SQLException;

}
