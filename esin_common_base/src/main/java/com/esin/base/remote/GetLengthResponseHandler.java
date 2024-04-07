package com.esin.base.remote;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;

public final class GetLengthResponseHandler extends BaseResponseHandler<Long> {

    @Override
    public Long handleResponse(HttpResponse response) throws IOException {
        try {
            String contentRange = response.getFirstHeader("Content-Range").getValue();
            return Long.valueOf(contentRange.substring(contentRange.lastIndexOf("/") + 1));
        } catch (Exception e) {
            return super.handleResponse(response);
        }
    }

    @Override
    protected Long handleEntity(HttpEntity entity) throws IOException {
        return entity.getContentLength();
    }
}
