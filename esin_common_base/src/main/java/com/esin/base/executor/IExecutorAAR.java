package com.esin.base.executor;

public interface IExecutorAAR<A1, A2, R> {
    R doExecute(A1 arg1, A2 arg2);
}
