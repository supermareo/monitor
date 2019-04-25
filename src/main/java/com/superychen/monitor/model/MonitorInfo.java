package com.superychen.monitor.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 监控信息,用于最终上报
 * 使用SerializedName转换成json上传会减小报文内容
 */
@Data
public class MonitorInfo {

    //--检测时间
    @SerializedName("t")
    private long time;
    //--主机编号
    @SerializedName("ci")
    private String ccuId;
    //--内存信息
    //总内存
    @SerializedName("mt")
    private long memTotal;
    //空闲
    @SerializedName("mf")
    private long memFree;
    //缓存
    @SerializedName("mc")
    private long memCached;
    //可使用
    @SerializedName("ma")
    private long memAvailable;
    //--CPU信息
    //CPU核心数目
    @SerializedName("cc")
    private int cpuCore;
    //最近5分钟负载
    @SerializedName("cla5")
    private double cpuLoadAvg5;
    //最近10分钟负载
    @SerializedName("cla10")
    private double cpuLoadAvg10;
    //最近15分钟负载
    @SerializedName("cla15")
    private double cpuLoadAvg15;
    //用户空间占用CPU百分比
    @SerializedName("cu")
    private double cpuUs;
    //内核空间占用CPU百分比
    @SerializedName("cs")
    private double cpuSy;
    //--磁盘信息
    //文件系统
    @SerializedName("fs")
    private String fileSystem;
    //总存储空间
    @SerializedName("ft")
    private String fileTotal;
    //已使用存储空间
    @SerializedName("fu")
    private String fileUsed;
    //可用存储空间
    @SerializedName("fa")
    private String fileAvail;
    //已使用存储空间占比
    @SerializedName("fup")
    private int fileUsedPercentage;
    //挂在路径
    @SerializedName("fmp")
    private String fileMountedPath;
    //--网络情况
    //网络是否连通
    @SerializedName("nc")
    private boolean netConnect;
    //延迟最小值
    @SerializedName("nmd")
    private double netMinDelay;
    //延迟最大值
    @SerializedName("nxd")
    private double netMaxDelay;
    //延迟平均值
    @SerializedName("nad")
    private double netAvgDelay;
    //网线是否插入网口
    @SerializedName("nec")
    private String netEthConnect;

}
