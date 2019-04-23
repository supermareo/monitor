package com.superychen.monitor.utils;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * 定时任务管理器
 */
@Slf4j
public class QuartzManagerUtil {

    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    /**
     * 添加定时任务
     *
     * @param job    任务名称
     * @param group  组名称
     * @param jobCls 任务类
     * @param cron   cron表达式
     */
    public static boolean addJob(String job, String group, Class<? extends Job> jobCls, String cron) {
        return addJob(job, group, jobCls, cron, null);
    }

    /**
     * 添加定时任务
     *
     * @param job    任务名称
     * @param group  组名称
     * @param jobCls 任务类
     * @param cron   cron表达式
     * @param delay  延迟执行毫秒数,为null或小于等于0时立即执行
     */
    public static boolean addJob(String job, String group, Class<? extends Job> jobCls, String cron, Long delay) {
        try {
            JobDetail jobDetail = JobBuilder.newJob().ofType(jobCls).withIdentity(job, group).build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing();
            TriggerBuilder<CronTrigger> tb = TriggerBuilder.newTrigger().withIdentity(job, group).withSchedule(scheduleBuilder);
            Trigger trigger = delay == null || delay <= 0 ? tb.startNow().build() : tb.startAt(new Date(System.currentTimeMillis() + delay)).build();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("addJob {},{},{} success", job, group, cron);
            return true;
        } catch (SchedulerException e) {
            log.error("addJob {},{},{} error, e=", job, group, cron, e);
            return false;
        }
    }

    /**
     * 删除定时任务
     *
     * @param job   任务名称
     * @param group 组名称
     */
    public static boolean removeJob(String job, String group) {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.pauseTrigger(new TriggerKey(job, group));
            scheduler.unscheduleJob(new TriggerKey(job, group));
            scheduler.deleteJob(new JobKey(job, group));
            return true;
        } catch (SchedulerException e) {
            log.error("removeJob {},{} error, e=", job, group, e);
            return false;
        }
    }

    /**
     * 修改定时任务时间
     *
     * @param job   任务名称
     * @param group 组名称
     * @param cron  cron表达式
     */
    public static boolean modifyJobTime(String job, String group, String cron) {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            TriggerKey triggerKey = new TriggerKey(job, group);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }
            String oldCron = trigger.getCronExpression();
            if (cron.equals(oldCron)) {
                return true;
            }
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(job, group));
            //删除老的定时任务
            removeJob(job, group);
            //添加新的定时任务
            addJob(job, group, jobDetail.getJobClass(), cron);
            log.info("modifyJobTime {},{} from {} to {} success", job, group, oldCron, cron);
            return true;
        } catch (SchedulerException e) {
            log.error("modifyJobTime {},{},{} error, e=", job, group, cron, e);
            return false;
        }
    }

}
