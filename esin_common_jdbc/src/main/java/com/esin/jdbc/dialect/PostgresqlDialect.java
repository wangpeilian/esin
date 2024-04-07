package com.esin.jdbc.dialect;

import java.util.List;

public class PostgresqlDialect extends BaseDialect {

    @Override
    public String getIdType() {
        return "SERIAL";
    }

    @Override
    public String getLimitSql(int offset, int length, List<Object> paramList) {
        if (length > 0) {
            if (offset > 0) {
                paramList.add(length);
                paramList.add(offset);
                return " limit ? offset ?";
            } else {
                paramList.add(length);
                return " limit ?";
            }
        } else {
            return "";
        }
    }

    @Override
    protected String getDoubleType() {
        return "DOUBLE PRECISION";
    }

    @Override
    protected String getByteType() {
        return "SMALLINT";
    }

    @Override
    protected String getDateType() {
        return "TIMESTAMP";
    }
}
