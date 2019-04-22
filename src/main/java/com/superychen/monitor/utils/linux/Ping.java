package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.Base;
import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.PropertiesUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ping命令
 * 检测网络连接状态与网络延迟
 */
@Slf4j
public class Ping extends LinuxCommand<Ping.PingResult> {

    private static final Pattern PATTERN = Pattern.compile("rtt min/avg/max/mdev = ([0-9.]+)/([0-9.]+)/([0-9.]+)/([0-9.]+) ms");

    private static final String PING_URL = PropertiesUtil.getProperty("ping.url", "www.baidu.com");
    private static final int PING_TIMES = Integer.parseInt(PropertiesUtil.getProperty("ping.times", "4"));

    private String addr;
    private int times = 4;

    public Ping() {
        this.addr = PING_URL;
        this.times = PING_TIMES;
    }

    public Ping(String addr) {
        this.addr = addr;
    }

    public Ping(String addr, int times) {
        this.addr = addr;
        this.times = times;
    }

    @Override
    String command() {
        return "ping -c " + times + " " + addr;
    }

    @Override
    PingResult process(LinuxExecResult result) {
        if (result.isSuccess()) {
            String resp = result.getResp();
            Matcher matcher = PATTERN.matcher(resp);
            //正确执行完成,resp如下,取 rtt min/avg/max/mdev = 3.205/3.502/3.761/0.221 ms 这行进行数据提取:
            //
            //PING www.a.shifen.com (180.97.33.108) 56(84) bytes of data.
            //64 bytes from 180.97.33.108 (180.97.33.108): icmp_seq=1 ttl=57 time=3.40 ms
            //64 bytes from 180.97.33.108 (180.97.33.108): icmp_seq=2 ttl=57 time=3.76 ms
            //64 bytes from 180.97.33.108 (180.97.33.108): icmp_seq=3 ttl=57 time=3.20 ms
            //64 bytes from 180.97.33.108 (180.97.33.108): icmp_seq=4 ttl=57 time=3.63 ms
            //
            //--- www.a.shifen.com ping statistics ---
            //4 packets transmitted, 4 received, 0% packet loss, time 3005ms
            //rtt min/avg/max/mdev = 3.205/3.502/3.761/0.221 ms
            //
            if (matcher.find() && matcher.groupCount() == 4) {
                double min = Double.parseDouble(matcher.group(1));
                double avg = Double.parseDouble(matcher.group(2));
                double max = Double.parseDouble(matcher.group(3));
                return new PingResult(true, min, max, avg);
            }
            return new PingResult(false);
        } else {
            return new PingResult(false);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PingResult extends Base {
        private boolean connect;
        private double minDelay;
        private double maxDelay;
        private double avgDelay;

        public PingResult(boolean connect) {
            this.connect = connect;
        }
    }

//    public static void main(String[] args) {
//        Ping ping = new Ping();
//        PingResult pingResult = ping.exec();
//        System.out.println(pingResult);
//    }

}
