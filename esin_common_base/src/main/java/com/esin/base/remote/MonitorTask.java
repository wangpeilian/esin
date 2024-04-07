package com.esin.base.remote;

import com.esin.base.executor.IExecutorA;
import com.esin.base.utility.Utility;

public class MonitorTask {
    private final String title;
    private long length = 0L;
    private final IExecutorA<String> executor;
    private long bCount = 0L;
    private long kbCount = 0L;
    private long time = 0L;

    public MonitorTask(String title, IExecutorA<String> executor) {
        this.title = title;
        this.executor = executor;
    }

    public long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = Math.max(Utility.toZero(length), 0L);
    }

    public long getCount() {
        return bCount;
    }

    public void addCount() {
        if (time == 0L) {
            time = System.nanoTime();
        }
        bCount++;
        if (kbCount != bCount / 1024) {
            kbCount = bCount / 1024;
            String message = title + bCount / 1024 + "K";
            if (length != 0L) {
                message += "/" + (length / 1024) + "K - " + bCount * 100 / length + "%";
            }
            executor.doExecute(message);
        }
//        if (bCount == length) {
//            time = (System.nanoTime() - time) / 1000000L;
//            System.out.println("Download speed : " + bCount * 1000 / (time * 1024) + " KB/S");
//        }
    }
}
