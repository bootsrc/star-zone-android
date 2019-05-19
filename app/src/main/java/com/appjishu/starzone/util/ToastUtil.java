package com.appjishu.starzone.util;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class ToastUtil {

    public static void showToast(final Activity activity, final String message) {
        if ("main".equals(Thread.currentThread().getName())) {
            Log.e("ToastUtil", "在主线程");
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("ToastUtil", "不在主线程");
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
