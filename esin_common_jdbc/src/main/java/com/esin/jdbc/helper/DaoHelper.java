package com.esin.jdbc.helper;

import com.esin.base.executor.IExecutorAA;
import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.BeanWrapper;
import com.esin.base.utility.ListUtil;
import com.esin.base.utility.MapUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.entity.RecordStatus;
import com.esin.jdbc.query.LimitBean;
import com.esin.jdbc.query.OrderByBean;
import com.esin.jdbc.query.QueryCriteria;
import com.esin.jdbc.query.ResultConvert;
import com.esin.jdbc.query.SelectBean;
import com.esin.jdbc.query.criterion.Criterion;
import com.esin.jdbc.query.criterion.Delete;
import com.esin.jdbc.query.criterion.Expression;
import com.esin.jdbc.query.criterion.ICriterion;
import com.esin.jdbc.query.criterion.Query;
import com.esin.jdbc.query.criterion.SqlFunc;
import com.esin.jdbc.query.criterion.Update;
import com.esin.jdbc.service.ServiceFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DaoHelper implements IDao {

    private final DaoFactory daoFactory;

    public DaoHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    @Override
    public <K, T extends IEntity<K>> Query<T> createQuery(Class<T> clazz) {
        return new Query<>(this, clazz);
    }

    @Override
    public <K, T extends IEntity<K>> Update<T> createUpdate(Class<T> clazz) {
        return new Update<>(this, clazz);
    }

    @Override
    public <K, T extends IEntity<K>> Delete<T> createDeleteDestroy(Class<T> clazz) {
        return new Delete<>(this, clazz);
    }

    @Override
    public <K, T extends IEntity<K>> String getTableName(Class<T> clazz) {
        return daoFactory.getSqlHelper().getTableName(clazz);
    }

    @Override
    public <K, T extends IEntity<K>> T loadLazyEntity(Class<T> clazz, K id) {
        return ListUtil.first(createQuery(clazz)
                .getWhere().addCriterion(Expression.eq(IEntity.Col_id, id)).getQuery()
                .queryLazyEntityList());
    }

    @Override
    public <K, T extends IEntity<K>> T load(Class<T> clazz, K id) {
        T entity = Utility.newInstance(clazz);
        entity.setId(id);
        entity = daoFactory.getCacheHelper().getCacheEntity(entity);
        if (entity != null) {
            return entity;
        }
        return ListUtil.first(createQuery(clazz)
                .getWhere().addCriterion(Expression.eq(IEntity.Col_id, id)).getQuery()
                .queryBeanList());
    }

    @Override
    public <K, T extends IEntity<K>> T load(T entity) {
        return load((Class<T>) entity.getClass(), entity.getId());
    }

    @Override
    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, String name, Object value) {
        return list(clazz, Criterion.of(name, value));
    }

    @Override
    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, Map<String, Object> paramMap) {
        ICriterion[] criteria = null;
        if (Utility.isNotEmpty(paramMap)) {
            criteria = paramMap.entrySet().stream()
                    .map(entry -> Criterion.of(entry.getKey(), entry.getValue()))
                    .toArray(ICriterion[]::new);
        }
        return list(clazz, criteria);
    }

    @Override
    public <K, T extends IEntity<K>> List<T> list(Class<T> clazz, ICriterion... criteria) {
        Query<T> query = createQuery(clazz);
        if (Utility.isNotEmpty(criteria)) {
            for (ICriterion criterion : criteria) {
                if (criterion instanceof SelectBean) {
                    query.getSelect().addEntity().addName(((SelectBean) criterion).getQueryJoinColumnList());
                } else if (criterion instanceof OrderByBean) {
                    query.getOrderBy().add((OrderByBean) criterion);
                } else if (criterion instanceof LimitBean) {
                    query.getLimit().setLimit(((LimitBean) criterion).getOffset(), ((LimitBean) criterion).getLength());
                } else if (criterion instanceof Criterion) {
                    query.getWhere().addCriterion((Criterion) criterion);
                }
            }
        }
        return query.queryBeanList();
    }

    @Override
    public <K, T extends IEntity<K>> int count(Class<T> clazz, Criterion... criteria) {
        return createQuery(clazz)
                .getSelect().addFunc(SqlFunc.Aggregate.Count, "id")
                .getQuery().getWhere().addCriterion(criteria)
                .getQuery().queryInt();
    }

    @Override
    public <K, T extends IEntity<K>> Map<K, T> map(Class<T> clazz, Collection<K> idList) {
        List<T> dataList = list(clazz, Utility.isEmpty(idList) ? null : MapUtil.of(IEntity.Col_id, idList));
        return ListUtil.map(dataList, (ListUtil.ConvertKey<T, K>) IEntity::getId);
    }

    @Override
    public <T> List<T> query(ResultConvert<T> resultConvert, String sql, Object... params) {
        return daoFactory.getJdbcHelper().executeQuery(resultConvert, sql, Arrays.asList(params));
    }

    private <K, T extends IEntity<K>> Query<T> buildQuery(QueryCriteria<T> queryCriteria) {
        AssertUtil.check(queryCriteria != null, "Cannot query null QueryCriteria.");
        AssertUtil.check(queryCriteria.getEntity(), "Cannot query null QueryCriteria(entity).");

        T entity = queryCriteria.getEntity();
        Class<T> clazz = (Class<T>) entity.getClass();
        Criterion criterion = Criterion.and();
        buildEntity(entity, criterion, Utility.EMPTY, false);

        T fuzzyEntity = queryCriteria.getFuzzyEntity();
        if (fuzzyEntity != null) {
            Criterion fuzzyCriterion = Criterion.or();
            buildEntity(fuzzyEntity, fuzzyCriterion, Utility.EMPTY, true);
            criterion = Criterion.and(criterion, fuzzyCriterion);
        }

        if (Utility.isNotEmpty(queryCriteria.getFuzzyNameList()) && Utility.isNotEmpty(queryCriteria.getFuzzyText())) {
            String[] textList = new String[]{queryCriteria.getFuzzyText().trim()};
            if (queryCriteria.getFuzzyText().contains("|")) {
                textList = queryCriteria.getFuzzyText().trim().split("\\|");
            }
            Criterion fuzzyCriterion = Criterion.or();
            for (String text : textList) {
                String[] values = new String[]{text.trim()};
                if (text.contains("&")) {
                    values = text.split("&");
                }
                Criterion and = Criterion.and();
                for (String value : values) {
                    value = value.trim();
                    if (Utility.isNotEmpty(value)) {
                        Criterion or = Criterion.or();
                        for (String name : queryCriteria.getFuzzyNameList()) {
                            if (value.startsWith("%") || value.endsWith("%")) {
                                or.add(Expression.ilike(name, value, SqlFunc.LikeMatch.Exact));
                            } else {
                                or.add(Expression.ilike(name, value, SqlFunc.LikeMatch.Any));
                            }
                        }
                        and.add(or);
                    }
                }
                fuzzyCriterion.add(and);
            }
            criterion = Criterion.and(criterion, fuzzyCriterion);
        }

        return queryCriteria.customize(createQuery(clazz).getWhere().addCriterion(criterion).getQuery());
    }

    private <K, T extends IEntity<K>> void buildEntity(IEntity entity, Criterion criterion, String parent, boolean fuzzy) {
        Class<T> clazz = (Class<T>) entity.getClass();
        Set<Column> columnSet = daoFactory.getEntityHelper().getColumnSet(clazz);
        for (Column column : columnSet) {
            Field field = daoFactory.getEntityHelper().getField(clazz, column);
            Object value = daoFactory.getEntityHelper().getValue(entity, column);
            if (value != null) {
                if (Utility.isNotEmpty(parent)) {
                    if (!(value instanceof IEntity)) {
                        if (fuzzy) {
                            if (value instanceof String && Utility.isNotEmpty(value.toString())) {
                                criterion.add(Expression.ilike(parent + field.getName(), value.toString(), SqlFunc.LikeMatch.Any));
                            }
                        } else {
                            criterion.add(Expression.eq(parent + field.getName(), value));
                        }
                    }
                } else if (value instanceof IEntity) {
                    buildEntity((IEntity) value, criterion, field.getName() + ".", fuzzy);
                } else {
                    if (fuzzy) {
                        if (value instanceof String && Utility.isNotEmpty(value.toString())) {
                            criterion.add(Expression.ilike(field.getName(), value.toString(), SqlFunc.LikeMatch.Any));
                        }
                    } else {
                        criterion.add(Expression.eq(field.getName(), value));
                    }
                }
            }
        }
    }

    private <K, T extends IEntity<K>> void buildOrder(QueryCriteria<T> queryCriteria, Query<T> query) {
        if (Utility.isNotEmpty(queryCriteria.getOrderName())) {
            if (Utility.isNotEmpty(queryCriteria.getOrderList())) {
                List<OrderByBean> beanList = new ArrayList<>(Arrays.asList(queryCriteria.getOrderList()));
                for (int i = 1; i < beanList.size(); i++) {
                    if (beanList.get(i).getOrderName().equals(queryCriteria.getOrderName())) {
                        for (int j = beanList.size() - 1; j >= i; j--) {
                            beanList.remove(j);
                        }
                    }
                }
                if (beanList.get(0).getOrderName().equals(queryCriteria.getOrderName())) {
                    if (queryCriteria.getOrderBy() != null) {
                        beanList.get(0).setOrderBy(queryCriteria.getOrderBy());
                    } else {
                        beanList.remove(0);
                    }
                } else {
                    OrderByBean entryBean = OrderByBean.of(queryCriteria.getOrderName(),
                            Utility.defaultIfNull(queryCriteria.getOrderBy(), Boolean.TRUE));
                    beanList.add(0, entryBean);
                }
                queryCriteria.setOrderList(beanList.toArray(new OrderByBean[0]));
            } else {
                OrderByBean orderByBean = OrderByBean.of(queryCriteria.getOrderName(),
                        Utility.defaultIfNull(queryCriteria.getOrderBy(), Boolean.TRUE));
                queryCriteria.setOrderList(new OrderByBean[]{orderByBean});
            }
        } else if (Utility.isEmpty(queryCriteria.getOrderList())) {
            OrderByBean orderByBean = OrderByBean.of(IEntity.Col_id, Boolean.TRUE);
            queryCriteria.setOrderList(new OrderByBean[]{orderByBean});
        }
        query.getOrderBy().add(queryCriteria.getOrderList());
    }

    public <K, T extends IEntity<K>> void buildPage(QueryCriteria<T> queryCriteria, Query<T> query, int recordCount) {
        if (Utility.isZero(queryCriteria.getMaxResults())) {
            queryCriteria.setPageNum(Utility.defaultIfNull(queryCriteria.getPageNum(), 1));
            if (recordCount == 0) {
                queryCriteria.setRecordCount(0);
                queryCriteria.setPageCount(0);
                queryCriteria.setPageNum(1);
            } else {
                queryCriteria.setRecordCount(recordCount);
                queryCriteria.setPageCount(recordCount / queryCriteria.getPageSize() + (recordCount % queryCriteria.getPageSize() == 0 ? 0 : 1));
                queryCriteria.setPageCount(Math.max(queryCriteria.getPageCount(), 1));
                queryCriteria.setPageNum(Math.max(1, Math.min(queryCriteria.getPageNum(), queryCriteria.getPageCount())));
            }
            queryCriteria.setFirstResult((queryCriteria.getPageNum() - 1) * queryCriteria.getPageSize());
            if (queryCriteria.getLastPageFull()) {
                queryCriteria.setFirstResult(Math.max(0, Math.min(queryCriteria.getFirstResult(), recordCount - queryCriteria.getPageSize())));
            }
            queryCriteria.setMaxResults(queryCriteria.getPageSize());
        }
        query.getLimit().setLimit(Utility.toZero(queryCriteria.getFirstResult()), queryCriteria.getMaxResults());
    }

    @Override
    public <T extends IEntity, B extends QueryCriteria<T>> int count(B queryCriteria) {
        return buildQuery(queryCriteria)
                .getSelect().addFunc(SqlFunc.Aggregate.Count, IEntity.Col_id).getQuery().queryInt();
    }

    @Override
    public <T extends IEntity, B extends QueryCriteria<T>> B query(B queryCriteria) {
        int recordCount = count(queryCriteria);
        if (recordCount == 0) {
            queryCriteria.setPageNum(1);
            queryCriteria.setPageCount(0);
            queryCriteria.setRecordCount(0);
            queryCriteria.setFirstResult(0);
            queryCriteria.setMaxResults(0);
            queryCriteria.setEntityList(Collections.emptyList());
        } else {
            Query<T> query = buildQuery(queryCriteria)
                    .getSelect().addEntity().addName(queryCriteria.getQueryJoinColumnList()).getQuery();
            buildOrder(queryCriteria, query);
            buildPage(queryCriteria, query, recordCount);
            queryCriteria.setEntityList(query.queryBeanList());
        }
        return queryCriteria;
    }

    @Override
    public <K, T extends IEntity<K>> T insert(T entity) {
        StringBuilder sbSql = new StringBuilder();
        List<Object[]> paramsList = entity.isNullId()
                ? daoFactory.getSqlHelper().insert(Collections.singletonList(entity), sbSql)
                : daoFactory.getSqlHelper().insertWithId(Collections.singletonList(entity), sbSql);
        daoFactory.getJdbcHelper().executeInsert(entity, sbSql.toString(), Arrays.asList(paramsList.get(0)));
        daoFactory.getCacheHelper().putCacheEntity(entity);
        return entity;
    }

    @Override
    public <K, T extends IEntity<K>> int insert(List<T> entityList) {
        if (Utility.isEmpty(entityList)) {
            return 0;
        }
        if (entityList.size() == 1) {
            insert(entityList.get(0));
            return 1;
        }
        StringBuilder sbSql = new StringBuilder();
        List<Object[]> paramsList = entityList.get(0).isNullId()
                ? daoFactory.getSqlHelper().insert(entityList, sbSql)
                : daoFactory.getSqlHelper().insertWithId(entityList, sbSql);
        return getDaoFactory().getJdbcHelper().executeBatch(entityList.get(0).storeSqlFile(), sbSql.toString(), paramsList);
    }

    @Override
    public <K, T extends IEntity<K>> boolean update(T entity, String... names) {
        StringBuilder sbSql = new StringBuilder();
        List<Object[]> paramsList = daoFactory.getSqlHelper().update(Collections.singletonList(entity), sbSql, names);
        return daoFactory.getJdbcHelper().executeUpdate(entity.storeSqlFile(), sbSql.toString(), Arrays.asList(paramsList.get(0))) == 1;
    }

    @Override
    public <K, T extends IEntity<K>> int update(List<T> entityList, String... names) {
        if (Utility.isEmpty(entityList)) {
            return 0;
        }
        if (entityList.size() == 1) {
            return update(entityList.get(0), names) ? 1 : 0;
        }
        StringBuilder sbSql = new StringBuilder();
        List<Object[]> paramsList = daoFactory.getSqlHelper().update(entityList, sbSql, names);
        return getDaoFactory().getJdbcHelper().executeBatch(entityList.get(0).storeSqlFile(), sbSql.toString(), paramsList);
    }

    @Override
    public <K, T extends IEntity<K>> T save(T entity) {
        if (entity.getId() == null) {
            return insert(entity);
        } else {
            update(entity);
            return entity;
        }
    }

    @Override
    public <K, T extends IEntity<K>> int save(List<T> entityList) {
        if (Utility.isEmpty(entityList)) {
            return 0;
        }
        if (entityList.size() == 1) {
            save(entityList.get(0));
            return 1;
        }
        return insert(entityList.stream().filter(t -> t.getId() == null).collect(Collectors.toList()))
                + update(entityList.stream().filter(t -> t.getId() != null).collect(Collectors.toList()));
    }

    @Override
    public <K, T extends IEntity<K>> boolean delete(T entity) {
        return delete(Collections.singletonList(entity)) == 1;
    }

    @Override
    public <K, T extends IEntity<K>> int delete(List<T> entityList) {
        if (Utility.isEmpty(entityList)) {
            return 0;
        }
        for (T entity : entityList) {
            entity.setRecordStatus(RecordStatus.deleted);
        }
        return update(entityList, "recordStatus");
    }

    @Override
    public <K, T extends IEntity<K>> boolean delete(Class<T> clazz, K id) {
        return delete(clazz, Collections.singletonList(id)) == 1;
    }

    @Override
    public <K, T extends IEntity<K>> int delete(Class<T> clazz, Collection<K> idList) {
        if (Utility.isEmpty(idList)) {
            return 0;
        }
        List<T> entityList = idList.stream().map(id -> {
            T entity = Utility.newInstance(clazz);
            entity.setId(id);
            return entity;
        }).collect(Collectors.toList());
        return delete(entityList);
    }

    @Override
    public <K, T extends IEntity<K>> boolean delete_destroy(T entity) {
        return delete_destroy(Collections.singletonList(entity)) == 1;
    }

    @Override
    public <K, T extends IEntity<K>> int delete_destroy(List<T> entityList) {
        if (Utility.isEmpty(entityList)) {
            return 0;
        }
        return delete_destroy((Class<T>) entityList.get(0).getClass(), ListUtil.toList(entityList, IEntity::getId));
    }

    @Override
    public <K, T extends IEntity<K>> boolean delete_destroy(Class<T> clazz, K id) {
        return delete_destroy(clazz, Collections.singletonList(id)) == 1;
    }

    @Override
    public <K, T extends IEntity<K>> int delete_destroy(Class<T> clazz, Collection<K> idList) {
        return createDeleteDestroy(clazz)
                .getWhere().addCriterion(Expression.in(IEntity.Col_id, idList))
                .getQuery().execute();
    }

    private void cascade(IEntity parent, IEntity self, IExecutorAA<IEntity, IEntity> preExecutor, IExecutorAA<IEntity, IEntity> postExecutor) {
        if (preExecutor != null) {
            preExecutor.doExecute(parent, self);
        }
        for (Field field : Utility.describeFieldMap(self).values()) {
            if (List.class.isAssignableFrom(field.getType())) {
                Column targetColumn = daoFactory.getEntityHelper().getColumn(self.getClass(), field.getName());
                if (targetColumn != null && Utility.isNotEmpty(targetColumn.name())) {
                    List<?> dataList = (List<?>) Utility.getFieldValue(field, self);
                    if (Utility.isNotEmpty(dataList)) {
                        for (Object child : dataList) {
                            cascade(self, (IEntity) child, preExecutor, postExecutor);
                        }
                    }
                }
            }
        }
        if (postExecutor != null) {
            postExecutor.doExecute(parent, self);
        }
    }

    @Override
    public void cascadeInsert(List<? extends IEntity> entityList) {
        if (Utility.isEmpty(entityList)) {
            return;
        }
        for (IEntity entity : entityList) {
            cascade(null, entity, (parent, self) -> {
                if (parent != null) {
                    for (Field field : Utility.describeFieldMap(self).values()) {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null && column.parent()
                                && parent.getClass().isAssignableFrom(field.getType())
                                && Utility.getFieldValue(field, self) == null) {
                            Utility.setFieldValue(field, self, parent);
                        }
                    }
                }
                insert(self);
            }, null);
        }
    }

    private void update(List<IEntity> dbEntityList, List<IEntity> updEntityList) {
        Map<Object, IEntity> dbEntityMap = ListUtil.map(dbEntityList, (ListUtil.ConvertKey<IEntity, Object>) IEntity::getId);
        for (IEntity updEntity : updEntityList) {
            if (updEntity.isNullId()) {
                cascadeInsert(Collections.singletonList(updEntity));
            } else if (dbEntityMap.containsKey(updEntity.getId())) {
                update(updEntity);
                IEntity dbEntity = dbEntityMap.get(updEntity.getId());
                for (Field field : Utility.describeFieldMap(dbEntity).values()) {
                    if (Collection.class.isAssignableFrom(Utility.getFieldType(field))
                            && IEntity.class.isAssignableFrom(Utility.getCollectionType(dbEntity.getClass(), field))) {
                        List<IEntity> updChildList = (List<IEntity>) Utility.getFieldValue(field, updEntity);
                        if (updChildList != null) {
                            List<IEntity> dbChildList = (List<IEntity>) Utility.getFieldValue(field, dbEntity);
                            update(dbChildList, updChildList);
                        }
                    }
                }
            } else {
                cascadeDelete(updEntity.getClass(), Collections.singletonList(updEntity.getId()));
            }
        }
    }

    @Override
    public void cascadeUpdate(List<? extends IEntity> entityList) {
        if (Utility.isEmpty(entityList)) {
            return;
        }
        for (IEntity entity : entityList) {
            IEntity dbEntity = load(entity);
            if (dbEntity != null) {
                update(Collections.singletonList(dbEntity), Collections.singletonList(entity));
            }
        }
    }

    @Override
    public <K, T extends IEntity<K>> void cascadeDelete(Class<T> clazz, Collection<K> idList) {
        if (Utility.isEmpty(idList)) {
            return;
        }
        for (T entity : map(clazz, idList).values()) {
            cascade(null, entity, null, (parent, self) -> delete_destroy(self));
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        return daoFactory.getJdbcHelper().executeUpdate(true, sql, Arrays.asList(params));
    }

    @Override
    public int batch(String sql, List<Object[]> paramsList) {
        BeanWrapper<Integer> result = new BeanWrapper<>();
        doTransactionTask(() -> result.setValue(daoFactory.getJdbcHelper().executeBatch(true, sql, paramsList)));
        return result.getValue();
    }

    @Override
    public <K, T extends IEntity<K>> void evict(T entity) {
        daoFactory.getCacheHelper().evict(entity);
    }

    @Override
    public <K, T extends IEntity<K>> void evict(List<T> entityList) {
        daoFactory.getCacheHelper().evict(entityList);
    }

    @Override
    public void clearCache() {
        daoFactory.getCacheHelper().clearCache();
    }

    @Override
    public void updateCachedEntitiesIfChanged() {
        daoFactory.getCacheHelper().updateChangedEntities();
    }

    @Override
    public void doTransactionTask(Runnable task) {
        ServiceFactory.getCommonService(this).doTransactionTask(task);
    }
}
