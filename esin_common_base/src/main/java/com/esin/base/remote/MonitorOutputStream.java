package com.esin.base.remote;

import java.io.IOException;
import java.io.OutputStream;

public final class MonitorOutputStream extends OutputStream {
    private final OutputStream os;
    private final MonitorTask monitorTask;

    public MonitorOutputStream(OutputStream os, MonitorTask monitorTask) {
        this.os = os;
        this.monitorTask = monitorTask;
    }

    public MonitorTask getMonitorTask() {
        return monitorTask;
    }

    public void write(int b) throws IOException {
        if (monitorTask != null) {
            monitorTask.addCount();
        }
        os.write(b);
    }

    public void write(byte[] b) throws IOException {
        super.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }

    public void flush() throws IOException {
        os.flush();
    }

    public void close() throws IOException {
        os.close();
    }
}
