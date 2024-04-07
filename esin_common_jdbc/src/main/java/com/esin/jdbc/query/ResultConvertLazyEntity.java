package com.esin.jdbc.query;

import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;
import com.esin.jdbc.lazy.LazyList;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultConvertLazyEntity<T extends IEntity> implements ResultConvert<T> {
    private static final Logger logger = Logger.getLogger(ResultConvertLazyEntity.class);

    private final Class<T> clazz;

    public ResultConvertLazyEntity(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(DaoFactory daoFactory, ResultSet row, List<String> names, int index) throws SQLException {
        T entity = null;
        try {
            entity = clazz.newInstance();
            entity.fillId(row);
            Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
            for (Column column : columnSet) {
                if (names.contains(column.name().toLowerCase())) {
                    daoFactory.getEntityHelper().setValue(entity, column, row.getObject(column.name()), true);
                }
            }
            Map<String, Field> fieldMap = Utility.describeFieldMap(clazz);
            for (Field field : fieldMap.values()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    Class<?> targetType = Utility.getCollectionType(clazz, field);
                    field.set(entity, new LazyList(daoFactory, targetType, field.getName(), entity.getId()));
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(clazz.getName() + " - " + names + " - " + index, e);
        }
        daoFactory.getCacheHelper().putCacheEntity(entity);
        return entity;
    }
}
