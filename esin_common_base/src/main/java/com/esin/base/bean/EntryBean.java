package com.esin.base.bean;

import java.util.Objects;

public class EntryBean<T, V> extends BaseBean {
    private T key;
    private V value;

    public EntryBean() {
    }

    public EntryBean(T key, V value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryBean<?, ?> entryBean = (EntryBean<?, ?>) o;
        return Objects.equals(key, entryBean.key) && Objects.equals(value, entryBean.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
