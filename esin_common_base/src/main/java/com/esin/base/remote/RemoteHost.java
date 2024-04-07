package com.esin.base.remote;

import com.esin.base.exception.SystemException;
import com.esin.base.executor.IExecutorA;
import com.esin.base.executor.IExecutorAA;
import com.esin.base.utility.FileUtil;
import com.esin.base.utility.ThreadUtil;
import com.esin.base.utility.Utility;
import org.apache.http.Header;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RemoteHost {
    public static final RemoteHost EMPTY = new RemoteHost(Utility.EMPTY);

    private String host;
    private final String charset;
    private final Credentials credentials;
    private List<Header> headerList;
    private CookieStore cookieStore;

    public RemoteHost(String host) {
        this(host, FileUtil.UTF8, null);
    }

    public RemoteHost(String host, String charset, Credentials credentials) {
        this.host = Utility.toEmpty(host).toLowerCase();
        this.credentials = credentials;
        this.charset = charset;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCharset() {
        return charset;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public List<Header> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<Header> headerList) {
        this.headerList = headerList;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public String get(String uri, boolean enableLog) {
        StringResponseHandler handler = new StringResponseHandler(charset, enableLog);
        return RemoteUtil.get(this, uri, handler);
    }

    public String get(String uri) {
        return get(uri, true);
    }

    public void get(String uri, IExecutorA<InputStream> callback) {
        InputStreamResponseHandler handler = new InputStreamResponseHandler(callback);
        RemoteUtil.get(this, uri, handler);
    }

    public void download_big_file(String uri, String filename, int splitCount, IExecutorAA<Long, Long> monitorExecutor) {
        List<Header> headerList = getHeaderList();
        if (headerList == null) {
            headerList = new ArrayList<>();
            setHeaderList(headerList);
        }
        for (int j = headerList.size() - 1; j >= 0; j--) {
            if ("Range".equals(headerList.get(j).getName())) {
                headerList.remove(j);
            }
        }
        headerList.add(new BasicHeader("Range", "bytes=0-1024"));
        GetLengthResponseHandler handler = new GetLengthResponseHandler();
        Long length = RemoteUtil.get(this, uri, handler);
        try {
            RandomAccessFile accessFile = new RandomAccessFile(filename, "rwd");
            accessFile.setLength(length);
            Utility.close(accessFile);
        } catch (IOException e) {
            throw new SystemException("download error." + filename, e);
        }
        AtomicInteger threadCount = new AtomicInteger(0);
        AtomicLong monitorCount = new AtomicLong(0);
        long size = length / splitCount;
        for (int i = 0; i < splitCount; i++) {
            final long begin = i * size;
            final long end = Math.min((i + 1) * size - 1, length - 1);
            for (int j = headerList.size() - 1; j >= 0; j--) {
                if ("Range".equals(headerList.get(j).getName())) {
                    headerList.remove(j);
                }
            }
            headerList.add(new BasicHeader("Range", "bytes=" + begin + "-" + end));
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    RemoteUtil.get(RemoteHost.this, uri, new InputStreamResponseHandler(new IExecutorA<InputStream>() {
                        @Override
                        public void doExecute(InputStream is) {
                            try {
                                RandomAccessFile accessFile = new RandomAccessFile(filename, "rwd");
                                accessFile.seek(begin);
                                byte[] buffer = new byte[1024];
                                int len = 0;
                                while ((len = is.read(buffer)) != -1) {
                                    accessFile.write(buffer, 0, len);
                                    accessFile.getFD().sync();
                                    monitorExecutor.doExecute(length, monitorCount.addAndGet(len));
                                }
                                Utility.close(accessFile);
                                Utility.close(is);
                            } catch (IOException e) {
                                throw new SystemException("download error." + uri, e);
                            } finally {
                                threadCount.incrementAndGet();
                            }
                        }
                    }));
                }
            });
            Utility.sleep(1);
        }
        while (threadCount.get() < splitCount) {
            Utility.sleep(1);
        }
        Utility.sleep(1);
    }

    public void download(String uri, MonitorOutputStream mos) {
        RemoteUtil.download(this, uri, mos);
    }

    public void download(String uri, Map<String, String> formDataMap, MonitorOutputStream mos) {
        DownloadResponseHandler handler = new DownloadResponseHandler(mos);
        RemoteUtil.post(this, uri, formDataMap, null, handler);
        Utility.close(mos);
    }

    public String upload(String uri, Map<String, String> formDataMap, Map<String, File> uploadFileMap) {
        StringResponseHandler handler = new StringResponseHandler(charset);
        return RemoteUtil.post(this, uri, formDataMap, uploadFileMap, handler);
    }

    public String post(String uri, Map<String, String> formDataMap) {
        StringResponseHandler handler = new StringResponseHandler(charset);
        return RemoteUtil.post(this, uri, formDataMap, null, handler);
    }

    public void post(String uri, Map<String, String> formDataMap, IExecutorA<InputStream> callback) {
        InputStreamResponseHandler handler = new InputStreamResponseHandler(callback);
        RemoteUtil.post(this, uri, formDataMap, null, handler);
    }

    public String login(String uri, Map<String, String> formDataMap) {
        LoginResponseHandler handler = new LoginResponseHandler(this);
        return RemoteUtil.post(this, uri, formDataMap, null, handler);
    }

    public <T> T jsonGet(String uri, JsonResponseHandler<T> handler) {
        return RemoteUtil.jsonGet(this, uri, handler);
    }

    public <T> T jsonGet(String uri, Object param, JsonResponseHandler<T> handler) {
        return RemoteUtil.jsonGet(this, uri, param, handler);
    }

    public <T> T jsonPost(String uri, Object param, JsonResponseHandler<T> handler) {
        return RemoteUtil.jsonPost(this, uri, param, handler);
    }

    public <T> T jsonPut(String uri, Object param, JsonResponseHandler<T> handler) {
        return RemoteUtil.jsonPut(this, uri, param, handler);
    }

    public <T> T jsonDelete(String uri, JsonResponseHandler<T> handler) {
        return RemoteUtil.jsonDelete(this, uri, handler);
    }
}
