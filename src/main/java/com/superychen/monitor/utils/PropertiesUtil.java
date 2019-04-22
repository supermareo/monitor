package com.superychen.monitor.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesUtil {

    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try (InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream("monitor.properties")) {
            PROPERTIES.load(is);
        } catch (Exception e) {
            log.error("load properties error, e=", e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key, null);
    }

    public static String getProperty(String key, String defaultVal) {
        return PROPERTIES.getProperty(key, defaultVal);
    }

}
