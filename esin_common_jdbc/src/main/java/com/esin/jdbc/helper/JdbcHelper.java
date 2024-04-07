package com.esin.jdbc.helper;

import com.esin.base.exception.DBException;
import com.esin.base.utility.FileUtil;
import com.esin.base.utility.FormatUtil;
import com.esin.base.utility.Logger;
import com.esin.base.utility.ObjectUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.BaseEntityIntegerSequence;
import com.esin.jdbc.entity.BaseEntityLongSequence;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.query.ResultConvert;
import org.postgresql.jdbc.PSQLSavepoint;

import javax.sql.PooledConnection;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JdbcHelper {
    private static final Logger logger = Logger.getLogger(JdbcHelper.class);
    private static final Savepoint Savepoint_Root = new PSQLSavepoint(0);
    private static final Savepoint Savepoint_None = new PSQLSavepoint(0);

    private final DaoFactory daoFactory;
    private final Map<Thread, PooledConnection> ThreadPooledConnectionMap = new ConcurrentHashMap<>();
    private final Map<Thread, Connection> ThreadConnectionMap = new ConcurrentHashMap<>();
    private final Map<Thread, Long> ThreadConnectionTimeMap = new ConcurrentHashMap<>();

    public JdbcHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    private Connection getConnection() {
        final Thread thread = Thread.currentThread();
        ThreadConnectionTimeMap.put(thread, System.currentTimeMillis());
        Connection connection = ThreadConnectionMap.get(thread);
        if (connection == null) {
            try {
                if (daoFactory.getConnectionPoolDataSource() != null) {
                    PooledConnection pooledConnection = daoFactory.getConnectionPoolDataSource().getPooledConnection();
                    ThreadPooledConnectionMap.put(thread, pooledConnection);
                    connection = pooledConnection.getConnection();
                } else {
                    connection = daoFactory.getDataSource().getConnection();
                }
                connection.setAutoCommit(true);
                ThreadConnectionMap.put(thread, connection);
                daoFactory.getDaoHelper().clearCache();
                logger.trace("Thread Connection get : " + ThreadConnectionMap.size());
            } catch (SQLException e) {
                throw new DBException("JdbcHelper.getConnection()", e);
            }
        }
        return connection;
    }

    public void closeConnection() {
        daoFactory.getDaoHelper().clearCache();
        final Thread thread = Thread.currentThread();
        closeConnection(thread);
        if (ThreadConnectionTimeMap.size() > 10) {
            List<Map.Entry<Thread, Long>> entryList = new ArrayList<>(ThreadConnectionTimeMap.entrySet());
            entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            final long curTime = System.currentTimeMillis();
            for (int i = 0; i < entryList.size() - 10; i++) {
                if (curTime - entryList.get(i).getValue() > 60000) {
                    // 无事务的连接，1分钟超时后自动释放；有事务的连接，5分钟超时后自动释放
                    boolean close = curTime - entryList.get(i).getValue() > 300000;
                    try {
                        close |= ThreadConnectionMap.get(entryList.get(i).getKey()).getAutoCommit();
                    } catch (SQLException ignored) {
                    }
                    if (close) {
                        closeConnection(entryList.get(i).getKey());
                    }
                }
            }
        }
    }

    private void closeConnection(Thread thread) {
        ThreadConnectionTimeMap.remove(thread);
        PooledConnection pooledConnection = ThreadPooledConnectionMap.remove(thread);
        Connection connection = ThreadConnectionMap.remove(thread);
        try {
            if (pooledConnection != null) {
                pooledConnection.close();
            } else if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DBException("JdbcHelper.closeConnection()", e);
        } finally {
            logger.trace("Thread Connection close : " + ThreadConnectionMap.size());
        }
    }

    public void setTransactionIsolation(int isolationLevel) {
        try {
            getConnection().setTransactionIsolation(isolationLevel);
        } catch (SQLException e) {
            throw new DBException("JdbcHelper.setTransactionIsolation(" + isolationLevel + ")", e);
        }
    }

    public Savepoint doBeginTransaction(boolean savepoint_able) {
        try {
            Connection connection = getConnection();
            if (connection.getAutoCommit()) {
                // 无事务 -> 开启事务
                logger.trace("JdbcHelper.doBeginTransaction ...");
                connection.setAutoCommit(false);
                return Savepoint_Root;
            } else if (savepoint_able) {
                // 有事务 -> 设置保存点
                Savepoint savepoint = connection.setSavepoint();
                logger.info("JdbcHelper.setSavepoint : " + savepoint);
                return savepoint;
            } else {
                // 有事务 -> 不设保存点
                logger.info("JdbcHelper.transactionExist");
                return Savepoint_None;
            }
        } catch (SQLException e) {
            throw new DBException("JdbcHelper.doBeginTransaction()", e);
        }
    }

    public void doCommitTransaction(Savepoint savepoint) {
        try {
            Connection connection = getConnection();
            if (connection.getAutoCommit()) {
                // 无事务 -> 直接返回
                return;
            }
            if (Savepoint_Root.equals(savepoint)) {
                // 有事务 -> 全局事务提交
                logger.trace("JdbcHelper.doCommitTransaction");
                connection.commit();
                connection.setAutoCommit(true);
            } else if (!Savepoint_None.equals(savepoint)) {
                // 有事务 -> 保存点释放
                logger.trace("JdbcHelper.releaseSavepoint : " + savepoint);
                connection.releaseSavepoint(savepoint);
            }
        } catch (SQLException e) {
            logger.error("", e);
            doRollbackTransaction(null);
        }
    }

    public void doRollbackTransaction(Savepoint savepoint) {
        try {
            Connection connection = getConnection();
            if (connection.getAutoCommit()) {
                // 无事务 -> 直接返回
                return;
            }
            if (Savepoint_Root.equals(savepoint) || Savepoint_None.equals(savepoint)) {
                // 有事务 -> 全局事务回滚
                logger.trace("JdbcHelper.doRollbackTransaction ...");
                connection.rollback();
                connection.setAutoCommit(true);
            } else {
                // 有事务 -> 保存点回滚
                logger.trace("JdbcHelper.rollbackSavepoint : " + savepoint);
                connection.rollback(savepoint);
            }
        } catch (SQLException e) {
            throw new DBException("JdbcHelper.doRollbackTransaction()", e);
        }
    }

    public <T> List<T> executeQuery(ResultConvert<T> resultConvert, final String sql, final List<Object> paramList) {
        final List<T> dataList = new ArrayList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = createStatement(false, sql);
            setPreparedStatementParamList(ps, sql, paramList);
            if (resultConvert.unique()) {
                ps.setMaxRows(1);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            List<String> names = new ArrayList<>(rsmd.getColumnCount());
            for (int i = 0, size = rsmd.getColumnCount(); i < size; i++) {
                names.add(rsmd.getColumnName(i + 1).toLowerCase());
            }
            int index = 0;
            while (rs.next()) {
                dataList.add(resultConvert.convert(daoFactory, rs, names, index++));
                if (resultConvert.unique()) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new DBException("JdbcHelper.executeQuery()", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(sql, e);
                }
            }
            closeStatement(ps);
        }

        if (dataList.size() != 1) {
            logger.trace("Result : size=" + dataList.size());
        } else {
            Object value = dataList.get(0);
            if (value != null) {
                if (value.getClass().isArray()) {
                    value = Arrays.asList((Object[]) value);
                } else if (value instanceof IEntity) {
                    value = value.getClass().getSimpleName() + "=" + ((IEntity) value).getId();
                }
            }
            logger.trace("Result : " + value);
        }
        return dataList;
    }

    public void executeInsert(final IEntity<?> entity, String sql, List<Object> paramList) {
        PreparedStatement ps = null;
        try {
            boolean storeSqlFile = entity.storeSqlFile() && Utility.isNotEmpty(JdbcConfig.sql_trace_file_dir) && Utility.isNotEmpty(daoFactory.getServerName());
            boolean insertAutoIncrementId = entity.isNullId();
            ps = createStatement(insertAutoIncrementId, sql);
            sql = setPreparedStatementParamList(ps, sql, paramList);
            ps.executeUpdate();
            if (insertAutoIncrementId) {
                ResultSet resultSet = ps.getGeneratedKeys();
                if (resultSet.next()) {
                    if (entity instanceof BaseEntityIntegerSequence) {
                        ((BaseEntityIntegerSequence) entity).setId(resultSet.getInt(1));
                    } else {
                        ((BaseEntityLongSequence) entity).setId(resultSet.getLong(1));
                    }
                }
            }
            logger.trace("Result : " + entity.getClass().getSimpleName() + "=" + entity.getId());
            if (storeSqlFile) {
                List<String> sqlList = Collections.singletonList(sql);
                if (insertAutoIncrementId) {
                    sqlList = new ArrayList<>(sqlList);
                    sqlList.add("# SeqId = " + entity.getId());
                }
                writeSqlFile(sqlList);
            }
        } catch (SQLException e) {
            if (Utility.toEmpty(e.getMessage()).contains("Unique index or primary key violation")
                    || Utility.toEmpty(e.getMessage()).contains("java.sql.BatchUpdateException: Duplicate entry")) {
                throw new DBException("Duplicate prime key.", e);
            } else {
                throw new DBException("ERROR : " + sql, e);
            }
        } finally {
            closeStatement(ps);
        }
    }

    public int executeUpdate(boolean storeSqlFile, String sql, List<Object> paramList) {
        PreparedStatement ps = null;
        try {
            storeSqlFile &= Utility.isNotEmpty(JdbcConfig.sql_trace_file_dir) && Utility.isNotEmpty(daoFactory.getServerName());
            boolean insertAutoIncrementId = sql.toLowerCase().startsWith("insert into ") && !sql.toLowerCase().contains("(id,");
            ps = createStatement(insertAutoIncrementId, sql);
            sql = setPreparedStatementParamList(ps, sql, paramList);
            int result = ps.executeUpdate();
            logger.trace("Result : execute=" + result);
            if (storeSqlFile) {
                List<String> sqlList = Collections.singletonList(sql);
                if (insertAutoIncrementId) {
                    sqlList = new ArrayList<>(sqlList);
                    ResultSet resultSet = ps.getGeneratedKeys();
                    while (resultSet.next()) {
                        sqlList.add("# SeqId = " + resultSet.getObject(1));
                    }
                }
                writeSqlFile(sqlList);
            }
            return result;
        } catch (SQLException e) {
            if (Utility.toEmpty(e.getMessage()).contains("Unique index or primary key violation")
                    || Utility.toEmpty(e.getMessage()).contains("java.sql.BatchUpdateException: Duplicate entry")) {
                throw new DBException("Duplicate prime key.", e);
            } else {
                throw new DBException("ERROR : " + sql, e);
            }
        } finally {
            closeStatement(ps);
        }
    }

    public int executeBatch(boolean storeSqlFile, final String sql, final List<Object[]> paramsList) {
        PreparedStatement ps = null;
        try {
            storeSqlFile &= Utility.isNotEmpty(JdbcConfig.sql_trace_file_dir) && Utility.isNotEmpty(daoFactory.getServerName());
            boolean insertAutoIncrementId = sql.toLowerCase().startsWith("insert into ") && !sql.toLowerCase().contains("(id,");
            int count = 0;
            int size = 0;
            ps = createStatement(insertAutoIncrementId, sql);
            List<String> batchSqlList = new ArrayList<>(storeSqlFile ? paramsList.size() : 0);
            List<String> batchParamList = new ArrayList<>(storeSqlFile ? paramsList.size() : 0);
            List<String> batchAutoIdList = new ArrayList<>(storeSqlFile && insertAutoIncrementId ? paramsList.size() : 0);
            for (Object[] params : paramsList) {
                String _sql = setPreparedStatementParamList(ps, sql, Arrays.asList(params));
                if (storeSqlFile) {
                    batchSqlList.add("# =SQL= : " + _sql);
                    batchParamList.add("# Param : " + ObjectUtil.toString(Arrays.stream(params)
                            .map(o -> o instanceof IEntity ? ((IEntity<?>) o).isNullId() ? null : ((IEntity<?>) o).getId() : o)
                            .collect(Collectors.toList())));
                }
                ps.addBatch();
                if (++count % 10000 == 0) {
                    size += Arrays.stream(ps.executeBatch()).sum();
                    if (storeSqlFile && insertAutoIncrementId) {
                        ResultSet resultSet = ps.getGeneratedKeys();
                        while (resultSet.next()) {
                            batchAutoIdList.add("# SeqId = " + resultSet.getObject(1));
                        }
                    }
                    ps.clearBatch();
                    logger.trace("Clear : batch=" + size);
                }
            }
            size += Arrays.stream(ps.executeBatch()).sum();
            if (storeSqlFile && insertAutoIncrementId) {
                ResultSet resultSet = ps.getGeneratedKeys();
                while (resultSet.next()) {
                    batchAutoIdList.add("# SeqId = " + resultSet.getObject(1));
                }
            }
            logger.trace("Result : batch=" + size);
            if (storeSqlFile) {
                List<String> batchList = new ArrayList<>(batchSqlList.size() + batchParamList.size() + batchAutoIdList.size() + 3);
                batchList.add("# Batch begin : " + paramsList.size());
                batchList.add("# SQL : " + sql);
                if (Utility.isEmpty(batchAutoIdList)) {
                    batchList.addAll(batchParamList);
                } else {
                    int minSize = Math.min(batchParamList.size(), batchAutoIdList.size());
                    for (int i = 0; i < minSize; i++) {
                        batchList.add(batchParamList.get(i));
                        batchList.add(batchAutoIdList.get(i));
                    }
                    for (int i = minSize; i < batchParamList.size(); i++) {
                        batchList.add(batchParamList.get(i));
                    }
                    for (int i = minSize; i < batchAutoIdList.size(); i++) {
                        batchList.add(batchAutoIdList.get(i));
                    }
                }
                batchList.addAll(batchSqlList);
                batchList.add("# Batch end : " + paramsList.size());
                writeSqlFile(batchList);
            }
            return size;
        } catch (SQLException e) {
            if (Utility.toEmpty(e.getMessage()).contains("Unique index or primary key violation")
                    || Utility.toEmpty(e.getMessage()).contains("java.sql.BatchUpdateException: Duplicate entry")) {
                throw new DBException("Duplicate prime key.", e);
            } else {
                throw new DBException(sql, e);
            }
        } finally {
            closeStatement(ps);
        }
    }

    private PreparedStatement createStatement(boolean insertAutoIncrementId, String sql) throws SQLException {
//        logger.trace("  SQL  : " + sql);
        Connection connection = getConnection();
        return insertAutoIncrementId ? connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
    }

    private String setPreparedStatementParamList(PreparedStatement ps, String sql, List<Object> paramList) throws SQLException {
        if (Utility.isNotEmpty(paramList)) {
            for (int i = 0; i < paramList.size(); i++) {
                Object param = paramList.get(i);
                if (param instanceof IEntity) {
                    param = ((IEntity<?>) param).isNullId() ? null : ((IEntity<?>) param).getId();
                } else if (param instanceof Calendar) {
                    param = new Timestamp(((Calendar) param).getTimeInMillis());
                } else if (param instanceof Date && !(param instanceof java.sql.Date)) {
                    param = new Timestamp(((Date) param).getTime());
                } else if (param instanceof UUID) {
                    // todo : 只有不支持UUID的数据库，才做类型转换
//                    param = param.toString();
                }
                ps.setObject(i + 1, param);
            }

//            logger.trace(" Param : " + Arrays.asList(params));
            int count = 0;
            while (sql.contains("?")) {
                sql = sql.replaceFirst("\\?", "_p" + (count++) + "_");
            }
            for (int i = 0; i < paramList.size(); i++) {
                Object param = paramList.get(i);
                if (param instanceof IEntity) {
                    param = ((IEntity<?>) param).isNullId() ? null : ((IEntity<?>) param).getId();
                }
                sql = sql.replace("_p" + i + "_", convert2String(param));
            }
        }
        logger.trace(" =SQL= : " + sql);
        return sql;
    }

    private synchronized void writeSqlFile(List<String> sqlList) {
        if (Utility.isNotEmpty(JdbcConfig.sql_trace_file_dir) && Utility.isNotEmpty(daoFactory.getServerName())) {
            String datetime = FormatUtil.formatDateTime(new Date()).replace("-", "").replace(":", "").replace(" ", "-");
            try {
                OutputStream os = new FileOutputStream(JdbcConfig.sql_trace_file_dir + "/"
                        + daoFactory.getServerName() + "_" + datetime.substring(0, 8) + ".sql", true);
                FileUtil.save(os, FileUtil.UTF8, handler -> sqlList.forEach(sql -> handler.doHandle(datetime + ":" + sql + "\r\n")));
            } catch (FileNotFoundException ignored) {
            }
        }
    }

    private String convert2String(Object param) {
        if (param == null) {
            return Utility.EMPTY;
        } else if (param instanceof String || param instanceof UUID) {
            return "'" + param + "'";
        } else if (param instanceof Date) {
            return "to_date('" + FormatUtil.formatDateTime((Date) param) + "','yyyy-mm-dd hh24:mi:ss')";
        } else if (param instanceof Calendar) {
            return "to_date('" + FormatUtil.formatDateTime(((Calendar) param).getTime()) + "','yyyy-mm-dd hh24:mi:ss')";
        } else {
            return String.valueOf(param);
        }
    }

    private void closeStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }
}
