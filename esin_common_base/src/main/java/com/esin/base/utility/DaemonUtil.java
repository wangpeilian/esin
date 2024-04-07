package com.esin.base.utility;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.List;

public class DaemonUtil {
    public static abstract class Task {
        public final String name;
        public final int interval;
        private int second = 0;

        public Task(String name) {
            this(name, 1);
        }

        public Task(String name, int interval) {
            this.name = name;
            this.interval = Math.max(interval, 1);
        }

        public abstract void doTask(int value);
    }

    private static Boolean status = null;
    private static final List<Task> taskList = new ArrayList<>();
    private static int second = 0;

    public static int getSecond() {
        return second;
    }

    public static void add(Task task) {
        taskList.add(task);
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = i + 1; j < taskList.size(); j++) {
                if (taskList.get(i).name.equals(taskList.get(j).name)) {
                    throw new DuplicateFormatFlagsException(taskList.get(i).name);
                }
            }
        }
        start();
    }

    private static void start() {
        if (status == null) {
            status = true;
            ThreadUtil.execute(() -> {
                final long startTime = System.nanoTime();
                while (status) {
                    for (int i = 0; status && i < taskList.size(); i++) {
                        Task task = taskList.get(i);
                        final long time = System.nanoTime();
                        second = (int) ((time - startTime) / 1000000000L);
                        if (second - task.second >= task.interval) {
                            task.second = second;
                            try {
                                task.doTask(second);
                            } catch (Throwable t) {
                                Logger.getLogger(DaemonUtil.class).error("Daemon task running error.", t);
                            } finally {
                                long millisSecond = (System.nanoTime() - time) / 1000000L;
                                if (millisSecond >= 3000L) {
                                    Logger.getLogger(DaemonUtil.class).warn("Cost long time task : " + task.name + " -> " + millisSecond + " ms");
                                }
                            }
                        }
                    }
                    Utility.sleep(1);
                }
                status = null;
            });
        }
    }

    public static void stop() {
        if (status != null) {
            status = false;
            for (int i = 0; status != null && i < 60; i++) {
                Utility.sleep(1);
            }
        }
    }
}
