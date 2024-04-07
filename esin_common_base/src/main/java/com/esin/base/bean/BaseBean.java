package com.esin.base.bean;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class BaseBean implements IBean {
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE, new StringBuffer(), BaseBean.class, true, false) {
            @Override
            public ToStringBuilder append(String fieldName, Object obj) {
                if (obj != null) {
                    return super.append(fieldName, convert(fieldName, obj));
                } else {
                    return this;
                }
            }
        }.toString();
    }

    protected Object convert(String fieldName, Object obj) {
        return obj;
    }
}
