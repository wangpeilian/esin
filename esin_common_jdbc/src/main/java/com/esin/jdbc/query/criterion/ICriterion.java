package com.esin.jdbc.query.criterion;

import com.esin.jdbc.query.LimitBean;
import com.esin.jdbc.query.OrderByBean;
import com.esin.jdbc.query.SelectBean;

public interface ICriterion {

    public static SelectBean select(String... names) {
        return SelectBean.of(names);
    }

    public static OrderByBean asc(String orderName) {
        return OrderByBean.asc(orderName);
    }

    public static OrderByBean desc(String orderName) {
        return OrderByBean.desc(orderName);
    }

    public static OrderByBean order(String orderName, Boolean orderBy) {
        return OrderByBean.of(orderName, orderBy);
    }

    public static LimitBean limit(int length) {
        return LimitBean.of(length);
    }

    public static LimitBean limit(int offset, int length) {
        return LimitBean.of(offset, length);
    }

}
