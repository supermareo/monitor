package com.superychen.monitor.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态配置信息
 */
@Slf4j
@Data
public class MonitorConfig extends Base {

    //主机号,用来支持对指定主机的配置
    //如果为*,则表示对所有主机的配置
    //支持多个主机,以 , 逗号分割
    @SerializedName("ci")
    private String ccuId;
    //版本
    @SerializedName("v")
    private String version;
    //是否进行采集与上报
    @SerializedName("ac")
    private boolean active;
    //采集任务cron
    @SerializedName("cc")
    private String cronCollect;
    //上报任务cron
    @SerializedName("cu")
    private String cronUpload;
    //缓存有效期
    @SerializedName("ct")
    private long cacheTime;

}
