package com.superychen.monitor.utils;

import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class OkHttpClientUtil {

    /**
     * 获取忽略证书的OkHttpClient，用于访问https
     */
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient();
    }

    /**
     * 发送请求并格式化返回的结果
     *
     * @param url     请求地址
     * @param data    请求数据
     * @param headers 请求头
     * @param clsOfT  返回类型
     * @param <T>     返回数据类型泛型
     * @param <E>     请求数据类型泛型
     * @return 返回数据, 如果请求失败或无返回, 返回null
     */
    public static <T, E> T post(String url, E data, Map<String, String> headers, Class<T> clsOfT) {
        try {
            Request.Builder rb = new Request.Builder().url(url).post(
                    RequestBody.create(MediaType.parse("application/json"), JsonUtil.toJson(data))
            );
            if (headers != null && !headers.isEmpty()) {
                Headers.Builder hb = new Headers.Builder();
                headers.forEach(hb::add);
                rb.headers(hb.build());
            }
            Request request = rb.build();
            Response response = getUnsafeOkHttpClient().newCall(request).execute();
            return processResp(response, clsOfT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 发送请求
     *
     * @param url    请求地址
     * @param clsOfT 返回类型
     * @param <T>    返回数据类型泛型
     * @return 返回数据, 如果请求失败或无返回, 返回null
     */
    public static <T> T get(String url, Class<T> clsOfT) {
        try {
            Response response = getUnsafeOkHttpClient().newCall(new Request.Builder().url(url).get().build()).execute();
            return processResp(response, clsOfT);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 发送请求
     *
     * @param url     请求地址
     * @param typeOfT 返回类型
     * @param <T>     返回数据类型泛型
     * @return 返回数据, 如果请求失败或无返回, 返回null
     */
    public static <T> T get(String url, Map<String, String> headers, Type typeOfT) {
        try {
            Request.Builder rb = new Request.Builder().url(url).get();
            if (headers != null) {
                headers.forEach(rb::addHeader);
            }
            Response response = getUnsafeOkHttpClient().newCall(rb.build()).execute();
            return processResp(response, typeOfT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理接口返回结果
     *
     * @param response 接口返回
     * @param clsOfT   要处理成的数据类型
     * @param <T>      要处理成的数据泛型
     * @return 要处理成的数据
     */
    private static <T> T processResp(Response response, Class<T> clsOfT) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
            return null;
        }
        String resp = response.body().string();
        return JsonUtil.fromJson(resp, clsOfT);
    }

    private static <T> T processResp(Response response, Type typeOfT) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
            return null;
        }
        String resp = response.body().string();
        return JsonUtil.fromJson(resp, typeOfT);
    }

}
