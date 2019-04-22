package com.superychen.monitor.jobs;

import com.superychen.monitor.cache.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
//等待前一次任务执行完成再去执行下一次的
@DisallowConcurrentExecution
public class CleanCacheJob implements Job {

    private CacheManager cacheManager = CacheManager.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.debug("scheduler clean cache start");
        cacheManager.cleanCache();
        log.debug("scheduler clean cache complete");
    }

}