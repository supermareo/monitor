package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.PropertiesUtil;

/**
 * 检测网口拔插状态
 * cat /sys/class/net/eth0/carrier
 * 如果值为1,表示有网线插入
 * 如果值为0,表示没有网线插入
 */
public class EthernetState extends LinuxCommand<String> {

    private static final String CARRIER_FILE = PropertiesUtil.getProperty("path.carrier", "/sys/class/net/eth0/carrier");

    @Override
    String command() {
        return "cat " + CARRIER_FILE;
    }

    @Override
    String process(LinuxExecResult result) {
        if (result.isSuccess()) {
            return result.getResp().trim();
        }
        return null;
    }

//    public static void main(String[] args) {
//        String exec = new EthernetState().exec();
//        System.out.println("->" + exec + "->");
//    }

}
