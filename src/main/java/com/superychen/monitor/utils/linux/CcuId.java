package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取主机id
 */
@Slf4j
public class CcuId extends LinuxCommand<String> {

    private static final String CCU_ID_FILE = PropertiesUtil.getProperty("path.ccu_id", "/etc/hj_ccuid");

    @Override
    String command() {
        return "cat " + CCU_ID_FILE;
    }

    @Override
    String process(LinuxExecResult result) {
        if (result.isSuccess()) {
            return result.getResp().trim();
        }
        return null;
    }

}
