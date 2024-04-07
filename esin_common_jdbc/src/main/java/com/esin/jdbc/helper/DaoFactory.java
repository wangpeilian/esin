package com.esin.jdbc.helper;

import com.esin.jdbc.dialect.IDialect;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

public class DaoFactory {
    private final String serverName;
    private final ConnectionPoolDataSource connectionPoolDataSource;
    private final DataSource dataSource;
    private final IDialect dialect;
    private DaoHelper daoHelper = new DaoHelper(this);
    private JdbcHelper jdbcHelper = new JdbcHelper(this);
    private SqlHelper sqlHelper = new SqlHelper(this);
    private EntityHelper entityHelper = new EntityHelper(this);
    private TableHelper tableHelper = new TableHelper(this);
    private CacheHelper cacheHelper = new CacheHelper(this);

    public DaoFactory(String serverName, ConnectionPoolDataSource connectionPoolDataSource, IDialect dialect) {
        this.serverName = serverName;
        this.connectionPoolDataSource = connectionPoolDataSource;
        this.dataSource = null;
        this.dialect = dialect;
    }

    public DaoFactory(String serverName, DataSource dataSource, IDialect dialect) {
        this.serverName = serverName;
        this.connectionPoolDataSource = null;
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    public String getServerName() {
        return serverName;
    }

    public ConnectionPoolDataSource getConnectionPoolDataSource() {
        return connectionPoolDataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public IDialect getDialect() {
        return dialect;
    }

    public DaoHelper getDaoHelper() {
        return daoHelper;
    }

    public void setDaoHelper(DaoHelper daoHelper) {
        this.daoHelper = daoHelper;
    }

    public JdbcHelper getJdbcHelper() {
        return jdbcHelper;
    }

    public void setJdbcHelper(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    public SqlHelper getSqlHelper() {
        return sqlHelper;
    }

    public void setSqlHelper(SqlHelper sqlHelper) {
        this.sqlHelper = sqlHelper;
    }

    public EntityHelper getEntityHelper() {
        return entityHelper;
    }

    public void setEntityHelper(EntityHelper entityHelper) {
        this.entityHelper = entityHelper;
    }

    public TableHelper getTableHelper() {
        return tableHelper;
    }

    public void setTableHelper(TableHelper tableHelper) {
        this.tableHelper = tableHelper;
    }

    public CacheHelper getCacheHelper() {
        return cacheHelper;
    }

    public void setCacheHelper(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }
}
