package com.esin.base.remote;

import com.esin.base.exception.SystemException;
import com.esin.base.executor.IExecutorA;
import com.esin.base.utility.FormatUtil;
import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.event.Level;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RemoteUtil {
    private static final Logger logger = Logger.getLogger(RemoteUtil.class);
    private static final List<IExecutorA<String>> executeCallbackList = new ArrayList<>();

    public static final Gson gson = new GsonBuilder().setDateFormat(FormatUtil.DateTimePattern).create();
    private static final int Retry_Count = 3;
    private static final int Retry_Wait = 1;
    private static HttpHost proxyHost = null;
    private static Credentials proxyCredentials = null;

    public static void addExecuteCallback(IExecutorA<String> executor) {
        executeCallbackList.add(executor);
    }

    private static void doExecuteCallback(String text) {
        executeCallbackList.forEach(executor -> executor.doExecute(text));
    }

    private static final HttpClient httpClient = createHttpClient();

    private static HttpClient createHttpClient() {
        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .useProtocol(SSLConnectionSocketFactory.SSL)
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();

            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true) {
                @Override
                protected void prepareSocket(SSLSocket socket) throws IOException {
                    super.prepareSocket(socket);
                    socket.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(sfr);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setMaxTotal(200);
        connectionManager.setValidateAfterInactivity(1000);

        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
                .build();

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .build();
    }

    public static String buildUrl(RemoteHost remoteHost, String uri) {
        if (uri.contains("://")) {
            return uri;
        }
        return remoteHost.getHost() + uri;
    }

    public static HttpHost buildHost(String url) {
        if (Utility.isEmpty(url)) {
            return null;
        }
        String schema = url.substring(0, url.indexOf(":"));
        String hostname = url.substring(url.indexOf(":") + 3);
        if (hostname.contains("/")) {
            hostname = hostname.substring(0, hostname.indexOf("/"));
        }
        int port = -1;
        if (hostname.contains(":")) {
            port = Integer.parseInt(hostname.substring(hostname.indexOf(":") + 1));
            hostname = hostname.substring(0, hostname.indexOf(":"));
//        } else if ("http".equals(schema)) {
//            port = 80;
//        } else if ("https".equals(schema)) {
//            port = 443;
        }
        return new HttpHost(hostname, port, schema);
    }

    private static <T> T executeRequest(Request request, RemoteHost host, ResponseHandler<T> handler) {
        return executeRequest(request, host, handler, Retry_Count);
    }

    private static <T> T executeRequest(Request request, RemoteHost host, ResponseHandler<T> handler, int retryCount) {
        doExecuteCallback(host.getHost() + request.toString());
        Set<String> headerNameSet = new HashSet<>();
        if (Utility.isNotEmpty(host.getHeaderList())) {
            for (Header header : host.getHeaderList()) {
                request.addHeader(header);
                headerNameSet.add(header.getName().toLowerCase());
            }
        }
        if (!headerNameSet.contains("User-Agent".toLowerCase())) {
            request.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0"));
        }

        for (int i = 0; i < retryCount; i++) {
            try {
                Executor executor = Executor.newInstance(httpClient);
                if (proxyHost != null) {
                    request.viaProxy(proxyHost);
                    if (proxyCredentials != null) {
                        executor.auth(proxyHost, proxyCredentials);
                    }
                }
                if (host.getCredentials() != null) {
                    executor.auth(buildHost(host.getHost()), host.getCredentials());
                }
                if (host.getCookieStore() != null) {
                    executor.cookieStore(host.getCookieStore());
                }
                return executor.execute(request).handleResponse(handler);
            } catch (IOException e) {
                logger.error(request.toString(), e);
                if (i != retryCount - 1) {
                    Utility.sleep(Retry_Wait);
                }
            }
        }
        return null;
    }

    public static void setProxyHost(String scheme, String host, String port) {
        if (Utility.isNotEmpty(host)) {
            proxyHost = new HttpHost(host, Utility.isEmpty(port) ? -1 : Integer.parseInt(port), Utility.toNull(scheme));
        } else {
            proxyHost = null;
        }
    }

    public static void setProxyCredentials(String username, String password, String workstation, String domain) {
        if (Utility.isNotEmpty(username) && Utility.isNotEmpty(password)) {
            if (Utility.isNotEmpty(domain)) {
                proxyCredentials = new NTCredentials(username, password, Utility.toNull(workstation), domain);
            } else {
                proxyCredentials = new UsernamePasswordCredentials(username, password);
            }
        } else {
            proxyCredentials = null;
        }
    }

    public static void download(RemoteHost host, String uri, MonitorOutputStream mos) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        if (host.getHeaderList() == null) {
            host.setHeaderList(new ArrayList<>());
        }
        for (int i = 0; i < 10; i++) {
            long readCount = mos.getMonitorTask().getCount();
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < host.getHeaderList().size(); k++) {
                    if ("Range".equals(host.getHeaderList().get(k).getName())) {
                        host.getHeaderList().remove(k);
                        break;
                    }
                }
                host.getHeaderList().add(new BasicHeader("Range", "bytes=" + mos.getMonitorTask().getCount() + "-"));
                try {
                    Request request = Request.Get(url);
                    request.connectTimeout(5000);
                    request.socketTimeout(5000);
                    Boolean result = executeRequest(request, host, new DownloadResponseHandler(mos), 1);
                    if (!Boolean.TRUE.equals(result)) {
                        Logger.getLogger(RemoteUtil.class).info((i * 10 + j + 1) + " : " + uri);
                    }
                } catch (Exception e) {
                    Logger.getLogger(RemoteUtil.class).error((i * 10 + j + 1) + " : " + uri, e);
                }
                if (mos.getMonitorTask().getLength() <= 0
                        || mos.getMonitorTask().getCount() >= mos.getMonitorTask().getLength()) {
                    break;
                }
                Utility.sleep(1);
            }
            if (readCount == mos.getMonitorTask().getCount()
                    || mos.getMonitorTask().getCount() >= mos.getMonitorTask().getLength()) {
                break;
            }
        }
        Utility.close(mos);
    }

    public static <T> T get(String url, ResponseHandler<T> handler) {
        return get(RemoteHost.EMPTY, url, handler);
    }

    public static <T> T get(RemoteHost host, String uri, ResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        return executeRequest(Request.Get(url), host, handler);
    }

    public static String post(RemoteHost host, String uri, Map<String, String> formDataMap) {
        return post(host, uri, formDataMap, null, null, null, new StringResponseHandler(host.getCharset()));
    }

    public static <T> T post(RemoteHost host, String uri, Map<String, String> formDataMap, Map<String, File> uploadFileMap, ResponseHandler<T> handler) {
        return post(host, uri, formDataMap, uploadFileMap, null, null, handler);
    }

    public static <T> T post(RemoteHost host, String uri, Map<String, Object> requestMap, Class<T> responseType) {
        return post(host, uri, null, null, RemoteUtil.gson.toJson(requestMap),
                ContentType.APPLICATION_JSON, new JsonResponseHandler<>(responseType));
    }

    public static <T> T post(RemoteHost host, String uri, String requestBody, ContentType contentType, ResponseHandler<T> handler) {
        return post(host, uri, null, null, requestBody, contentType, handler);
    }

    private static <T> T post(RemoteHost host, String uri,
                              Map<String, String> formDataMap, Map<String, File> uploadFileMap,
                              String requestBody, ContentType contentType,
                              ResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        if (Utility.isNotEmpty(formDataMap)) {
            RemoteUtil.outputLog("= Request ==", formDataMap.toString(), false);
        }
        if (Utility.isNotEmpty(uploadFileMap)) {
            RemoteUtil.outputLog("=== File ===", uploadFileMap.keySet().toString(), false);
        }
        if (Utility.isNotEmpty(requestBody)) {
            RemoteUtil.outputLog("= Request ==", requestBody, false);
        }
        Request request = Request.Post(url);
        if (Utility.isNotEmpty(uploadFileMap)) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName(host.getCharset()));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            contentType = ContentType.MULTIPART_FORM_DATA.withCharset(host.getCharset());
            for (Map.Entry<String, File> entry : uploadFileMap.entrySet()) {
                if (Utility.isNotEmpty(entry.getValue())) {
                    builder.addBinaryBody(entry.getKey(), entry.getValue(), contentType, entry.getValue().getName());
                }
            }
            if (Utility.isNotEmpty(formDataMap)) {
                contentType = ContentType.create(URLEncodedUtils.CONTENT_TYPE, host.getCharset());
                for (Map.Entry<String, String> entry : formDataMap.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue(), contentType);
                }
            }
            request.body(builder.build());
        } else if (Utility.isNotEmpty(formDataMap)) {
            Form form = Form.form();
            for (Map.Entry<String, String> entry : formDataMap.entrySet()) {
                form.add(entry.getKey(), entry.getValue());
            }
            if (Utility.isNotEmpty(host.getCharset())) {
                request.bodyForm(form.build(), Charset.forName(host.getCharset()));
            } else {
                request.bodyForm(form.build());
            }
        } else if (Utility.isNotEmpty(requestBody)) {
            request.bodyString(requestBody, contentType);
        }
        return executeRequest(request, host, handler);
    }

    private static String executeBodyRequest(Request request, RemoteHost host, String charset) {
        return executeRequest(request, host, new StringResponseHandler(charset));
    }

    private static <T> T executeJsonRequest(Request request, RemoteHost host, JsonResponseHandler<T> handler) {
        doExecuteCallback(host.getHost() + request.toString());
        if (Utility.isNotEmpty(host.getHeaderList())) {
            for (Header header : host.getHeaderList()) {
                request.addHeader(header);
            }
        }
        Executor executor = Executor.newInstance(httpClient);
        if (proxyHost != null) {
            request.viaProxy(proxyHost);
            if (proxyCredentials != null) {
                executor.auth(proxyHost, proxyCredentials);
            }
        }
        if (host.getCredentials() != null) {
            executor.auth(buildHost(host.getHost()), host.getCredentials());
        }
        if (host.getCookieStore() != null) {
            executor.cookieStore(host.getCookieStore());
        }
        try {
            return executor.execute(request).handleResponse(handler);
        } catch (IOException e) {
            throw new SystemException(host.getHost() + " -> " + request.toString(), e);
        }
    }

    public static <T> T jsonGet(RemoteHost host, String uri, JsonResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        Request request = Request.Get(url);
        return executeJsonRequest(request, host, handler);
    }

    public static <T> T jsonGet(RemoteHost host, String uri, Object param, JsonResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        logger.trace(url);
        url += "?" + Utility.map2url(Utility.object2map(param));
        RemoteUtil.outputLog("=== URL ====", url, true);
        Request request = Request.Get(url);
        return executeJsonRequest(request, host, handler);
    }

    public static <T> T jsonPost(RemoteHost host, String uri, Object param, JsonResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        logger.trace(url);
        Request request = Request.Post(url);
        String jsonBody = gson.toJson(param);
        RemoteUtil.outputLog("=== URL ====", url, true);
        RemoteUtil.outputLog("= Request ==", jsonBody, false);
        request.bodyString(jsonBody, ContentType.APPLICATION_JSON);
        return executeJsonRequest(request, host, handler);
    }

    public static String bodyPost(RemoteHost host, String uri, String body, String charset) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        RemoteUtil.outputLog("= Request ==", body, false);
        Request request = Request.Post(url);
        request.bodyString(body, ContentType.APPLICATION_XML.withCharset(charset));
        return executeBodyRequest(request, host, charset);
    }

    public static <T> T jsonPut(RemoteHost host, String uri, Object param, JsonResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        Request request = Request.Put(url);
        String jsonBody = gson.toJson(param);
        RemoteUtil.outputLog("=== URL ====", url, true);
        RemoteUtil.outputLog("= Request ==", jsonBody, false);
        request.bodyString(jsonBody, ContentType.APPLICATION_JSON);
        return executeJsonRequest(request, host, handler);
    }

    public static <T> T jsonDelete(RemoteHost host, String uri, JsonResponseHandler<T> handler) {
        String url = buildUrl(host, uri);
        RemoteUtil.outputLog("=== URL ====", url, true);
        Request request = Request.Delete(url);
        return executeJsonRequest(request, host, handler);
    }

    private static long global_count = 10000L;
    private static final ThreadLocal<Long> local_count = new ThreadLocal<>();

    public static void outputLog(String type, String content, boolean init) {
        outputLog(type, content, init, true);
    }

    public static void outputLog(String type, String content, boolean init, boolean enableLog) {
        if (Logger.checkLevel(Level.TRACE)) {
            if (init || local_count.get() == null) {
                global_count++;
                if (global_count >= 100000) {
                    global_count -= 90000;
                }
                local_count.set(global_count);
            }
            if (enableLog) {
                logger.trace(type + " " + String.valueOf(local_count.get()).substring(1) + " : " + content);
            } else {
                System.out.println(type + " " + String.valueOf(local_count.get()).substring(1) + " : " + content);
            }
        }
    }
}
