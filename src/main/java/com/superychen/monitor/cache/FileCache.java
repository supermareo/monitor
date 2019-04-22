package com.superychen.monitor.cache;

import com.superychen.monitor.model.CacheModel;
import com.superychen.monitor.utils.FileUtil;
import com.superychen.monitor.utils.JsonUtil;
import com.superychen.monitor.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件缓存-单例
 */
@Slf4j
public class FileCache {

    private static final String CACHE_PATH = PropertiesUtil.getProperty("path.cache", "caches/");

    private static FileCache instance;

    public static FileCache getInstance() {
        return instance;
    }

    static {
        instance = new FileCache();
        File file = new File(CACHE_PATH);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            log.info("cache dir create result {}, {}", mkdirs, CACHE_PATH);
        } else {
            log.info("cache dir already exists, {}", file.getAbsolutePath());
        }
    }

    private FileCache() {
    }

    void cache(CacheModel data) {
        String fileName = "cache_" + data.getUid() + "_" + data.getDeadTime() + ".cache";
        FileUtil.write(CACHE_PATH + fileName, JsonUtil.toJson(data));
    }

    List<CacheModel> loadAll() {
        List<String> fileList = FileUtil.list(CACHE_PATH, "^cache_.*\\.cache$");
        if (fileList.isEmpty()) {
            return Collections.emptyList();
        }
        List<CacheModel> cacheModels = new ArrayList<>();
        for (String fileName : fileList) {
            String json = FileUtil.readAndDelete(CACHE_PATH + fileName);
            CacheModel cacheModel = JsonUtil.fromJson(json, CacheModel.class);
            if (cacheModel != null && cacheModel.getUid() != null) {
                cacheModels.add(cacheModel);
            }
        }
        return cacheModels;
    }

    /**
     * 清除过期文件
     */
    public void removeOverTimeCaches() {
        List<String> fileList = FileUtil.list(CACHE_PATH, "^cache_.*\\.cache$");
        fileList = fileList.stream().filter(this::overTime).collect(Collectors.toList());
        for (String fileName : fileList) {
            FileUtil.delete(CACHE_PATH + fileName);
        }
    }

    /**
     * 文件是否过期
     *
     * @param fileName 文件名
     * @return 是否过期
     */
    private boolean overTime(String fileName) {
        String[] split = fileName.split("_");
        String deadTime = split[split.length - 1];
        deadTime = deadTime.split("\\.")[0];
        long timestamp = Long.parseLong(deadTime);
        long now = System.currentTimeMillis();
        return now > timestamp;
    }

}
