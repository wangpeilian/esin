package com.esin.jdbc.dialect;

import java.util.List;

public class MysqlDialect extends BaseDialect {

    public String getIdType() {
        return "INTEGER AUTO_INCREMENT";
    }

    @Override
    public String getLimitSql(int offset, int length, List<Object> paramList) {
        if (length > 0) {
            if (offset > 0) {
                paramList.add(offset);
                paramList.add(length);
                return " limit ?,?";
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
        return "DOUBLE";
    }

    @Override
    protected String getByteType() {
        return "TINYINT";
    }

    @Override
    protected String getDateType() {
        return "DATETIME";
    }
}
