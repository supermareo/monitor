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
 * 查看 /proc/meminfo 文件获取内存信息
 * 查看内存使用情况
 */
@Slf4j
public class MemInfo extends LinuxCommand<MemInfo.MemoryResult> {

    private static final Pattern PATTERN_TOTAL = Pattern.compile("MemTotal:\\s+(\\d+)\\s+kB");
    private static final Pattern PATTERN_FREE = Pattern.compile("MemFree:\\s+(\\d+)\\s+kB");
    private static final Pattern PATTERN_CACHED = Pattern.compile("Cached:\\s+(\\d+)\\s+kB");
    private static final Pattern PATTERN_AVAILABLE = Pattern.compile("MemAvailable:\\s+(\\d+)\\s+kB");

    private static final String MEMINFO_FILE = PropertiesUtil.getProperty("path.meminfo", "/proc/meminfo");

    @Override
    String command() {
        return "cat " + MEMINFO_FILE;
    }

    @Override
    MemoryResult process(LinuxExecResult result) {
        if (result.isSuccess()) {
            String resp = result.getResp();
            //正确执行完成,resp如下:
            //MemTotal:       13723204 kB
            //MemFree:         6429412 kB
            //MemAvailable:    9820096 kB
            //Buffers:           79248 kB
            //Cached:          3321808 kB
            //SwapCached:            0 kB
            //Active:          4525764 kB
            //Inactive:        1925876 kB
            //Active(anon):    3052560 kB
            //Inactive(anon):     5180 kB
            //Active(file):    1473204 kB
            //Inactive(file):  1920696 kB
            //Unevictable:          16 kB
            //Mlocked:              16 kB
            //SwapTotal:       2097148 kB
            //SwapFree:        2097148 kB
            //Dirty:             22696 kB
            //Writeback:             0 kB
            //AnonPages:       3050884 kB
            //Mapped:           448632 kB
            //Shmem:              7160 kB
            //Slab:             398732 kB
            //SReclaimable:     324412 kB
            //SUnreclaim:        74320 kB
            //KernelStack:       14372 kB
            //PageTables:        49096 kB
            //NFS_Unstable:          0 kB
            //Bounce:                0 kB
            //WritebackTmp:          0 kB
            //CommitLimit:     8958748 kB
            //Committed_AS:    7738008 kB
            //VmallocTotal:   34359738367 kB
            //VmallocUsed:           0 kB
            //VmallocChunk:          0 kB
            //HardwareCorrupted:     0 kB
            //AnonHugePages:         0 kB
            //ShmemHugePages:        0 kB
            //ShmemPmdMapped:        0 kB
            //CmaTotal:              0 kB
            //CmaFree:               0 kB
            //HugePages_Total:       0
            //HugePages_Free:        0
            //HugePages_Rsvd:        0
            //HugePages_Surp:        0
            //Hugepagesize:       2048 kB
            //DirectMap4k:      253760 kB
            //DirectMap2M:     9609216 kB
            //DirectMap1G:     5242880 kB
            Matcher tMatcher = PATTERN_TOTAL.matcher(resp);
            Matcher fMatcher = PATTERN_FREE.matcher(resp);
            Matcher cMatcher = PATTERN_CACHED.matcher(resp);
            Matcher aMatcher = PATTERN_AVAILABLE.matcher(resp);
            long t = -1, f = -1, a = -1, c = -1;
            if (tMatcher.find()) {
                t = Long.parseLong(tMatcher.group(1));
            }
            if (fMatcher.find()) {
                f = Long.parseLong(fMatcher.group(1));
            }
            if (cMatcher.find()) {
                c = Long.parseLong(cMatcher.group(1));
            }
            if (aMatcher.find()) {
                a = Long.parseLong(aMatcher.group(1));
            }
            return new MemoryResult(t, f, c, a);
        }
        return new MemoryResult(-1, -1, -1, -1);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryResult extends Base {
        private long total;
        private long free;
        private long cached;
        private long available;
    }

//    public static void main(String[] args) {
//        System.out.println(new Memory().exec());
//    }

}
