package com.esin.base.utility;

public abstract class ThreadLocalUtil<T> extends ThreadLocalBaseUtil<T> {
    protected ThreadLocalUtil() {
        super(null);
    }

    protected final T newInstance(Object target) {
        return newInstance();
    }

    protected abstract T newInstance();
}
