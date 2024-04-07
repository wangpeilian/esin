package com.esin.jdbc.entity;

import com.esin.base.bean.IBean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public interface IEntity<K> extends IBean {

    public static final String Col_id = "id";

    public void doLoadLazy();

    public K getId();

    public void setId(K id);

    public boolean isNullId();

    public String[] getIdSqlType();

    public void fillId(ResultSet resultSet) throws SQLException;

    default public String getName() {
        return String.valueOf(getId());
    }

    default public void setName(String name) {
    }

    public Date getCreateTm();

    public void setCreateTm(Date createTm);

    public Date getUpdateTm();

    public void setUpdateTm(Date updateTm);

    public RecordStatus getRecordStatus();

    public void setRecordStatus(RecordStatus recordStatus);

    public default boolean storeSqlFile() {
        return true;
    }

}
