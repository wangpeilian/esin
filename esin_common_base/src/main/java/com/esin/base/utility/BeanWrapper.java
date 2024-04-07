package com.esin.base.utility;

public class BeanWrapper<T> {
    private T value;

    public BeanWrapper() {
    }

    public BeanWrapper(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
