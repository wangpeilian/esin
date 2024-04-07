package com.esin.base.remote;

import com.esin.base.exception.LogicException;
import com.esin.base.exception.SystemException;
import com.esin.base.utility.FileUtil;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

public class JsonResponseHandler<T> extends BaseResponseHandler<T> {
    private static final StringResponseHandler handler = new StringResponseHandler(FileUtil.UTF8);
    private final Class<T> type;

    public JsonResponseHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public T handleResponse(HttpResponse response) {
        final StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 400) {
            String errorMsg = handler.handleEntity(entity);
            try {
                return RemoteUtil.gson.fromJson(errorMsg, type);
            } catch (JsonSyntaxException e) {
                throw new SystemException(statusLine.getStatusCode() + ": 返回的不是Json数据格式", e);
            }
        } else if (statusLine.getStatusCode() >= 300) {
            String location = response.getFirstHeader("Location").getValue();
            throw new LogicException(statusLine.getStatusCode() + ": 服务端地址已跳转 (" + location + ")");
        } else if (entity != null) {
            return handleEntity(entity);
        } else {
            throw new LogicException(statusLine.getStatusCode() + ": 返回的数据对象为空");
        }
    }

    @Override
    protected T handleEntity(HttpEntity entity) {
        String body = handler.handleEntity(entity);
        return RemoteUtil.gson.fromJson(body, type);
    }
}
