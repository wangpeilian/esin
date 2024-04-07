package com.esin.test.common;

import com.esin.jdbc.dialect.PostgresqlDialect;
import com.esin.jdbc.helper.DaoFactory;
import org.postgresql.ds.PGSimpleDataSource;

public class DaoFactoryUtil {
    public static DaoFactory createPostgresqlDaoFactory() {
        final String serverName = "localhost_jdbc_test";
        final String url = "jdbc:postgresql://localhost:5432/jdbc_test";
        final String username = "postgres";
        final String password = "1qaz@WSX";
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return new DaoFactory(serverName, dataSource, new PostgresqlDialect());
    }

/*
    public static DaoFactory createMysqlDaoFactory() {
        final String url = "jdbc:mysql://192.168.1.41:3306/esin_api";
        final String username = "root";
        final String password = "Wpl@20080228";
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return new DaoFactory(dataSource, new MysqlDialect());
    }
 */
}
