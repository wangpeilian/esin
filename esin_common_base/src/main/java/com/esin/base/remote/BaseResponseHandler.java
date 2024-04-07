package com.esin.base.remote;

import com.esin.base.utility.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public abstract class BaseResponseHandler<T> implements ResponseHandler<T> {

    private static boolean throwExceptionIf4XXX = true;

    public static void setCurrentNotThrowExceptionIf4XXX() {
        throwExceptionIf4XXX = false;
    }

    protected final Logger logger = Logger.getLogger(getClass());

    private String location = null;

    public String getLocation() {
        return location;
    }

    @Override
    public T handleResponse(HttpResponse response) throws IOException {
        final StatusLine statusLine = response.getStatusLine();
        String message = statusLine.getStatusCode() + " : " + statusLine.getReasonPhrase();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 400) {
            if (throwExceptionIf4XXX) {
                EntityUtils.consume(entity);
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            } else {
                throwExceptionIf4XXX = true;
            }
        } else if (statusLine.getStatusCode() >= 300) {
            location = response.getFirstHeader("Location").getValue();
            logger.warn(message + " : " + location);
        } else {
            logger.trace(message);
        }
        if (entity == null) {
            return null;
        }
        return handleEntity(entity);
    }

    protected abstract T handleEntity(HttpEntity entity) throws IOException;

}
