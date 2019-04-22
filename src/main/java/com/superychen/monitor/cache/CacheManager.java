package com.superychen.monitor.cache;

import com.superychen.monitor.model.CacheModel;
import com.superychen.monitor.service.ConfigManagerService;
import com.superychen.monitor.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 缓存管理-单例
 */
@Slf4j
public class CacheManager {

    private static final CacheManager instance;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static FileCache fileCache;

    static {
        instance = new CacheManager();
        fileCache = FileCache.getInstance();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    private CacheManager() {
    }

    /**
     * 缓存数据
     *
     * @param time 数据获取时间
     * @param data 数据内容
     * @param <T>  数据泛型
     */
    public <T> void cache(long time, T data) {
        String uid = SIMPLE_DATE_FORMAT.format(new Date(time)) + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        CacheModel cacheModel = new CacheModel(uid, JsonUtil.toJson(data), time + ConfigManagerService.getInstance().getMonitorConfig().getCacheTime());
        //存文件
        fileCache.cache(cacheModel);
    }

    /**
     * 加载所有缓存
     *
     * @return 缓存数据列表
     */
    public List<CacheModel> loadAll() {
        return fileCache.loadAll();
    }

    /**
     * 重新缓存-取出去发送,但是发送失败了,重新缓存起来
     *
     * @param cacheModels 数据列表
     */
    public void reCache(List<CacheModel> cacheModels) {
        for (CacheModel cacheModel : cacheModels) {
            fileCache.cache(cacheModel);
        }
    }

    /**
     * 清空过期缓存数据
     */
    public void cleanCache() {
        fileCache.removeOverTimeCaches();
    }

}
