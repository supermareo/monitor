package com.superychen.monitor.jobs;

import com.superychen.monitor.service.CollectService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
//等待前一次任务执行完成再去执行下一次的
@DisallowConcurrentExecution
public class CollectJob implements Job {

    private CollectService collectService = new CollectService();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.debug("scheduler collect start");
        collectService.collect();
        log.debug("scheduler collect complete");
    }

}