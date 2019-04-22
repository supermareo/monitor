package com.superychen.monitor.model;

import com.superychen.monitor.utils.JsonUtil;

public class Base {

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

}
