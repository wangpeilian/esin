package com.esin.base.remote;

import java.io.IOException;
import java.io.InputStream;

public final class MonitorInputStream extends InputStream {
    private final InputStream is;
    private final MonitorTask monitorTask;

    public MonitorInputStream(InputStream is, MonitorTask monitorTask) {
        this.is = is;
        this.monitorTask = monitorTask;
    }

    @Override
    public int read() throws IOException {
        if (monitorTask != null) {
            monitorTask.addCount();
        }
        return is.read();
    }

    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return is.skip(n);
    }

    public int available() throws IOException {
        return is.available();
    }

    public void close() throws IOException {
        is.close();
    }

    public void mark(int readlimit) {
        is.mark(readlimit);
    }

    public void reset() throws IOException {
        is.reset();
    }

    public boolean markSupported() {
        return is.markSupported();
    }
}