package com.superychen.monitor.model;

import lombok.Data;

/**
 * 缓存对象
 */
@Data
public class CacheModel {

    //唯一标示
    private String uid;
    //创建时间
    private Long createTime;
    //失效时间
    private Long deadTime;
    //数据
    private String data;

    public CacheModel(String uid, String data) {
        this.uid = uid;
        this.createTime = System.currentTimeMillis();
        this.deadTime = -1L;
        this.data = data;
    }

    public CacheModel(String uid, String data, long deadTime) {
        this.uid = uid;
        this.createTime = System.currentTimeMillis();
        this.deadTime = deadTime;
        this.data = data;
    }

}
