package com.esin.base.remote;

import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class StringResponseHandler extends BaseResponseHandler<String> {
    private static final Logger logger = Logger.getLogger(StringResponseHandler.class);
    private final String charset;
    private final boolean enableLog;

    public StringResponseHandler(String charset, boolean enableLog) {
        this.charset = charset;
        this.enableLog = enableLog;
    }

    public StringResponseHandler(String charset) {
        this(charset, true);
    }

    @Override
    protected String handleEntity(HttpEntity entity) {
        if (Utility.isNotEmpty(getLocation())) {
            return getLocation();
        }
        try {
            String body = EntityUtils.toString(entity, charset);
            RemoteUtil.outputLog("= Response =", body, false, enableLog);
            return body;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
