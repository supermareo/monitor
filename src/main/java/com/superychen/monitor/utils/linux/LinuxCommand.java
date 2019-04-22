package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public abstract class LinuxCommand<T> {

    private static final String SYSTEM_CHARSET = System.getProperty("file.encoding");
    private static final Runtime RUNTIME = Runtime.getRuntime();

    public T exec() {
        String command = command();
        LinuxExecResult exec = exec(command);
        return process(exec);
    }

    //生成要执行的命令
    abstract String command();

    //处理执行结果,反馈最终结果
    abstract T process(LinuxExecResult result);

    /**
     * 执行linux命令并返回结果
     *
     * @param cmd 命令
     * @return 命令执行结果
     */
    public static LinuxExecResult exec(String cmd) {
        log.debug("linux exec '{}' start", cmd);
        try {
            Process process = RUNTIME.exec(cmd);
            try (InputStream is = process.getInputStream(); InputStreamReader isr = new InputStreamReader(is, SYSTEM_CHARSET);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    log.debug("> {}", line);
                    sb.append(line).append("\n");
                }
                String resp = sb.toString();
                log.debug("linux exec '{}' success, resp={}", cmd, resp);
                return LinuxExecResult.success(resp);
            } catch (Exception e) {
                log.error("linux exec '{}' error, e=", cmd, e);
                return LinuxExecResult.fail(e.getMessage());
            }
        } catch (IOException e) {
            log.error("linux exec '{}' error, e=", cmd, e);
            return LinuxExecResult.fail(e.getMessage());
        }
    }

}
