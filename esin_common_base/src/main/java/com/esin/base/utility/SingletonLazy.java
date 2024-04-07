package com.esin.base.utility;

import com.esin.base.executor.IExecutorR;

public final class SingletonLazy<T> {
    private final IExecutorR<T> executor;
    private T value = null;

    public SingletonLazy(IExecutorR<T> executor) {
        this.executor = executor;
    }

    public final T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = executor.doExecute();
                }
            }
        }
        return value;
    }
}
