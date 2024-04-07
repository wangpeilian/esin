package com.esin.jdbc.entity;

import com.esin.base.bean.BaseBean;
import com.esin.base.utility.FormatUtil;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.helper.DaoFactory;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public abstract class BaseEntity<K> extends BaseBean implements IEntity<K> {

    transient private DaoFactory daoFactory;
    transient private Boolean lazyLoad;

    private K id;

    @Column(name = "record_status", enum_name = false, order = 9997)
    private RecordStatus recordStatus;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = FormatUtil.DateTimePattern)
    @Column(name = "create_tm", order = 9998)
    private Date createTm;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = FormatUtil.DateTimePattern)
    @Column(name = "update_tm", order = 9999)
    private Date updateTm;

    public BaseEntity() {
    }

    public BaseEntity(K id) {
        this.id = id;
    }

    @Override
    protected final Object convert(String fieldName, Object obj) {
        if (obj instanceof IEntity) {
            obj = obj.getClass().getSimpleName() + "=" + ((IEntity) obj).getId();
        } else if (obj instanceof Date) {
            obj = FormatUtil.formatDateTime((Date) obj);
        }
        return super.convert(fieldName, obj);
    }

    public final void doLoadLazy() {
        if (daoFactory != null && Boolean.TRUE.equals(lazyLoad)) {
            this.lazyLoad = false;
            BaseEntity entity = daoFactory.getDaoHelper().loadLazyEntity(this.getClass(), this.id);
            entity.lazyLoad = false;
            if ("Finalizer".equals(Thread.currentThread().getName())) {
                daoFactory.getJdbcHelper().closeConnection();
            }
            try {
                BeanUtils.copyProperties(this, entity);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
    }

    public final K getId() {
        return id;
    }

    public final void setId(K id) {
        this.id = id;
    }

    public Date getCreateTm() {
        return createTm;
    }

    public void setCreateTm(Date createTm) {
        this.createTm = createTm;
    }

    public Date getUpdateTm() {
        return updateTm;
    }

    public void setUpdateTm(Date updateTm) {
        this.updateTm = updateTm;
    }

    public RecordStatus getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(RecordStatus recordStatus) {
        this.recordStatus = recordStatus;
    }
}
