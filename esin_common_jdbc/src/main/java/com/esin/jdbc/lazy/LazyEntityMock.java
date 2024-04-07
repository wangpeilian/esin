package com.esin.jdbc.lazy;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

public class LazyEntityMock {
    public static <K, T extends IEntity<K>> T getProxyEntity(DaoFactory daoFactory, Class<T> type, K id) {
        IEntity entity = Utility.newInstance(type);
        entity.setId(id);
        if (!entity.isNullId()) {
            LazyEntityUtil.setLazyLoad(daoFactory, entity);
        }
        return (T) entity;
    }
}
