package com.appjishu.starzone.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.model.SaveImageResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 周旭 on 2017/4/9.
 */

public class ImgUtil {
    //保存文件到指定路径
    public static SaveImageResult saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + SystemConstant.PHOTOS_SAVED_PATH;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {

                SaveImageResult saveImageResult = new SaveImageResult();
                saveImageResult.setSuccess(true);
                saveImageResult.setAbsolutePath(storePath + "/" + fileName);
                return saveImageResult;
            } else {
                SaveImageResult saveImageResult = new SaveImageResult();
                saveImageResult.setSuccess(false);
                saveImageResult.setAbsolutePath(null);
                return saveImageResult;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SaveImageResult saveImageResult = new SaveImageResult();
        saveImageResult.setSuccess(false);
        saveImageResult.setAbsolutePath(null);
        return saveImageResult;
    }

    public static boolean isHeadImgValid(String headImg) {
        if (!TextUtils.isEmpty(headImg)) {
            if (headImg.startsWith("/star-sign/")
                    || headImg.startsWith("/star-sign-file/mobile/default-img/")) {
                return true;
            }
        }
        return false;
    }
}
