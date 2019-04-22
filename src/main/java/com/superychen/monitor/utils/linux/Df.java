package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.Base;
import com.superychen.monitor.model.LinuxExecResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * df命令
 * 查看硬盘使用情况
 */
@Slf4j
public class Df extends LinuxCommand<Df.StorageResult> {

    // /dev/sda1       196G   42G  144G   23% /
    private static final Pattern PATTERN = Pattern.compile("(\\S+)\\s+([0-9A-Z.]+)\\s+([0-9A-Z.]+)\\s+([0-9A-Z.]+)\\s+(\\d+)%\\s+(\\S+)");

    private String path;

    public Df(String path) {
        this.path = path;
    }

    @Override
    String command() {
        return "df -h " + path;
    }

    @Override
    StorageResult process(LinuxExecResult result) {
        if (result.isSuccess()) {
            String resp = result.getResp();
            //正确执行完成,resp如下:
            //文件系统        容量  已用  可用 已用% 挂载点
            ///dev/sda1       196G   42G  144G   23% /
            Matcher matcher = PATTERN.matcher(resp);
            if (matcher.find()) {
                String fileSystem = matcher.group(1);
                String total = matcher.group(2);
                String used = matcher.group(3);
                String available = matcher.group(4);
                int usedPercent = Integer.parseInt(matcher.group(5));
                String mountPath = matcher.group(6);
                return new StorageResult(fileSystem, total, used, available, usedPercent, mountPath);
            }
        }
        return null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageResult extends Base {
        private String fileSystem;
        private String total;
        private String used;
        private String available;
        private int usedPercent;
        private String mountPath;
    }

    public static void main(String[] args) {
        System.out.println(new Df("/").exec());
    }

}
