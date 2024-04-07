package com.esin.jdbc.query;

import com.esin.base.bean.BaseBean;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.JdbcConfig;
import com.esin.jdbc.query.criterion.Query;

import java.util.List;

public class QueryCriteria<T extends IEntity> extends BaseBean {

    private T entity;
    private T fuzzyEntity;
    private List<String> fuzzyNameList;
    private String fuzzyText;
    private List<String> queryJoinColumnList;
    private List<T> entityList;

    private Integer pageNum = 1;
    private Integer pageSize = JdbcConfig.query_page_size;
    private Integer pageCount;
    private Integer recordCount;
    private Integer firstResult;
    private Integer maxResults;
    private Boolean lastPageFull = Boolean.FALSE;

    private String orderName;
    private Boolean orderBy;
    private OrderByBean[] orderList;

    public QueryCriteria() {
    }

    public QueryCriteria(T entity) {
        this.entity = entity;
    }

    public Query<T> customize(Query<T> query) {
        return query;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public T getFuzzyEntity() {
        return fuzzyEntity;
    }

    public void setFuzzyEntity(T fuzzyEntity) {
        this.fuzzyEntity = fuzzyEntity;
    }

    public List<String> getFuzzyNameList() {
        return fuzzyNameList;
    }

    public void setFuzzyNameList(List<String> fuzzyNameList) {
        this.fuzzyNameList = fuzzyNameList;
    }

    public String getFuzzyText() {
        return fuzzyText;
    }

    public void setFuzzyText(String fuzzyText) {
        this.fuzzyText = fuzzyText;
    }

    public List<String> getQueryJoinColumnList() {
        return queryJoinColumnList;
    }

    public void setQueryJoinColumnList(List<String> queryJoinColumnList) {
        this.queryJoinColumnList = queryJoinColumnList;
    }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Boolean getLastPageFull() {
        return lastPageFull;
    }

    public void setLastPageFull(Boolean lastPageFull) {
        this.lastPageFull = lastPageFull;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Boolean getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Boolean orderBy) {
        this.orderBy = orderBy;
    }

    public OrderByBean[] getOrderList() {
        return orderList;
    }

    public void setOrderList(OrderByBean[] orderList) {
        this.orderList = orderList;
    }
}
