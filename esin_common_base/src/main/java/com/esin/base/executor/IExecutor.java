package com.esin.base.executor;

public abstract class IExecutor implements Runnable {

    @Override
    public final void run() {
        doExecute();
    }

    protected abstract void doExecute();

}
