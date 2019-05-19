package com.appjishu.starzone.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

public class ActivityUtil {
    /**
     * 判断某个Activity 界面是否在前台
     * @param context
     * @param className 某个界面名称
     * @return
     */
    public static boolean  isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = null;
        if (am != null) {
            list = am.getRunningTasks(1);
        }
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
