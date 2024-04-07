package com.esin.base.remote;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.IOException;

public final class DownloadResponseHandler extends BaseResponseHandler<Boolean> {

    private final MonitorOutputStream mos;

    public DownloadResponseHandler(MonitorOutputStream mos) {
        this.mos = mos;
    }

    @Override
    public Boolean handleResponse(HttpResponse response) throws IOException {
        if (mos.getMonitorTask().getLength() <= 0) {
            Header header = response.getFirstHeader("Content-Range");
            if (header == null) {
                header = response.getFirstHeader("Content-Length");
            }
            if (header != null) {
                String contentRange = header.getValue();
                long fileLength = Long.valueOf(contentRange.substring(contentRange.lastIndexOf("/") + 1));
                mos.getMonitorTask().setLength(fileLength);
            }
        }
        return super.handleResponse(response);
    }

    @Override
    protected Boolean handleEntity(HttpEntity entity) throws IOException {
        entity.writeTo(mos);
        if (mos.getMonitorTask().getCount() < mos.getMonitorTask().getLength()) {
            mos.flush();
        } else {
            mos.close();
        }
        return Boolean.TRUE;
    }
}
