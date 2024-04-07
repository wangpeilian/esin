package com.esin.jdbc.query;

import com.esin.base.bean.IBean;
import com.esin.jdbc.query.criterion.ICriterion;

public class LimitBean implements IBean, ICriterion {

    public static LimitBean of(int length) {
        return of(0, length);
    }

    public static LimitBean of(int offset, int length) {
        LimitBean bean = new LimitBean();
        bean.setOffset(offset);
        bean.setLength(length);
        return bean;
    }

    private int offset;
    private int length;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
