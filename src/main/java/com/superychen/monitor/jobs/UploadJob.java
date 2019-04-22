package com.superychen.monitor.jobs;

import com.superychen.monitor.cache.CacheManager;
import com.superychen.monitor.model.CacheModel;
import com.superychen.monitor.model.MonitorInfo;
import com.superychen.monitor.service.UploadService;
import com.superychen.monitor.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
//等待前一次任务执行完成再去执行下一次的
@DisallowConcurrentExecution
public class UploadJob implements Job {

    private UploadService uploadService = new UploadService();
    private CacheManager cacheManager = CacheManager.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            //随机等待10秒
            Thread.sleep(1000 * new Random().nextInt(30));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("scheduler upload start");
        //获取缓存的数据
        List<CacheModel> cacheModels = cacheManager.loadAll();
        //取出监控信息
        List<MonitorInfo> monitorInfos = cacheModels.stream().map(cacheModel -> JsonUtil.fromJson(cacheModel.getData(), MonitorInfo.class)).collect(Collectors.toList());
        //批量上报
        boolean upload = uploadService.upload(monitorInfos);
        //如果没有上报成功
        if (!upload) {
            //重新缓存起来
            cacheManager.reCache(cacheModels);
        }
        log.debug("scheduler upload complete");
    }

}