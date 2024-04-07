package com.esin.jdbc.lazy;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.BaseEntity;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;

import java.lang.reflect.Field;

public abstract class LazyEntityUtil {

    public static void setLazyLoad(DaoFactory daoFactory, IEntity entity) {
        Field field = Utility.describeFieldMap(BaseEntity.class).get("daoFactory");
        field.setAccessible(true);
        Utility.setFieldValue(field, entity, daoFactory);
        field = Utility.describeFieldMap(BaseEntity.class).get("lazyLoad");
        field.setAccessible(true);
        Utility.setFieldValue(field, entity, Boolean.TRUE);
    }

}
