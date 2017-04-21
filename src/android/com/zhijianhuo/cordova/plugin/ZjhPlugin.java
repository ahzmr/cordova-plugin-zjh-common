package com.zhijianhuo.cordova.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.ClipboardManager;
import android.webkit.CookieManager;

import com.zhijianhuo.cordova.core.demain.SimpleResult;
import com.zhijianhuo.cordova.core.plugin.BasePlugin;
import com.zhijianhuo.cordova.plugin.support.GetTaoBaoIdPlugin;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 淘客原生接口
 * Created by wenin on 2016/12/23.
 */

public class ZjhPlugin extends BasePlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getAuthInfo".equals(action)) {
            String url = args.optString(0);
            String cookies = CookieManager.getInstance().getCookie(url);
            callbackContext.success(cookies);
        } else if ("systemCopy".equals(action)) {
            String keyword = args.optString(0);
            ClipboardManager clip = (ClipboardManager) cordova.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(keyword); //复制到剪切板
            callbackContext.success();
        } else if ("systemPaste".equals(action)) {
            ClipboardManager clipboard = (ClipboardManager) cordova.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            CharSequence text = clipboard.hasText() ? clipboard.getText() : "";
            callbackContext.success(text.toString());
        } else if (action.equals("openApp")) {
            String pkg = args.getString(0);
            try {
                Intent intent = null;
                intent = cordova.getActivity().getPackageManager().getLaunchIntentForPackage(pkg);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                cordova.getActivity().startActivity(intent);
                callbackContext.success();
            } catch (Exception e) {
                callbackContext.error("尚未安装对应客户端");
            }
        } else if (action.equals("getPackageInfo")) {
            try {
                String appId = cordova.getActivity().getApplication().getPackageName();
                PackageInfo packageInfo = cordova.getActivity().getPackageManager().getPackageInfo(
                        appId, 0);
                if (null != packageInfo) {
                    JSONObject rst = new JSONObject();
                    rst.put("appId", appId);
                    rst.put("versionName", packageInfo.versionName);
                    rst.put("versionCode", packageInfo.versionCode);
                    callbackContext.success(rst);
                }
            } catch (Exception e) {
                String msg = "获取应用版本信息异常，错误信息为：" + e.getMessage();
                callbackContext.error(msg);
            }
        } else if ("getTbIdForUrl".equals(action)) {
            SimpleResult<String> result = new GetTaoBaoIdPlugin().getTBId(args.optString(0));
            returnForSimpleResult(callbackContext, result);
        } else {
            return false;
        }
        return true;
    }
}
