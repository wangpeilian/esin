package com.esin.base.remote;

import com.esin.base.executor.IExecutorA;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;

public final class InputStreamResponseHandler extends BaseResponseHandler<Void> {
    private final IExecutorA<InputStream> executor;

    public InputStreamResponseHandler(IExecutorA<InputStream> executor) {
        this.executor = executor;
    }

    @Override
    protected Void handleEntity(HttpEntity entity) throws IOException {
        InputStream is = entity.getContent();
        if (is != null) {
            executor.doExecute(entity.getContent());
        }
        return null;
    }
}
