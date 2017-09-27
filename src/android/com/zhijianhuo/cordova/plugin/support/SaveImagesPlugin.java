package com.zhijianhuo.cordova.plugin.support;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.zhijianhuo.cordova.core.demain.SimpleResult;
import com.zhijianhuo.cordova.core.util.HttpUtils;
import com.zhijianhuo.cordova.core.util.IOUtils;
import com.zhijianhuo.cordova.plugin.ZjhPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wenin819@gmail.com on 16/8/29.
 */
public class SaveImagesPlugin {

    public static SimpleResult<List<String>> saveImages(Application application, JSONArray files) {
        List<String> list = new ArrayList<String>();
        if(null != files) {
            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.optJSONObject(i);
                if(null == file) {
                    continue;
                }
                String fileName = file.optString("fileName", null);
                String url = file.optString("url");
                list.add(saveImage(application, url, fileName));
            }
        }
        return SimpleResult.newSuccessResult(list);
    }

    private static String saveImage(Application application, String url, String fileName) {
        if(TextUtils.isEmpty(url)) {
            return null;
        }

        InputStream inputStream = null;
        BufferedOutputStream output = null;
                HttpURLConnection get = null;
        try {
            if(url.startsWith("http")) {
                get = HttpUtils.getDefaultConn(url, "GET");
                inputStream = get.getInputStream();
                if(null == fileName) {
                    fileName = getFileName(url);
                }
            } else if (url.startsWith("data:image")) {  // base64 image
                String imageDataBytes = url.substring(url.indexOf(",") + 1);
                byte imageBytes[] = Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT);
                inputStream = new ByteArrayInputStream(imageBytes);
            }

            if(null != inputStream) {

                File filesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                if(null != ZjhPlugin.IMAGE_DIR_NAME) {
                    filesDir = new File(Environment.getExternalStorageDirectory(), ZjhPlugin.IMAGE_DIR_NAME);
                }
                if(TextUtils.isEmpty(fileName)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmssSSS");
                    fileName = "SaveImage_" + dateFormat.format(new Date()) + ".png";
                }
                File image = new File(filesDir, fileName);

                if(!image.getParentFile().exists()) {
                    image.getParentFile().mkdirs();
                }
                output = new BufferedOutputStream(new FileOutputStream(image));
                IOUtils.copy(inputStream, output);
                output.flush();
                url = image.getAbsolutePath();
                String path = formatPath(url);
                noticeImage(application, path);
                return path;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(output);
            if(null != get) {
                get.disconnect();
            }
        }
        return formatPath(url);
    }

    /**
     * 通知图片更新
     * @param application
     * @param path 图片路径
     */
    private static void noticeImage(Application application, String path) {
        path = formatPath(path);
        if(null == path) {
            return;
        }
        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(MainApplication.getApp().getContentResolver(),
//                    path, getFileName(path), null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        application.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + path)));
    }

    private static String getFileName(String filePath) {
        if(TextUtils.isEmpty(filePath)) {
            return null;
        }
        int startIdx = filePath.lastIndexOf("/");
        int endIdx = filePath.indexOf("?");
        String fileName = filePath.substring(startIdx == -1 ? 0 : startIdx + 1, endIdx == -1 ? filePath.length() : endIdx);
        return !fileName.contains(".") ? (fileName + ".jpg") : fileName;
    }

    private static String formatPath(String path) {
        return null != path && path.startsWith("file://") ? path.replace("file://", "") : path;
    }

}
