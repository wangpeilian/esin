package com.esin.base.utility;

import com.esin.base.executor.IExecutorA;
import com.esin.base.executor.IExecutorAR;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtil {

    private static final Logger logger = Logger.getLogger(ThreadUtil.class);

    private static final java.util.concurrent.ThreadPoolExecutor ThreadPoolExecutor = new java.util.concurrent.ThreadPoolExecutor(
            10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static void execute(Runnable runnable) {
        ThreadPoolExecutor.execute(runnable);
    }

    public static void doThreadTask(int taskSize, IExecutorAR<Integer, String> executor) {
        doThreadTask(10, taskSize, executor, null);
    }

    public static void doThreadTask(int threadSize, int taskSize, IExecutorAR<Integer, String> executor, IExecutorA<Throwable> error) {
        long startTime = System.nanoTime();
        AtomicInteger threadCount = new AtomicInteger(0);
        AtomicInteger taskCount = new AtomicInteger(0);
        for (int i = 0; i < threadSize; i++) {
            threadCount.incrementAndGet();
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            int taskIndex = taskCount.getAndIncrement();
                            if (taskIndex >= taskSize) {
                                break;
                            }
                            try {
                                String message = executor.doExecute(taskIndex);
                                long seconds = (System.nanoTime() - startTime) / 1000000000L;
                                String costTime = String.valueOf(100 + seconds / 3600).substring(1)
                                        + ":" + String.valueOf(100 + seconds / 60 % 60).substring(1)
                                        + ":" + String.valueOf(100 + seconds % 60).substring(1);
                                logger.info("run task " + (taskIndex + 1) + "/" + taskSize + " : " + message
                                        + " : " + costTime + " : " + Thread.currentThread().getName());
                            } catch (Throwable t) {
                                logger.error("doThreadTask error.", t);
                                if (error != null) {
                                    error.doExecute(t);
                                }
                            }
                        }
                    } finally {
                        threadCount.decrementAndGet();
                    }
                }
            });
        }
        while (threadCount.get() != 0) {
            Utility.sleep(1);
        }
    }

}
