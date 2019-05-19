package com.appjishu.starzone.util;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;

import com.appjishu.starzone.app.MyApp;
import com.appjishu.starzone.constant.StorageConstant;
import com.appjishu.starzone.model.LoginResult;
import com.razerdp.github.com.common.entity.UserInfo;
import com.xiaomi.mipush.sdk.MiPushClient;

public class SharedPreferencesMyUtil {
    private static final String MY_TAG = "SharedPrefUtil";

    public static void storeToken(ContextWrapper contextWrapper, LoginResult loginResult) {
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_PASSPORT, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("userId", loginResult.getUserId());
        editor.putString("mobile", loginResult.getUsername());
        editor.putString("token", loginResult.getToken());
        editor.commit();
        if (loginResult.getUserId() > 0) {
            MiPushClient.setAlias(MyApp.getInstance(), loginResult.getUserId() + "", null);
            Log.i(MY_TAG, "MiPushClient_setAlias:alias=" + loginResult.getUserId() + "");
        }
    }

    public static LoginResult queryToken(ContextWrapper contextWrapper) {
        if (contextWrapper == null) {
            return null;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_PASSPORT, 0);
        long userId = sp.getLong("userId", 0);
        String mobile = sp.getString("mobile", null);
        String token = sp.getString("token", null);
        LoginResult loginResult = new LoginResult();
        loginResult.setUserId(userId);
        loginResult.setToken(token);
        loginResult.setUsername(mobile);
        return loginResult;
    }

    /**
     * @param contextWrapper
     * @param headFilePath   头像图片本地文件的绝对路径
     */
    public static void storeHead(ContextWrapper contextWrapper, String headFilePath) {
        if (contextWrapper == null) {
            return;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_STORAGE
                , 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("headFilePath", headFilePath);
        editor.commit();
    }

    public static String queryHead(ContextWrapper contextWrapper) {
        if (contextWrapper == null) {
            return null;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_STORAGE
                , 0);
        return sp.getString("headFilePath", null);
    }

    public static void storeHeadVersion(ContextWrapper contextWrapper, long headVersion) {
        if (contextWrapper == null) {
            return;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_STORAGE
                , 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("headVersion", headVersion);
        editor.commit();
    }

    public static Long queryHeadVersion(ContextWrapper contextWrapper) {
        if (contextWrapper == null) {
            return null;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_STORAGE
                , 0);
        return sp.getLong("headVersion", 0);
    }

    public static void storeUserInfo(ContextWrapper contextWrapper, UserInfo userInfo) {
        if (contextWrapper == null) {
            return;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_USER_INFO
                , 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("headImg", userInfo.getHeadImg());
        editor.putString("cover", userInfo.getCover());
        editor.putString("nickname", userInfo.getNickname());
        editor.commit();
    }

    public static UserInfo queryUserInfo(ContextWrapper contextWrapper) {
        if (contextWrapper == null) {
            return null;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_FILE_USER_INFO
                , 0);
        String headImg = sp.getString("headImg", "");
        String cover = sp.getString("cover", "");
        String nickname = sp.getString("nickname", "");

        UserInfo userInfo = new UserInfo();
        userInfo.setHeadImg(headImg);
        userInfo.setCover(cover);
        userInfo.setNickname(nickname);
        userInfo.setUsername(nickname);
        LoginResult loginResult = queryToken(contextWrapper);
        if (loginResult != null) {
            userInfo.setId(loginResult.getUserId());
        }
        return userInfo;
    }

    public static void storeMiRegid(ContextWrapper contextWrapper, String regid) {
        if (contextWrapper == null) {
            return;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_MIPUSH
                , 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("regid", regid);
        editor.commit();
    }

    public static String queryMiRegid(ContextWrapper contextWrapper) {
        if (contextWrapper == null) {
            return null;
        }
        SharedPreferences sp = contextWrapper.getSharedPreferences(StorageConstant.SHARED_PREFERENCES_NAME_MIPUSH
                , 0);
        String regid = sp.getString("regid", "");
        return regid;
    }
}
