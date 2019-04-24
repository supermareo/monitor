package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.PropertiesUtil;

import java.util.Arrays;

/**
 * 获取CPU信息
 * 目前支持获取CPU核心数,如果获取失败,返回-1
 */
public class CpuInfo extends LinuxCommand<Integer> {

    @Override
    String command() {
        return PropertiesUtil.getProperty("CMD_CPU_INFO", "cat /proc/cpuinfo");
    }

    @Override
    Integer process(LinuxExecResult result) {
        if (result.isSuccess()) {
            String resp = result.getResp();
            return Math.toIntExact(Arrays.stream(resp.split("\n")).filter(s -> s.startsWith("processor")).count());
        }
        return -1;
    }

//    public static void main(String[] args) {
//        System.out.println(new CpuInfo().exec());
//    }

}
