package com.superychen.monitor.utils.linux;

import com.superychen.monitor.model.Base;
import com.superychen.monitor.model.LinuxExecResult;
import com.superychen.monitor.utils.PropertiesUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top命令
 */
@Slf4j
public class Top extends LinuxCommand<Top.TopResult> {

    //top - 14:49:56 up 5 days, 20:49,  4 users,  load average: 0.98, 0.88, 0.8
    //top - 17:14:34 up  1:54,  1 user,  load average: 0.46, 0.78, 0.68
    //private static final Pattern PATTERN = Pattern.compile("top\\s+-\\s+([0-9:]+)\\s+up\\s+([\\d+ days,]*\\s+[0-9:]+),\\s+(\\d+)\\s+user[s]*,\\s+load average:\\s+([0-9.]+),\\s+([0-9.]+),\\s+([0-9.]+)");
    //private static final Pattern PATTERN_SUMMARY = Pattern.compile("top\\s+-\\s+(\\S+)\\s+up\\s+(\\d+:\\d+),\\s+(\\d+)\\s+user,\\s+load average:\\s+([0-9.]+),\\s+([0-9.]+),\\s+([0-9.]+)");
    private static final Pattern PATTERN_SUMMARY = Pattern.compile(PropertiesUtil.getProperty("REGEX_TOP_SUMMARY"));
    //任务: 310 total,   1 running, 234 sleeping,   0 stopped,   0 zombie
    //Tasks: 178 total,   1 running, 177 sleeping,   0 stopped,   0 zombie
    //private static final Pattern PATTERN_TASK = Pattern.compile("任务:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+running,\\s+(\\d+)\\s+sleeping," +
    //        "\\s+(\\d+)\\s+stopped,\\s+(\\d+)\\s+zombie");
    private static final Pattern PATTERN_TASK = Pattern.compile(PropertiesUtil.getProperty("REGEX_TOP_TASK"));
    //%Cpu(s):  9.5 us,  3.8 sy,  0.4 ni, 83.7 id,  2.2 wa,  0.0 hi,  0.3 si,  0.0 st
    private static final Pattern PATTERN_CPU = Pattern.compile("%Cpu\\(s\\):\\s+([0-9.]+)\\s+us,\\s+([0-9.]+)\\s+sy,\\s+([0-9.]+)\\s+ni," +
            "\\s+([0-9.]+)\\s+id,\\s+([0-9.]+)\\s+wa,\\s+([0-9.]+)\\s+hi,\\s+([0-9.]+)\\s+si,\\s+([0-9.]+)\\s+st");

    //KiB Mem : 13723204 total,  6159052 free,  3699612 used,  3864540 buff/cache
    private static final Pattern PATTERN_MEM = Pattern.compile("KiB Mem\\s+:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+free,\\s+(\\d+)\\s+used,\\s+(\\d+)\\s+buff/cache");

    //KiB Swap:  2097148 total,  2097148 free,        0 used.  9688732 avail Mem
    private static final Pattern PATTERN_SWAP = Pattern.compile("KiB Swap:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+free,\\s+(\\d+)\\s+used.\\s+(\\d+)\\s+avail Mem");

    // 66656 superyc+  20   0 3119936  70652  24552 S 156.2  0.5   0:00.74 java
    private static final Pattern PATTERN_PROCESS = Pattern.compile("\\s*(\\d+)\\s+(\\S+)\\s+\\d+\\s+\\d+\\s+(\\d+)\\s+([0-9a-z.]+)\\s+(\\d+)\\s+([A-Z])\\s+([0-9.]+)\\s+([0-9.]+)\\s+(\\S+)\\s+(.*)");

    @Override
    String command() {
        return "top -b -n 1";
    }

    @Override
    TopResult process(LinuxExecResult result) {
        if (result.isSuccess()) {
            //正确执行完成,resp如下:
            //top - 17:14:34 up  1:54,  1 user,  load average: 0.46, 0.78, 0.68
            //任务: 310 total,   1 running, 234 sleeping,   0 stopped,   0 zombie
            //%Cpu(s):  9.5 us,  3.8 sy,  0.4 ni, 83.7 id,  2.2 wa,  0.0 hi,  0.3 si,  0.0 st
            //KiB Mem : 13723204 total,  6159052 free,  3699612 used,  3864540 buff/cache
            //KiB Swap:  2097148 total,  2097148 free,        0 used.  9688732 avail Mem
            //
            //进程 USER      PR  NI    VIRT    RES    SHR �  %CPU %MEM     TIME+ COMMAND
            // 66656 superyc+  20   0 3119936  70652  24552 S 156.2  0.5   0:00.74 java
            //  2438 superyc+  20   0 3954056 232096 100256 S   6.2  1.7   5:32.68 gnome-she+
            //  2957 superyc+  20   0 6585476 1.508g 117684 S   6.2 11.5  44:55.66 java
            // 66688 superyc+  20   0   51352   4280   3528 R   6.2  0.0   0:00.01 top
            //     1 root      20   0  225672   9636   6864 S   0.0  0.1   0:08.02 systemd
            //     2 root      20   0       0      0      0 S   0.0  0.0   0:00.02 kthreadd
            //     4 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kworker/0+
            //     6 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 mm_percpu+
            //     7 root      20   0       0      0      0 S   0.0  0.0   0:01.58 ksoftirqd+
            //     8 root      20   0       0      0      0 I   0.0  0.0   0:12.73 rcu_sched
            //     9 root      20   0       0      0      0 I   0.0  0.0   0:00.00 rcu_bh
            //    10 root      rt   0       0      0      0 S   0.0  0.0   0:00.05 migration+
            //    11 root      rt   0       0      0      0 S   0.0  0.0   0:00.02 watchdog/0
            //    12 root      20   0       0      0      0 S   0.0  0.0   0:00.00 cpuhp/0
            //    13 root      20   0       0      0      0 S   0.0  0.0   0:00.00 cpuhp/1
            //    14 root      rt   0       0      0      0 S   0.0  0.0   0:00.02 watchdog/1
            //    15 root      rt   0       0      0      0 S   0.0  0.0   0:00.03 migration+
            //    16 root      20   0       0      0      0 S   0.0  0.0   0:00.32 ksoftirqd+
            //    18 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kworker/1+
            //    19 root      20   0       0      0      0 S   0.0  0.0   0:00.00 cpuhp/2
            //    20 root      rt   0       0      0      0 S   0.0  0.0   0:00.02 watchdog/2
            //    21 root      rt   0       0      0      0 S   0.0  0.0   0:00.07 migration+
            //    22 root      20   0       0      0      0 S   0.0  0.0   0:01.03 ksoftirqd+
            //    24 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kworker/2+
            //    25 root      20   0       0      0      0 S   0.0  0.0   0:00.00 cpuhp/3
            //    26 root      rt   0       0      0      0 S   0.0  0.0   0:00.02 watchdog/3
            //    27 root      rt   0       0      0      0 S   0.0  0.0   0:00.03 migration+
            //    28 root      20   0       0      0      0 S   0.0  0.0   0:02.95 ksoftirqd+
            //    30 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kworker/3+
            //    31 root      20   0       0      0      0 S   0.0  0.0   0:00.00 kdevtmpfs
            //    32 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 netns
            //    33 root      20   0       0      0      0 S   0.0  0.0   0:00.00 rcu_tasks+
            //    34 root      20   0       0      0      0 S   0.0  0.0   0:00.00 kauditd
            //    37 root      20   0       0      0      0 S   0.0  0.0   0:00.01 khungtaskd
            //    38 root      20   0       0      0      0 S   0.0  0.0   0:00.00 oom_reaper
            //    39 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 writeback
            //    40 root      20   0       0      0      0 S   0.0  0.0   0:00.00 kcompactd0
            //    41 root      25   5       0      0      0 S   0.0  0.0   0:00.00 ksmd
            //    42 root      39  19       0      0      0 S   0.0  0.0   0:00.00 khugepaged
            //    43 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 crypto
            //    44 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kintegrit+
            //    45 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kblockd
            //    46 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 ata_sff
            //    47 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 md
            //    48 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 edac-poll+
            //    49 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 devfreq_wq
            //    50 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 watchdogd
            //    55 root      20   0       0      0      0 S   0.0  0.0   0:00.00 kswapd0
            //    56 root      20   0       0      0      0 S   0.0  0.0   0:00.00 ecryptfs-+
            //    98 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kthrotld
            //    99 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 acpi_ther+
            //   100 root      20   0       0      0      0 S   0.0  0.0   0:00.02 scsi_eh_0
            //   101 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_0
            //   102 root      20   0       0      0      0 S   0.0  0.0   0:00.01 scsi_eh_1
            //   103 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_1
            //   109 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 ipv6_addr+
            //   118 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 kstrp
            //   135 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 charger_m+
            //   193 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 mpt_poll_0
            //   194 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 mpt/0
            //   195 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_2
            //   196 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_2
            //   197 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_3
            //   198 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_3
            //   199 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_4
            //   200 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_4
            //   201 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_5
            //   202 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_5
            //   203 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_6
            //   204 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_6
            //   205 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_7
            //   206 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_7
            //   207 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_8
            //   208 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_8
            //   209 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_9
            //   210 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_9
            //   211 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_10
            //   212 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   213 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_11
            //   214 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   215 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_12
            //   216 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   217 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_13
            //   218 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   219 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_14
            //   220 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   221 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_15
            //   222 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   223 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_16
            //   224 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   225 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_17
            //   226 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   227 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_18
            //   228 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   229 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_19
            //   230 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   231 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_20
            //   232 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   233 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_21
            //   234 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   235 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_22
            //   236 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   237 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_23
            //   238 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   239 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_24
            //   240 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   241 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_25
            //   242 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   243 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_26
            //   244 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   245 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_27
            //   246 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   247 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_28
            //   248 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   249 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_29
            //   250 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   251 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_30
            //   252 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   253 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_31
            //   254 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   255 root      20   0       0      0      0 S   0.0  0.0   0:00.00 scsi_eh_32
            //   256 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 scsi_tmf_+
            //   284 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 ttm_swap
            //   285 root     -51   0       0      0      0 S   0.0  0.0   0:05.55 irq/16-vm+
            //   293 root       0 -20       0      0      0 I   0.0  0.0   0:01.63 kworker/0+
            //   297 root       0 -20       0      0      0 I   0.0  0.0   0:00.17 kworker/1+
            //   318 root       0 -20       0      0      0 I   0.0  0.0   0:00.47 kworker/2+
            //   324 root       0 -20       0      0      0 I   0.0  0.0   0:01.98 kworker/3+
            //   330 root      20   0       0      0      0 S   0.0  0.0   0:03.40 jbd2/sda1+
            //   331 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 ext4-rsv-+
            //   389 root      20   0       0      0      0 I   0.0  0.0   0:00.43 kworker/2+
            //   397 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop0
            //   402 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop1
            //   403 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop2
            //   404 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop3
            //   413 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop5
            //   414 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop6
            //   415 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop7
            //   416 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop8
            //   418 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop9
            //   420 root       0 -20       0      0      0 S   0.0  0.0   0:00.16 loop10
            //   423 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop11
            //   425 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop12
            //   427 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop13
            //   428 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop14
            //   432 root       0 -20       0      0      0 S   0.0  0.0   0:00.01 loop15
            //   433 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop16
            //   710 root      20   0  503356  11140   8796 S   0.0  0.1   0:00.14 udisksd
            //   711 message+  20   0   51860   6280   3860 S   0.0  0.0   0:02.47 dbus-daem+
            //   751 root      20   0  110508   3496   3164 S   0.0  0.0   0:00.77 irqbalance
            //   753 root      20   0    4552    788    724 S   0.0  0.0   0:01.81 acpid
            //   757 root      20   0   38428   3272   2984 S   0.0  0.0   0:00.01 cron
            //   758 root      20   0  186936   9232   8436 S   0.0  0.1   0:03.88 thermald
            //   760 root      20   0  353528   9016   7672 S   0.0  0.1   0:00.06 ModemMana+
            //   787 root      20   0  177628  17184   9256 S   0.0  0.1   0:00.16 networkd-+
            //   788 root      20   0  566076  16948  14028 S   0.0  0.1   0:02.66 NetworkMa+
            //   792 avahi     20   0   47488   3908   3220 S   0.0  0.0   0:02.28 avahi-dae+
            //   795 syslog    20   0  263036   5136   3572 S   0.0  0.0   0:00.14 rsyslogd
            //   798 root      20   0   70708   6248   5440 S   0.0  0.0   0:00.66 systemd-l+
            //   801 root      20   0  294996   7388   6412 S   0.0  0.1   0:00.28 accounts-+
            //   858 colord    20   0  940004  15052   9744 S   0.0  0.1   0:00.23 colord
            //   859 avahi     20   0   47076    344      0 S   0.0  0.0   0:00.00 avahi-dae+
            //   883 root      20   0  194316  19872  11960 S   0.0  0.1   0:00.14 unattende+
            //   912 root      20   0   25660   5956   4736 S   0.0  0.0   0:00.01 dhclient
            //   934 root      20   0  308300   8084   6972 S   0.0  0.1   0:00.03 gdm3
            //   957 root      20   0  261772   8452   7360 S   0.0  0.1   0:00.03 gdm-sessi+
            //   973 whoopsie  20   0  462188  12580  10916 S   0.0  0.1   0:00.04 whoopsie
            //   990 kernoops  20   0   56936    420      0 S   0.0  0.0   0:00.15 kerneloops
            //   992 kernoops  20   0   56936    420      0 S   0.0  0.0   0:00.16 kerneloops
            //  1118 gdm       20   0   76988   8164   6776 S   0.0  0.1   0:00.08 systemd
            //  1120 gdm       20   0  114104   2664      4 S   0.0  0.0   0:00.00 (sd-pam)
            //  1126 mysql     20   0 1418568 178180  12924 S   0.0  1.3   0:38.48 mysqld
            //  1135 gdm       20   0  197912   5692   5184 S   0.0  0.0   0:00.00 gdm-wayla+
            //  1137 gdm       20   0   50260   4496   3708 S   0.0  0.0   0:00.14 dbus-daem+
            //  1151 gdm       20   0  559272  14144  11844 S   0.0  0.1   0:00.23 gnome-ses+
            //  1192 gdm       20   0 3427252 154216  92240 S   0.0  1.1   0:05.75 gnome-she+
            //  1292 root      20   0  322396   8860   7716 S   0.0  0.1   0:00.05 upowerd
            //  2057 gdm       20   0  543160  51316  35236 S   0.0  0.4   0:00.25 Xwayland
            //  2064 gdm       20   0  349280   6184   5596 S   0.0  0.0   0:00.01 at-spi-bu+
            //  2069 gdm       20   0   49924   4148   3676 S   0.0  0.0   0:00.01 dbus-daem+
            //  2071 gdm       20   0  220760   7068   6300 S   0.0  0.1   0:00.02 at-spi2-r+
            //  2074 gdm       20   0 1153500  11524   8484 S   0.0  0.1   0:00.24 pulseaudio
            //  2076 rtkit     21   1  183504   2952   2684 S   0.0  0.0   0:00.13 rtkit-dae+
            //  2099 root      20   0  448788  17804  13180 S   0.0  0.1   0:16.27 packageki+
            //  2158 root      20   0  297076   7044   6308 S   0.0  0.1   0:00.03 boltd
            //  2162 gdm       20   0  644468  51232  39964 S   0.0  0.4   0:00.14 gsd-xsett+
            //  2168 gdm       20   0  278452   6028   5476 S   0.0  0.0   0:00.01 gsd-a11y-+
            //  2169 gdm       20   0  493440  49556  38884 S   0.0  0.4   0:00.13 gsd-clipb+
            //  2170 gdm       20   0  808588  51572  40212 S   0.0  0.4   0:00.84 gsd-color
            //  2173 gdm       20   0  393856  13752  11884 S   0.0  0.1   0:00.03 gsd-datet+
            //  2174 gdm       20   0  283888   5344   4932 S   0.0  0.0   0:00.01 gsd-house+
            //  2177 gdm       20   0  647832  50120  39232 S   0.0  0.4   0:00.14 gsd-keybo+
            //  2178 gdm       20   0 1164000  52636  41220 S   0.0  0.4   0:00.19 gsd-media+
            //  2182 gdm       20   0  202144   4612   4236 S   0.0  0.0   0:00.02 gsd-mouse
            //  2183 gdm       20   0  667100  51528  40344 S   0.0  0.4   0:00.17 gsd-power
            //  2186 gdm       20   0  267152   8752   7688 S   0.0  0.1   0:00.01 gsd-print+
            //  2187 gdm       20   0  202164   4720   4348 S   0.0  0.0   0:00.00 gsd-rfkill
            //  2188 gdm       20   0  275880   5880   5308 S   0.0  0.0   0:00.00 gsd-scree+
            //  2191 gdm       20   0  305400   8580   7716 S   0.0  0.1   0:00.00 gsd-shari+
            //  2197 gdm       20   0  378232   8264   7460 S   0.0  0.1   0:00.03 gsd-smart+
            //  2201 gdm       20   0  333148   8320   7352 S   0.0  0.1   0:00.08 gsd-sound
            //  2204 gdm       20   0  578360  50508  39372 S   0.0  0.4   0:00.16 gsd-wacom
            //  2279 root      20   0  415568   9220   7788 S   0.0  0.1   0:00.04 gdm-sessi+
            //  2282 superyc+  20   0   77008   8308   6788 S   0.0  0.1   0:00.13 systemd
            //  2283 superyc+  20   0  114104   2668      4 S   0.0  0.0   0:00.00 (sd-pam)
            //  2296 superyc+  20   0  436104   8032   6904 S   0.0  0.1   0:00.07 gnome-key+
            //  2301 superyc+  20   0  212244   6172   5556 S   0.0  0.0   0:00.00 gdm-x-ses+
            //  2303 superyc+  20   0  443228 100144  38828 S   0.0  0.7   5:12.95 Xorg
            //  2312 superyc+  20   0   52588   7164   3924 S   0.0  0.1   0:04.12 dbus-daem+
            //  2315 superyc+  20   0  560016  15080  12396 S   0.0  0.1   0:00.24 gnome-ses+
            //  2337 superyc+  20   0  450112  98556  46128 S   0.0  0.7   0:18.04 fcitx
            //  2353 superyc+  20   0   50072   3540   3164 S   0.0  0.0   0:00.21 dbus-daem+
            //  2359 superyc+  39  19   27592    212      0 S   0.0  0.0   0:00.00 fcitx-dbu+
            //  2411 superyc+  20   0   11304    320      0 S   0.0  0.0   0:00.03 ssh-agent
            //  2414 superyc+  20   0  349316   6280   5700 S   0.0  0.0   0:00.01 at-spi-bu+
            //  2419 superyc+  20   0   49928   4308   3804 S   0.0  0.0   0:00.46 dbus-daem+
            //  2421 superyc+  20   0  220772   6884   6172 S   0.0  0.1   0:02.61 at-spi2-r+
            //  2444 superyc+  20   0  292092   7244   6356 S   0.0  0.1   0:00.08 gvfsd
            //  2449 superyc+  20   0  416112   5408   4872 S   0.0  0.0   0:00.01 gvfsd-fuse
            //  2460 superyc+   9 -11 1172368  12132   8972 S   0.0  0.1   0:00.85 pulseaudio
            //  2491 superyc+  20   0  689916  21000  17800 S   0.0  0.2   0:00.08 gnome-she+
            //  2498 superyc+  20   0 1306956  26904  22720 S   0.0  0.2   0:00.19 evolution+
            //  2505 superyc+  20   0  306744   8760   7140 S   0.0  0.1   0:00.28 gvfs-udis+
            //  2512 superyc+  20   0  774580  31000  25656 S   0.0  0.2   0:00.12 goa-daemon
            //  2513 superyc+  20   0  378944   7740   6856 S   0.0  0.1   0:00.05 gvfs-afc-+
            //  2520 superyc+  20   0  288760   6684   5908 S   0.0  0.0   0:00.01 gvfs-gpho+
            //  2524 superyc+  20   0  274176   6088   5520 S   0.0  0.0   0:00.01 gvfs-goa-+
            //  2533 superyc+  20   0  377340   8408   7400 S   0.0  0.1   0:00.42 goa-ident+
            //  2539 superyc+  20   0  275972   4796   4332 S   0.0  0.0   0:00.02 gvfs-mtp-+
            //  2543 superyc+  20   0  521768  27188  17776 S   0.0  0.2   0:00.57 gsd-power
            //  2545 superyc+  20   0  349612  10672   9308 S   0.0  0.1   0:00.03 gsd-print+
            //  2546 superyc+  20   0  423492   5924   5260 S   0.0  0.0   0:00.02 gsd-rfkill
            //  2547 superyc+  20   0  275880   4740   4352 S   0.0  0.0   0:00.01 gsd-scree+
            //  2551 superyc+  20   0  453136   9568   8296 S   0.0  0.1   0:00.05 gsd-shari+
            //  2554 superyc+  20   0  378240   8216   7412 S   0.0  0.1   0:00.02 gsd-smart+
            //  2559 superyc+  20   0  333160   7828   6920 S   0.0  0.1   0:00.01 gsd-sound
            //  2565 superyc+  20   0  499200  26628  17368 S   0.0  0.2   0:00.69 gsd-xsett+
            //  2568 superyc+  20   0  433892  26796  16896 S   0.0  0.2   0:00.54 gsd-wacom
            //  2579 superyc+  20   0  278460   5984   5432 S   0.0  0.0   0:00.01 gsd-a11y-+
            //  2583 superyc+  20   0  349436  25428  16064 S   0.0  0.2   0:00.49 gsd-clipb+
            //  2586 superyc+  20   0  470036  13800  11940 S   0.0  0.1   0:00.02 gsd-datet+
            //  2587 superyc+  20   0  364892   7600   6764 S   0.0  0.1   0:00.17 gsd-house+
            //  2588 superyc+  20   0  663320  26616  17360 S   0.0  0.2   0:01.27 gsd-color
            //  2592 superyc+  20   0 1084064  28188  18964 S   0.0  0.2   0:00.62 gsd-media+
            //  2595 superyc+  20   0  278468   5984   5432 S   0.0  0.0   0:00.01 gsd-mouse
            //  2598 superyc+  20   0  498472  20472  15864 S   0.0  0.1   0:00.47 gsd-keybo+
            //  2634 superyc+  20   0  508904  12772  11012 S   0.0  0.1   0:00.02 gsd-print+
            //  2637 superyc+  20   0 1029064  60104  42960 S   0.0  0.4   0:04.12 nautilus-+
            //  2644 superyc+  20   0  368492   7568   6552 S   0.0  0.1   0:00.04 gvfsd-tra+
            //  2645 superyc+  20   0  244036  29448  24800 S   0.0  0.2   0:21.33 vmtoolsd
            //  2646 superyc+  20   0  271932   6224   5188 S   0.0  0.0   0:00.02 gsd-disk-+
            //  2697 superyc+  20   0  892488  67632  27660 S   0.0  0.5   0:00.55 evolution+
            //  2727 superyc+  20   0  188036   5452   4636 S   0.0  0.0   0:00.03 dconf-ser+
            //  2746 superyc+  20   0 1136212  63172  24156 S   0.0  0.5   0:00.42 evolution+
            //  2748 superyc+  20   0   80132   6216   5688 S   0.0  0.0   0:00.03 gconfd-2
            //  2763 superyc+  20   0  733004  25140  21744 S   0.0  0.2   0:00.04 evolution+
            //  2772 superyc+  20   0 1019812  31156  25300 S   0.0  0.2   0:00.32 evolution+
            //  2785 superyc+  20   0 3341252 111120  58488 S   0.0  0.8   0:22.33 sogou-qim+
            //  2902 superyc+  20   0    4628   1724   1616 S   0.0  0.0   0:00.00 idea.sh
            //  3187 superyc+  20   0    8744   1768   1572 S   0.0  0.0   0:01.70 fsnotifie+
            //  3357 superyc+  20   0  484756  24440  18632 S   0.0  0.2   0:01.88 sogou-qim+
            //  3407 superyc+  20   0 1439176 154192  47368 S   0.0  1.1   0:05.68 gnome-sof+
            //  3409 superyc+  20   0  689064  32004  22244 S   0.0  0.2   0:00.84 update-no+
            //  3481 root      20   0  556012  22312  14468 S   0.0  0.2   0:00.48 fwupd
            //  3564 superyc+  20   0 4078448 195716  18824 S   0.0  1.4   0:22.64 java
            //  4199 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 xfsalloc
            //  4200 root       0 -20       0      0      0 I   0.0  0.0   0:00.00 xfs_mru_c+
            //  4205 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsIO
            //  4206 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsCommit
            //  4207 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsCommit
            //  4208 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsCommit
            //  4209 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsCommit
            //  4210 root      20   0       0      0      0 S   0.0  0.0   0:00.00 jfsSync
            //  4555 superyc+  20   0  787936  32012  26348 S   0.0  0.2   0:00.06 deja-dup-+
            // 19563 superyc+  20   0   32372   7712   3592 S   0.0  0.1   0:00.68 bash
            // 20168 root       0 -20       0      0      0 S   0.0  0.0   0:00.02 loop17
            // 20933 root      20   0       0      0      0 I   0.0  0.0   0:00.86 kworker/2+
            // 20963 root      20   0 1309724  25660  12944 S   0.0  0.2   0:14.62 snapd
            // 22343 root      20   0   44752   2840   2368 S   0.0  0.0   0:00.03 wpa_suppl+
            // 22887 root      20   0  108512   8948   6912 S   0.0  0.1   0:00.01 cupsd
            // 22888 root      20   0  303668  10892   9452 S   0.0  0.1   0:00.02 cups-brow+
            // 34825 superyc+  20   0   11304   1564   1320 S   0.0  0.0   0:00.01 ssh-agent
            // 37947 root      20   0   46148   4496   3152 S   0.0  0.0   0:00.13 systemd-u+
            // 47588 systemd+  20   0   70748   5340   4780 S   0.0  0.0   0:00.14 systemd-r+
            // 47592 systemd+  20   0  143976   3236   2708 S   0.0  0.0   0:00.06 systemd-t+
            // 47595 root      19  -1   94828  14124  13368 S   0.0  0.1   0:00.19 systemd-j+
            // 49439 root      20   0  303572  10360   7108 S   0.0  0.1   0:00.36 polkitd
            // 58022 superyc+  30  10  888232 171760 103168 S   0.0  1.3   0:16.55 update-ma+
            // 58490 superyc+  20   0  699208  52180  37184 S   0.0  0.4   0:03.76 gnome-ter+
            // 58497 superyc+  20   0   30480   6000   3840 S   0.0  0.0   0:00.10 bash
            // 59053 root      20   0       0      0      0 I   0.0  0.0   0:53.76 kworker/3+
            // 59054 root      20   0       0      0      0 I   0.0  0.0   0:00.00 kworker/3+
            // 59059 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop4
            // 59130 root      20   0       0      0      0 I   0.0  0.0   0:00.00 kworker/0+
            // 59132 root      20   0       0      0      0 I   0.0  0.0   0:00.64 kworker/0+
            // 59137 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop18
            // 59225 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop19
            // 59233 root      20   0       0      0      0 I   0.0  0.0   0:06.79 kworker/1+
            // 59328 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop20
            // 59446 root       0 -20       0      0      0 S   0.0  0.0   0:00.00 loop21
            // 63300 superyc+  20   0 3741276 213156  19612 S   0.0  1.6   0:15.17 java
            // 65058 root      20   0       0      0      0 I   0.0  0.0   0:00.00 kworker/1+
            // 65069 superyc+  20   0  204532   6076   5508 S   0.0  0.0   0:00.01 gvfsd-met+
            // 65727 root      20   0       0      0      0 I   0.0  0.0   0:00.12 kworker/u+
            // 65981 root      25   5  316024 115032  69648 S   0.0  0.8   0:03.02 aptd
            // 66043 _apt      25   5   90704   8832   7820 S   0.0  0.1   0:03.54 http
            // 66044 _apt      25   5   90704   8672   7660 S   0.0  0.1   0:01.76 http
            // 66045 _apt      25   5   90712   8848   7820 S   0.0  0.1   0:01.18 http
            // 66046 root      20   0       0      0      0 I   0.0  0.0   0:00.11 kworker/u+
            // 66659 superyc+  20   0 6059412  47476  18132 S   0.0  0.3   0:00.51 java
            String resp = result.getResp();
            TopResult topResult = new TopResult();
            Cpu cpu = new Cpu();
            topResult.setCpu(cpu);

            Matcher suMatcher = PATTERN_SUMMARY.matcher(resp);
            if (suMatcher.find()) {
                String time = suMatcher.group(1);
                int user = Integer.parseInt(suMatcher.group(3));
                topResult.setSummary(new Summary(time, user));

                String runTime = suMatcher.group(2);
                double loadAverage5 = Double.parseDouble(suMatcher.group(4));
                double loadAverage10 = Double.parseDouble(suMatcher.group(5));
                double loadAverage15 = Double.parseDouble(suMatcher.group(6));
                cpu.setRunTime(runTime);
                cpu.setLoadAverage5(loadAverage5);
                cpu.setLoadAverage10(loadAverage10);
                cpu.setLoadAverage15(loadAverage15);
            }
            Matcher cMatcher = PATTERN_CPU.matcher(resp);
            if (cMatcher.find()) {
                double us = Double.parseDouble(cMatcher.group(1));
                double sy = Double.parseDouble(cMatcher.group(2));
                double ni = Double.parseDouble(cMatcher.group(3));
                double id = Double.parseDouble(cMatcher.group(4));
                double wa = Double.parseDouble(cMatcher.group(5));
                double hi = Double.parseDouble(cMatcher.group(6));
                double si = Double.parseDouble(cMatcher.group(7));
                double st = Double.parseDouble(cMatcher.group(8));
                cpu.setUs(us);
                cpu.setSy(sy);
                cpu.setNi(ni);
                cpu.setId(id);
                cpu.setWa(wa);
                cpu.setHi(hi);
                cpu.setSi(si);
                cpu.setSt(st);
            }

            Matcher tMatcher = PATTERN_TASK.matcher(resp);
            if (tMatcher.find()) {
                int total = Integer.parseInt(tMatcher.group(2));
                int running = Integer.parseInt(tMatcher.group(3));
                int sleeping = Integer.parseInt(tMatcher.group(4));
                int stopped = Integer.parseInt(tMatcher.group(5));
                int zombie = Integer.parseInt(tMatcher.group(6));
                topResult.setTask(new Task(total, running, sleeping, stopped, zombie));
            }

            Matcher mMatcher = PATTERN_MEM.matcher(resp);
            if (mMatcher.find()) {
                long total = Long.parseLong(mMatcher.group(1));
                long free = Long.parseLong(mMatcher.group(2));
                long used = Long.parseLong(mMatcher.group(3));
                long buffCache = Long.parseLong(mMatcher.group(4));
                topResult.setMemory(new Memory(total, free, used, buffCache));
            }

            Matcher swMatcher = PATTERN_SWAP.matcher(resp);
            if (swMatcher.find()) {
                long total = Long.parseLong(swMatcher.group(1));
                long free = Long.parseLong(swMatcher.group(2));
                long used = Long.parseLong(swMatcher.group(3));
                long avail = Long.parseLong(swMatcher.group(4));
                topResult.setSwap(new Swap(total, free, used, avail));
            }

            List<Process> processes = new ArrayList<>();
            String[] split = resp.split("\n");
            for (String line : split) {
                Matcher matcher = PATTERN_PROCESS.matcher(line);
                if (matcher.find()) {
                    double cpuUsage = Double.parseDouble(matcher.group(7));
                    if (cpuUsage <= 0) {
                        break;
                    }
                    long pid = Long.parseLong(matcher.group(1));
                    String user = matcher.group(2);
                    long virt = Long.parseLong(matcher.group(3));
                    String res = matcher.group(4);
                    long share = Long.parseLong(matcher.group(5));
                    String state = matcher.group(6);
                    double mem = Double.parseDouble(matcher.group(8));
                    String time = matcher.group(9);
                    String command = matcher.group(10);
                    Process process = new Process(pid, user, virt, res, share, state, cpuUsage, mem, time, command);
                    processes.add(process);
                }
                if (processes.size() >= 10) {
                    break;
                }
            }
            topResult.setTopNProcesses(processes);
            return topResult;
        }
        return null;
    }

    @Getter
    @Setter
    public static class TopResult extends Base {
        private Summary summary;
        private Task task;
        private Cpu cpu;
        private Memory memory;
        private Swap swap;
        private List<Process> topNProcesses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary extends Base {
        private String time;
        private int user;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task extends Base {
        private int total;
        private int running;
        private int sleeping;
        private int stopped;
        private int zombie;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cpu extends Base {
        private String runTime;
        private double loadAverage5;
        private double loadAverage10;
        private double loadAverage15;
        private double us;
        private double sy;
        private double ni;
        private double id;
        private double wa;
        private double hi;
        private double si;
        private double st;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Memory extends Base {
        private long total;
        private long free;
        private long used;
        private long buffCache;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Swap extends Base {
        private long total;
        private long free;
        private long used;
        private long available;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Process extends Base {
        private long pid;
        private String user;
        private long virt;
        private String res;
        private long share;
        private String state;
        private double cpu;
        private double mem;
        private String time;
        private String command;
    }

//    public static void main(String[] args) {
//        TopResult topResult = new Top().exec();
//        System.out.println(topResult);
//    }

}
