package com.esin.base.utility;

public abstract class BeanProvider<T> {
    private final boolean checkNullFlg;
    private final T defaultValue;
    protected BeanWrapper<T> value = null;

    public BeanProvider() {
        this(true, null);
    }

    public BeanProvider(boolean checkNullFlg, T defaultValue) {
        this.checkNullFlg = checkNullFlg;
        this.defaultValue = defaultValue;
    }

    public T getValue() {
        for (int i = 0; i < 3; i++) {
            T actualValue = getActualValue();
            if (actualValue == null) {
                actualValue = defaultValue;
            }
            if (isInitValue()) {
                return checkNullValue(actualValue);
            }
            Utility.sleep(1);
        }
        return checkNullValue(defaultValue);
    }

    private T checkNullValue(T value) {
        if (checkNullFlg) {
            AssertUtil.check(value != null, "BeanProvider.checkNullValue:" + getClass().getName());
        }
        return value;
    }

    private T getActualValue() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = new BeanWrapper<>(initValue());
                }
            }
        }
        return value == null ? null : value.getValue();
    }

    public void reload() {
        value = null;
    }

    public boolean isInitValue() {
        return value != null;
    }

    public void setValue(T value) {
        this.value = new BeanWrapper<T>(value);
    }

    protected abstract T initValue();
}
