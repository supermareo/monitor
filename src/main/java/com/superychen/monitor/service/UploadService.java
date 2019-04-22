package com.superychen.monitor.service;

import com.superychen.monitor.model.MonitorInfo;
import com.superychen.monitor.model.UploadResp;
import com.superychen.monitor.utils.OkHttpClientUtil;
import com.superychen.monitor.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UploadService {

    private static final String UPLOAD_TOKEN = PropertiesUtil.getProperty("token.upload");
    private static final String UPLOAD_URL = PropertiesUtil.getProperty("url.upload.one");
    private static final String UPLOAD_URL_BATCH = PropertiesUtil.getProperty("url.upload.batch");
    private static final Map<String, String> HEADER = new HashMap<String, String>() {
        {
            put("token", UPLOAD_TOKEN);
        }
    };

    public boolean upload(MonitorInfo monitorInfo) {
        return doUpload(UPLOAD_URL, monitorInfo);
    }

    public boolean upload(List<MonitorInfo> monitorInfos) {
        if (monitorInfos.isEmpty()) {
            return true;
        }
        return doUpload(UPLOAD_URL_BATCH, monitorInfos);
    }

    private boolean doUpload(String url, Object data) {
        UploadResp resp = OkHttpClientUtil.post(url, data, HEADER, UploadResp.class);
        if (resp != null && resp.getCode() == 200) {
            return true;
        }
        log.error("upload fail, url={}, resp={}", url, resp);
        return false;
    }


}
