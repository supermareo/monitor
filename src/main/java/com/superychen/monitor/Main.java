package com.superychen.monitor;

import com.superychen.monitor.service.ConfigManagerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        ConfigManagerService instance = ConfigManagerService.getInstance();
    }

}
