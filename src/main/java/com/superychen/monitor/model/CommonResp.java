package com.superychen.monitor.model;

import lombok.Data;

@Data
public class CommonResp<T> {

    private int code;
    private String msg;
    private T data;

}
