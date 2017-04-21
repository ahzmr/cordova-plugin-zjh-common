package com.zhijianhuo.cordova.core.plugin;

import com.zhijianhuo.cordova.core.demain.SimpleResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * 基础Cordova插件类
 * Created by wenin on 2016/12/23.
 */

public class BasePlugin extends CordovaPlugin {

    protected void returnForSimpleResult(CallbackContext callbackContext, SimpleResult simpleResult) {
        if(simpleResult.isSeccess()) {
            Object result = simpleResult.getResult();
            if(result instanceof String) {
                callbackContext.success((String) result);
            } else if(result instanceof JSONArray) {
                callbackContext.success((JSONArray) result);
            } else if(result instanceof JSONObject) {
                callbackContext.success((JSONObject) result);
            } else if(result instanceof Map) {
                callbackContext.success(new JSONObject((Map) result));
            } else if(null != result) {
                callbackContext.error("插件返回结果类型不能识别");
            } else {
                callbackContext.success();
            }
        } else {
            callbackContext.error(simpleResult.getMessage());
        }
    }

}
