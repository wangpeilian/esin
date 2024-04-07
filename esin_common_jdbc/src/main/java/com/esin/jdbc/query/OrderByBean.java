package com.esin.jdbc.query;

import com.esin.base.bean.IBean;
import com.esin.base.utility.Utility;
import com.esin.jdbc.query.criterion.ICriterion;

import java.util.LinkedHashMap;

public class OrderByBean implements IBean, ICriterion {

    public static OrderByBean asc(String orderName) {
        return of(orderName, Boolean.TRUE);
    }

    public static OrderByBean desc(String orderName) {
        return of(orderName, Boolean.FALSE);
    }

    public static OrderByBean of(String orderName, Boolean orderBy) {
        OrderByBean bean = new OrderByBean();
        bean.setOrderName(orderName);
        bean.setOrderBy(orderBy);
        return bean;
    }

    private String orderName;
    private Boolean orderBy;
    private LinkedHashMap<String, Boolean> orderMap = null;

    public OrderByBean addAsc(String orderName) {
        return add(orderName, Boolean.TRUE);
    }

    public OrderByBean addDesc(String orderName) {
        return add(orderName, Boolean.FALSE);
    }

    public OrderByBean add(String orderName, Boolean orderBy) {
        getOrderMap();
        this.setOrderName(orderName);
        this.setOrderBy(orderBy);
        return this;
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

    public LinkedHashMap<String, Boolean> getOrderMap() {
        if (orderMap == null) {
            orderMap = new LinkedHashMap<>();
        }
        if (Utility.isNotEmpty(orderName) && !orderMap.containsKey(orderName)) {
            orderMap.put(orderName, orderBy);
        }
        return orderMap;
    }
}
