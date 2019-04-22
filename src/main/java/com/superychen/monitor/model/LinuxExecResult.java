package com.superychen.monitor.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Linux命令执行结果
 */
@Getter
@Setter
public class LinuxExecResult extends Base {
    private boolean success;
    private String msg;
    private String resp;

    private LinuxExecResult(boolean success, String msg, String resp) {
        this.success = success;
        this.msg = msg;
        this.resp = resp;
    }

    public static LinuxExecResult success(String result) {
        return new LinuxExecResult(true, "success", result);
    }

    public static LinuxExecResult fail(String msg) {
        return new LinuxExecResult(false, msg, null);
    }

}
