package com.superychen.monitor.jobs;

import com.superychen.monitor.service.ConfigManagerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
//等待前一次任务执行完成再去执行下一次的
@DisallowConcurrentExecution
public class FetchConfigJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        log.debug("scheduler fetch config start");
        ConfigManagerService.getInstance().refreshConfig();
        log.debug("scheduler fetch config start");
    }

}
