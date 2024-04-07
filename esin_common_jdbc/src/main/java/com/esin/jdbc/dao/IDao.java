package com.esin.jdbc.dao;

import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;
import com.esin.jdbc.query.QueryCriteria;
import com.esin.jdbc.query.ResultConvert;
import com.esin.jdbc.query.criterion.Criterion;
import com.esin.jdbc.query.criterion.Delete;
import com.esin.jdbc.query.criterion.ICriterion;
import com.esin.jdbc.query.criterion.Query;
import com.esin.jdbc.query.criterion.Update;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IDao {

    public DaoFactory getDaoFactory();

    public <K, T extends IEntity<K>> Query<T> createQuery(Class<T> clazz);

    public <K, T extends IEntity<K>> Update<T> createUpdate(Class<T> clazz);

    public <K, T extends IEntity<K>> Delete<T> createDeleteDestroy(Class<T> clazz);

    public <K, T extends IEntity<K>> String getTableName(Class<T> clazz);

    public <K, T extends IEntity<K>> T loadLazyEntity(Class<T> clazz, K id);

    public <K, T extends IEntity<K>> T load(Class<T> clazz, K id);

    public <K, T extends IEntity<K>> T load(T entity);

    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, String name, Object value);

    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, Map<String, Object> paramMap);

    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, ICriterion... criteria);

    public <K, T extends IEntity<K>> int count(Class<T> clazz, Criterion... criteria);

    public <K, T extends IEntity<K>> Map<K, T> map(Class<T> clazz, Collection<K> idList);

    public <T> List<T> query(ResultConvert<T> resultConvert, String sql, Object... params);

    public <T extends IEntity, B extends QueryCriteria<T>> int count(B queryCriteria);

    public <T extends IEntity, B extends QueryCriteria<T>> B query(B queryCriteria);

    public <K, T extends IEntity<K>> T insert(T entity);

    public <K, T extends IEntity<K>> int insert(List<T> entityList);

    public <K, T extends IEntity<K>> boolean update(T entity, String... names);

    public <K, T extends IEntity<K>> int update(List<T> entityList, String... names);

    public <K, T extends IEntity<K>> T save(T entity);

    public <K, T extends IEntity<K>> int save(List<T> entityList);

    public <K, T extends IEntity<K>> boolean delete(T entity);

    public <K, T extends IEntity<K>> int delete(List<T> entityList);

    public <K, T extends IEntity<K>> boolean delete(Class<T> clazz, K id);

    public <K, T extends IEntity<K>> int delete(Class<T> clazz, Collection<K> idList);

    public <K, T extends IEntity<K>> boolean delete_destroy(T entity);

    public <K, T extends IEntity<K>> int delete_destroy(List<T> entityList);

    public <K, T extends IEntity<K>> boolean delete_destroy(Class<T> clazz, K id);

    public <K, T extends IEntity<K>> int delete_destroy(Class<T> clazz, Collection<K> idList);

    public void cascadeInsert(List<? extends IEntity> entityList);

    public void cascadeUpdate(List<? extends IEntity> entityList);

    public <K, T extends IEntity<K>> void cascadeDelete(Class<T> clazz, Collection<K> idList);

    public int execute(String sql, Object... params);

    public int batch(String sql, List<Object[]> paramsList);

    public <K, T extends IEntity<K>> void evict(T entity);

    public <K, T extends IEntity<K>> void evict(List<T> entityList);

    public void clearCache();

    public void updateCachedEntitiesIfChanged();

    public void doTransactionTask(Runnable task);

}
