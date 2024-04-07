package com.esin.jdbc.query;

import com.esin.base.bean.IBean;
import com.esin.base.utility.Utility;
import com.esin.jdbc.query.criterion.ICriterion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectBean implements IBean, ICriterion {

    public static SelectBean of(String... names) {
        SelectBean bean = new SelectBean();
        if (Utility.isNotEmpty(names)) {
            bean.queryJoinColumnList.addAll(Arrays.asList(names));
        }
        return bean;
    }

    private final List<String> queryJoinColumnList = new ArrayList<>();

    public List<String> getQueryJoinColumnList() {
        return queryJoinColumnList;
    }

    public void add(List<String> queryJoinColumnList) {
        this.queryJoinColumnList.addAll(queryJoinColumnList);
    }

    public SelectBean add(String... names) {
        if (Utility.isNotEmpty(names)) {
            queryJoinColumnList.addAll(Arrays.asList(names));
        }
        return this;
    }
}
