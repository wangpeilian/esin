package com.esin.base.remote;

import com.esin.base.utility.Utility;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class LoginResponseHandler extends StringResponseHandler {
    private final RemoteHost remoteHost;

    public LoginResponseHandler(RemoteHost remoteHost) {
        super(remoteHost.getCharset());
        this.remoteHost = remoteHost;
    }

    @Override
    public String handleResponse(HttpResponse response) throws IOException {
        HttpHost httpHost = RemoteUtil.buildHost(remoteHost.getHost());
        Header[] headers = response.getHeaders("Set-Cookie");
        if (Utility.isNotEmpty(headers)) {
            List<Header> headerList = new ArrayList<>();
            String cookieHeader = "";
            CookieStore cookieStore = new BasicCookieStore();
            for (Header header : headers) {
                String[] values = header.getValue().split(";", 2)[0].split("=", 2);
                cookieHeader += "; " + values[0] + "=" + values[1];
                BasicClientCookie cookie = new BasicClientCookie(values[0], values[1]);
                cookie.setPath("/");
                cookie.setExpiryDate(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
                cookie.setVersion(0);
                if (httpHost != null) {
                    cookie.setDomain(httpHost.getHostName());
                }
                cookie.setSecure(false);
                cookieStore.addCookie(cookie);
            }
            remoteHost.setCookieStore(cookieStore);
            headerList.add(new BasicHeader("Cookie", cookieHeader.substring(2)));
            if (Utility.isNotEmpty(headerList)) {
                if (httpHost != null) {
                    headerList.add(new BasicHeader("Host", httpHost.getHostName()));
                }
                remoteHost.setHeaderList(headerList);
            }
        }
        return super.handleResponse(response);
    }
}
