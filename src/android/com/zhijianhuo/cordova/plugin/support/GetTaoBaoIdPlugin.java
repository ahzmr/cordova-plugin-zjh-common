package com.zhijianhuo.cordova.plugin.support;

import com.zhijianhuo.cordova.core.demain.SimpleResult;
import com.zhijianhuo.cordova.core.util.HttpUtils;

import java.io.IOException;

/**
 * Created by wenin819@gmail.com on 16/6/23.
 */
public class GetTaoBaoIdPlugin {
    private static final String ItemUrlPref = "a.m.taobao.com/i";
    private static final String TMItemUrlPref = "a.m.tmall.com/i";

    public SimpleResult<String> getTBId(String url) {
        if(null == url || url.indexOf("http") != 0) {
            return SimpleResult.newCommFailResult("商品链接不合法");
        }
        String html;
        try {
            html = HttpUtils.getHtml(url);
            String id = getIdForItemUrl(html);
            if(null != id) {
                return SimpleResult.newSuccessResult(id);
            } else {
                return SimpleResult.newCommFailResult("获取商品ID失败");
            }
        } catch (IOException e) {
            return SimpleResult.newCommFailResult("打开商品链接失败");
        }
    }

    private String getIdForItemUrl(String html) {
        if(null == html) {
            return null;
        }
        int start = html.indexOf(ItemUrlPref);
        if(start > -1) {
            int end = html.indexOf(".htm", start);
            return start > -1 && end > -1 ? html.substring(start + ItemUrlPref.length(), end) : null;
        }
        start = html.indexOf(TMItemUrlPref);
        if(start > -1) {
            int end = html.indexOf(".htm", start);
            return start > -1 && end > -1 ? (html.substring(start + TMItemUrlPref.length(), end) + " ") : null;
        }

        html = html.replaceAll("[ \\t]+", "");
        String varStart = "varurl='";
        start =  html.indexOf(varStart);
        if(start > -1) {
            start += varStart.length();
            int end = html.indexOf("'", start);
            String url = html.substring(start, end);
            String sub = html.contains("tmall") ? " " : "";

            String paramsStr = url.split("\\?")[1];
            String[] split = paramsStr.split("&");
            for (String s : split) {
                String[] keyVal = s.split("=");
                if("id".equals(keyVal[0])) {
                    return keyVal[1] + sub;
                }
            }
        }
        return null;
    }
}
