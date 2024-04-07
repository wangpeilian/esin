package com.esin.jdbc.query;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;
import com.esin.jdbc.query.criterion.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultConvertFactory {

    public static <T> ResultConvert<T> Unique(ResultConvert<T> resultConvert) {
        return new ResultConvert<T>() {
            @Override
            public boolean unique() {
                return true;
            }

            @Override
            public T convert(DaoFactory daoFactory, ResultSet row, java.util.List<java.lang.String> names, int index) throws SQLException {
                return resultConvert.convert(daoFactory, row, names, index);
            }
        };
    }

    public static final ResultConvert<Object> Object = (daoFactory, row, names, index) -> row.getObject(1);

    public static final ResultConvert<Integer> Integer = (daoFactory, row, names, index) -> row.getInt(1);

    public static final ResultConvert<Integer> IntegerUnique = Unique(Integer);

    public static final ResultConvert<Long> Long = (daoFactory, row, names, index) -> row.getLong(1);

    public static final ResultConvert<Float> Float = (daoFactory, row, names, index) -> row.getFloat(1);

    public static final ResultConvert<Double> Double = (daoFactory, row, names, index) -> row.getDouble(1);

    public static final ResultConvert<String> String = (daoFactory, row, names, index) -> row.getString(1);

    public static final ResultConvert<Boolean> Boolean = (daoFactory, row, names, index) -> row.getBoolean(1);

    public static final ResultConvert<Date> Date = (daoFactory, row, names, index) -> row.getDate(1);

    public static final ResultConvert<Object[]> Array = (daoFactory, row, names, index) -> {
        Object[] values = new Object[names.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = row.getObject(i + 1);
        }
        return values;
    };

    public static final ResultConvert<List<Object>> List = (daoFactory, row, names, index) -> Arrays.asList(Array.convert(daoFactory, row, names, index));

    public static final ResultConvert<Map<String, Object>> Map = (daoFactory, row, names, index) -> {
        Map<String, Object> rowMap = new LinkedHashMap<>(names.size());
        for (String name : names) {
            rowMap.put(name, row.getObject(name));
        }
        return rowMap;
    };

    private static final Map<Class<? extends IEntity>, ResultConvert<? extends IEntity>> EntityMap = new HashMap<>();
    private static final Map<Class<? extends IEntity>, ResultConvert<? extends IEntity>> BeanMap = new HashMap<>();

    public static <T extends IEntity> ResultConvert<T> LazyEntity(Class<T> clazz) {
        return (ResultConvert<T>) EntityMap.computeIfAbsent(clazz, key -> new ResultConvertLazyEntity<>(clazz));
    }

    public static <T extends IEntity> ResultConvert<T> Bean(Class<T> clazz) {
        return (ResultConvert<T>) BeanMap.computeIfAbsent(clazz, key -> new ResultConvertBean<>(clazz, Collections.emptyMap()));
    }

    public static <T extends IEntity> ResultConvert<T> Bean(Query<T> query) {
        Map<String, String> aliasMap = query.getSelect().getAliasMap();
        if (Utility.isEmpty(aliasMap)) {
            return Bean(query.getFrom().getClazz());
        } else {
            return new ResultConvertBean<>(query.getFrom().getClazz(), aliasMap);
        }
    }
}
