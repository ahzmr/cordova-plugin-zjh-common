package com.zhijianhuo.cordova.core.util;

import android.os.Build;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wenin819@gmail.com on 16/6/23.
 */
public class HttpUtils {

    private static void disableConnectionReuseIfNecessary() {
        // 这是一个2.2版本之前的bug
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * 得到默认http连接
     * @param url 请求地址
     * @param requestMethod 请求方法
     * @return http连接
     * @throws IOException
     */
    public static HttpURLConnection getDefaultConn(URL url, String requestMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 关闭 keepAlive
        disableConnectionReuseIfNecessary();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestMethod(requestMethod);
        return connection;
    }

    /**
     * 得到默认http连接
     * @param url 请求地址
     * @param requestMethod 请求方法
     * @return http连接
     * @throws IOException
     */
    public static HttpURLConnection getDefaultConn(String url, String requestMethod) throws IOException {
        return getDefaultConn(new URL(url), requestMethod);
    }

    /**
     * 得到html页面内容
     * @param url 请求地址
     * @return 页面内容
     * @throws IOException
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(getDefaultConn(url, "GET"));
    }
    public static String getHtml(HttpURLConnection conn) throws IOException {
        Assert.assertNotNull("参数conn不能为空", conn);
        InputStream inputStream = null;
        try {
            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    return getHtml(conn.getHeaderField("Location"));
            }
            inputStream = conn.getInputStream();
            return IOUtils.toString(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            if(null != conn) {
                conn.disconnect();
            }
        }
    }
}
