package com.superychen.monitor.service;

import com.superychen.monitor.cache.CacheManager;
import com.superychen.monitor.model.MonitorInfo;
import com.superychen.monitor.utils.linux.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备信息采集服务
 */
@Slf4j
public class CollectService {

    //缓存管理器
    private CacheManager cacheManager = CacheManager.getInstance();

    private String ccuId = ConfigManagerService.CCU_ID;
    private Df df = new Df("/");
    private EthernetState ethernetState = new EthernetState();
    private MemInfo memInfo = new MemInfo();
    private Ping ping = new Ping();
    private CpuInfo cpuInfo = new CpuInfo();
    private Top top = new Top();

    public void collect() {
        long now = System.currentTimeMillis();
        //--收集系统信息
        Df.StorageResult df = this.df.exec();
        String ethernetState = this.ethernetState.exec();
        MemInfo.MemoryResult memInfo = this.memInfo.exec();
        Ping.PingResult ping = this.ping.exec();
        Top.TopResult top = this.top.exec();
        Integer cpuCore = this.cpuInfo.exec();
        //--构造对象
        MonitorInfo monitorInfo = new MonitorInfo();
        //时间
        monitorInfo.setTime(now);
        //主机编号
        monitorInfo.setCcuId(ccuId);
        //内存信息
        monitorInfo.setMemTotal(memInfo.getTotal());
        monitorInfo.setMemFree(memInfo.getFree());
        monitorInfo.setMemAvailable(memInfo.getAvailable());
        monitorInfo.setMemCached(memInfo.getCached());
        //cpu信息
        monitorInfo.setCore(cpuCore);
        Top.Cpu cpu = top.getCpu();
        monitorInfo.setCpuLoadAvg5(cpu.getLoadAverage5());
        monitorInfo.setCpuLoadAvg10(cpu.getLoadAverage10());
        monitorInfo.setCpuLoadAvg15(cpu.getLoadAverage15());
        monitorInfo.setCpuUs(cpu.getUs());
        monitorInfo.setCpuSy(cpu.getSy());
        //磁盘信息
        monitorInfo.setFileSystem(df.getFileSystem());
        monitorInfo.setFileTotal(df.getTotal());
        monitorInfo.setFileUsed(df.getUsed());
        monitorInfo.setFileAvail(df.getAvailable());
        monitorInfo.setFileUsedPercentage(df.getUsedPercent());
        monitorInfo.setFileMountedPath(df.getMountPath());
        //网络情况
        monitorInfo.setNetConnect(ping.isConnect());
        monitorInfo.setNetMinDelay(ping.getMinDelay());
        monitorInfo.setNetMaxDelay(ping.getMaxDelay());
        monitorInfo.setNetAvgDelay(ping.getAvgDelay());
        monitorInfo.setNetEthConnect(ethernetState);
        //缓存
        cacheManager.cache(now, monitorInfo);
    }

}
