package com.esin.jdbc.dao;

public class Dao {

    private static IDao dao = null;

    public static IDao getDao() {
        return dao;
    }

    public static void setDao(IDao dao) {
        Dao.dao = dao;
    }

    public static void closeConnection() {
        if (dao != null) {
            dao.getDaoFactory().getJdbcHelper().closeConnection();
        }
    }

}
