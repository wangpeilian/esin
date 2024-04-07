package com.esin.jdbc.query.criterion;

import com.esin.base.utility.ListUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.query.ResultConvert;
import com.esin.jdbc.query.ResultConvertFactory;

import java.util.ArrayList;
import java.util.List;

public class Query<T extends IEntity> implements IStatement {

    private final IDao dao;

    private final Select<T> select;

    private final From<T> from;

    private final Where<T> where;

    private String alias;

    private GroupBy<T> groupBy;

    private Having<T> having;

    private OrderBy<T> orderBy;

    private Limit<T> limit;

    public Query(IDao dao, Class<T> clazz) {
        this.dao = dao;
        this.select = new Select<>(this);
        this.from = new From<>(this, clazz);
        this.where = new Where<>(this);
    }

    public SubQuery<T> asSubQuery(SqlFunc.SubQueryMethod method) {
        return SubQuery.create(method, this);
    }

    public Query<T> setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getAlias() {
        if (Utility.isEmpty(alias)) {
            alias = "t";
        }
        return alias;
    }

    public List<T> queryBeanList() {
        return queryList(ResultConvertFactory.Bean(this));
    }

    public List<T> queryLazyEntityList() {
        return queryList(ResultConvertFactory.LazyEntity(getFrom().getClazz()));
    }

    public Integer queryInt() {
        return ListUtil.first(queryList(ResultConvertFactory.IntegerUnique));
    }

    public <R> List<R> queryList(ResultConvert<R> resultConvert) {
        List<Object> paramList = new ArrayList<>();
        String sql = getSql(paramList);
        return dao.getDaoFactory().getJdbcHelper().executeQuery(resultConvert, sql, paramList);
    }

    public int execute() {
        List<Object> paramList = new ArrayList<>();
        String sql = getSql(paramList);
        boolean storeSqlFile = Utility.newInstance(getFrom().getClazz()).storeSqlFile();
        return getDao().getDaoFactory().getJdbcHelper().executeUpdate(storeSqlFile, sql, paramList);
    }

    public IDao getDao() {
        return dao;
    }

    public Select<T> getSelect() {
        return select;
    }

    public From<T> getFrom() {
        return from;
    }

    public Where<T> getWhere() {
        return where;
    }

    public GroupBy<T> getGroupBy() {
        if (this.groupBy == null) {
            this.groupBy = new GroupBy<>(this);
        }
        return groupBy;
    }

    public Having<T> getHaving() {
        if (this.having == null) {
            this.having = new Having<>(this);
        }
        return having;
    }

    public OrderBy<T> getOrderBy() {
        if (this.orderBy == null) {
            this.orderBy = new OrderBy<>(this);
        }
        return orderBy;
    }

    public Limit<T> getLimit() {
        if (this.limit == null) {
            this.limit = new Limit<>(this);
        }
        return limit;
    }

    public String getSql(List<Object> paramList) {
        StringBuilder sb = new StringBuilder();
        if (where != null) {
            sb.append(where.getSql(paramList));
        }
        if (groupBy != null) {
            sb.append(groupBy.getSql());
            if (having != null) {
                sb.append(having.getSql(paramList));
            }
        }
        if (orderBy != null) {
            sb.append(orderBy.getSql());
        }
        if (limit != null) {
            sb.append(limit.getSql(paramList));
        }
        return select.getSql() + from.getSql() + sb;
    }
}
