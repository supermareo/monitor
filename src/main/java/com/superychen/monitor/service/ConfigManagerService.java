package com.superychen.monitor.service;

import com.google.gson.reflect.TypeToken;
import com.superychen.monitor.jobs.CleanCacheJob;
import com.superychen.monitor.jobs.CollectJob;
import com.superychen.monitor.jobs.FetchConfigJob;
import com.superychen.monitor.jobs.UploadJob;
import com.superychen.monitor.model.CommonResp;
import com.superychen.monitor.model.MonitorConfig;
import com.superychen.monitor.utils.*;
import com.superychen.monitor.utils.linux.CcuId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 负责整个流程管理
 * 1.项目启动时,调用运维平台接口,获取配置
 * 2.根据配置信息,启动采集定时任务,推送定时任务等
 * 3.定期调用运维平台接口,获取配置
 * 4.根据配置动态更改
 * 4.1 采集/上报任务执行频率,执行开关
 */
@Slf4j
@Getter
public class ConfigManagerService {

    private static final ConfigManagerService instance;
    private static final String UPLOAD_TOKEN = PropertiesUtil.getProperty("token.upload");
    private static final Map<String, String> HEADER = new HashMap<String, String>() {
        {
            put("token", UPLOAD_TOKEN);
        }
    };
    //主机号
    public static final String CCU_ID = "CCU_" + new CcuId().exec();
    //如果这个文件不存在,启动报错,正好
    private static final String CONFIG_PATH = ConfigManagerService.class.getClassLoader().getResource("config.json").getPath();
    //配置信息获取地址
    private static final String URL_CONFIG = PropertiesUtil.getProperty("url.config", "https://192.168.50.253:8563/1.0/monitor/config/#CCU_ID");

    private MonitorConfig monitorConfig;
    //配置是否变更
    private boolean configModified;

    static {
        instance = new ConfigManagerService();
    }

    public static ConfigManagerService getInstance() {
        return instance;
    }

    private ConfigManagerService() {
        log.info("config manager init");
        //加载配置信息
        this.monitorConfig = loadConfig();
        parseConfig(this.monitorConfig, true);
        //添加定时清除任务,每2分钟执行一次
        QuartzManagerUtil.addJob("clean", "monitor", CleanCacheJob.class, "0 0/2 * * * ?");
        //添加定时获取配置信息任务,每2分钟执行一次,启动延迟2分钟执行
        QuartzManagerUtil.addJob("config", "monitor", FetchConfigJob.class, "0 0/2 * * * ?", 1000 * 60 * 2L);
        log.info("config manager complete");
    }

    private void parseConfig(MonitorConfig monitorConfig, boolean firstFlag) {
        log.info("parseConfig {},{}", monitorConfig, firstFlag);
        //如果配置文件没有改变,且不是首次启动
        //因为首次启动
        if (!this.configModified && !firstFlag) {
            return;
        }
        //关闭服务
        if (!monitorConfig.isActive()) {
            //如果是首次启动,什么都不用干
            if (firstFlag) {
                return;
            }
            //否则,清除定时任务
            //清除收集任务
            QuartzManagerUtil.removeJob("collect", "monitor");
            //清除上报任务
            QuartzManagerUtil.removeJob("upload", "monitor");
            return;
        }
        if (firstFlag) {
            //添加定时采集任务
            QuartzManagerUtil.addJob("collect", "monitor", CollectJob.class, monitorConfig.getCronCollect());
            //添加定时上传任务
            QuartzManagerUtil.addJob("upload", "monitor", UploadJob.class, monitorConfig.getCronUpload());
        } else {
            //修改定时采集任务执行时间
            QuartzManagerUtil.modifyJobTime("collect", "monitor", monitorConfig.getCronCollect());
            //修改定时上传任务执行时间
            QuartzManagerUtil.modifyJobTime("upload", "monitor", monitorConfig.getCronUpload());
        }
    }

    //加载配置
    //先调接口获取配置
    //如果接口获取不到配置,再从本地拿配置文件并解析
    private MonitorConfig loadConfig() {
        this.configModified = false;
        //调接口获取配置信息
        CommonResp<MonitorConfig> remoteResp = OkHttpClientUtil.get(URL_CONFIG.replace("#CCU_ID", CCU_ID), HEADER, new TypeToken<CommonResp<MonitorConfig>>() {
        }.getType());
        //读取本地配置信息
        MonitorConfig configFromLocal = JsonUtil.fromJson(FileUtil.read(CONFIG_PATH), MonitorConfig.class);
        //比较是否相同
        //如果接口返回空
        if (remoteResp == null || remoteResp.getCode() != 200 || remoteResp.getData() == null) {
            log.info("load config from remote empty, not modified");
            return configFromLocal;
        }
        MonitorConfig configFromRemote = remoteResp.getData();
        if (configFromRemote == null) {
            log.info("load config from remote empty, not modified");
            return configFromLocal;
        }
        //如果接口返回版本与本地版本一致,不改变
        if (configFromRemote.getVersion().equals(configFromLocal.getVersion())) {
            log.info("load config from remote version {} same to local, not modified", configFromRemote.getVersion());
            return configFromLocal;
        }
        //返回接口返回的配置
        //存到文件中并返回
        this.configModified = true;
        FileUtil.write(CONFIG_PATH, JsonUtil.toPrettyJson(configFromRemote));
        log.info("load config from remote diff from local, remote={}, local={}", configFromRemote, configFromLocal);
        return configFromRemote;
    }

    /**
     * 刷新配置
     */
    public void refreshConfig() {
        MonitorConfig config = loadConfig();
        parseConfig(config, false);
    }

}
