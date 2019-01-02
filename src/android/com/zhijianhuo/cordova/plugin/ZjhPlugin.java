package com.zhijianhuo.cordova.plugin;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.zhijianhuo.cordova.core.demain.SimpleResult;
import com.zhijianhuo.cordova.core.plugin.BasePlugin;
import com.zhijianhuo.cordova.plugin.support.GetTaoBaoIdPlugin;
import com.zhijianhuo.cordova.plugin.support.SaveImagesPlugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

/**
 * 淘客原生接口
 * Created by wenin on 2016/12/23.
 */

public class ZjhPlugin extends BasePlugin {
    public static final int REQUEST_PERMISS_CODE = 819;
    public static final int REQUEST_PERMISS_SAVE_IMAGE_CODE = 8191;
    public static String IMAGE_DIR_NAME = null;

    private static final String LOG_TAG = "ZjhPlugin";
    private CallbackContext callbackContext;
    private JSONArray args;
    private String action;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        String imageDirName = preferences.getString("IMAGE_DIR_NAME", null);
        if(!TextUtils.isEmpty(imageDirName)) {
            IMAGE_DIR_NAME = imageDirName;
        }
    }

    private CookieManager getCookieManager() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(cordova.getActivity());
        }
        return CookieManager.getInstance();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.args = args;
        this.action = action;
        if ("getAuthInfo".equals(action) || "getCookies".equals(action)) {
            String url = args.optString(0);
            if (null == url || url.isEmpty()) {
                url = "alimama.com";
            }
            String cookies = getCookieManager().getCookie(url);
            callbackContext.success(cookies);
        } else if ("setCookies".equals(action)) {
            String url = args.optString(0);
            String cookieStr = args.optString(1);
            CookieManager cookieManager = getCookieManager();
            if (null == url || url.isEmpty()) {
                url = "alimama.com";
            }
            if(null != cookieStr && !cookieStr.isEmpty()) {
                String[] split = cookieStr.split("\n");
                for (String s : split) {
                    cookieManager.setCookie(url, s);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().flush();
                } else {
                    CookieSyncManager.getInstance().sync();
                }
            }
            callbackContext.success(cookieManager.getCookie(url));
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
                PackageInfo packageInfo = cordova.getActivity().getPackageManager()
                        .getPackageInfo(appId, 0);
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
        } else if("saveImages".equals(action)) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if(!hasPermisssion(permissions)) {
                requestPermissions(REQUEST_PERMISS_SAVE_IMAGE_CODE, permissions);
                return true;
            }
            SimpleResult<List<String>> result = SaveImagesPlugin.saveImages(cordova.getActivity().getApplication(), args.getJSONArray(0));
            returnForSimpleResult(callbackContext, result);
        } else if("toSelfAction".equals(action)) {
            SimpleResult<Object> result = toSelfAction(cordova.getActivity(), args.optString(0));
            returnForSimpleResult(callbackContext, result);
        } else if("hasPermisssion".equals(action)) {
            if(hasPermisssion(getPermisssions(args.optJSONArray(0)))) {
                callbackContext.success();
            } else {
                callbackContext.error("Permission Denied!");
            }
        } else if("requestPermissions".equals(action)) {
            requestPermissions(REQUEST_PERMISS_CODE, getPermisssions(args.optJSONArray(0)));
        } else {
            return false;
        }
        return true;
    }

    private SimpleResult<Object> toSelfAction(Activity activity, String action) {
        if (TextUtils.isEmpty(action)) {
            return SimpleResult.newCommFailResult("参数action不能为空");
        }
        String appId = activity.getApplication().getPackageName();
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if ("setting".equals(action)) {
            mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        } else if ("delete".equals(action)) {
            mIntent.setAction(Intent.ACTION_DELETE);
        } else {
            mIntent.setAction(action.indexOf('.') > -1 ? action : "android.intent.action." + action);
        }
        mIntent.setData(Uri.fromParts("package", appId, null));
        activity.startActivity(mIntent);
        return SimpleResult.newSuccessResult(null);
    }

    private String[] getPermisssions(JSONArray array) {
        List<String> list = new ArrayList<>();
        if(null != array) {
            for (int i = 0; i < array.length(); i++) {
                String val = array.optString(i, null);
                if (null != val) {
                    list.add(val);
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * check application's permissions
     */
    protected boolean hasPermisssion(String[] permissions) {
        for(String p : permissions) {
            if(!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     *
     * @param requestCode The code to get request action
     */
    protected void requestPermissions(int requestCode, String[] permissions) {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }

    /**
     * processes the result of permission request
     *
     * @param requestCode The code to get request action
     * @param permissions The collection of permissions
     * @param grantResults The result of grant
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        switch (requestCode) {
            case REQUEST_PERMISS_CODE:
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.error("Permission Denied!");
                return;
            }
        }
        this.callbackContext.success(new JSONArray(Arrays.asList(permissions)));
                break;
            case REQUEST_PERMISS_SAVE_IMAGE_CODE:
                if(null != callbackContext && null != args) {
                    this.cordova.getThreadPool().submit(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                execute(action, args, callbackContext);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                break;
        }
    }
}
