package com.superychen.monitor.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件操作工具类
 */
@Slf4j
public class FileUtil {

    /**
     * 读文件
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String read(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 读文件并删除
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readAndDelete(String path) {
        String read = read(path);
        if (read != null) {
            delete(path);
        }
        return read;
    }


    /**
     * 写文件
     *
     * @param path    文件路径
     * @param content 要写入的内容
     */
    public static void write(String path, String content) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(content);
        } catch (Exception ignored) {
        }
    }

    /**
     * 列出文件夹下所有文件名称
     *
     * @param dirPath 目标文件夹
     * @return 文件名称列表
     */
    public static List<String> list(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            return Collections.emptyList();
        }
        String[] list = file.list();
        if (null == list) {
            return Collections.emptyList();
        }
        return Arrays.asList(list);
    }

    /**
     * 列出文件夹下所有名称满足正则表达式的文件名称
     *
     * @param dirPath 目标文件夹
     * @param regex   文件名正则
     * @return 满足的文件名列表
     */
    public static List<String> list(String dirPath, String regex) {
        List<String> fileNames = list(dirPath);
        Pattern pattern = Pattern.compile(regex);
        return fileNames.stream().filter(n -> pattern.matcher(n).find()).collect(Collectors.toList());
    }

    public static void delete(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            boolean delete = file.delete();
        }
    }

}
