package com.appjishu.starzone.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.appjishu.starzone.activity.ChatActivity;
import com.appjishu.starzone.activity.ChatWindowActivity;
import com.appjishu.starzone.constant.MiSdkConstant;
import com.appjishu.starzone.model.ChatMsg;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

//import cn.bmob.v3.Bmob;
//import cn.bmob.v3.BmobConfig;
import java.util.List;
import java.util.Map;

import razerdp.github.com.lib.api.AppContext;
import razerdp.github.com.lib.manager.localphoto.LocalPhotoManager;

import static com.appjishu.starzone.constant.MiSdkConstant.APP_KEY;

/**
 * Created by liushaoming on 2016/10/26.
 * <p>
 * app
 */

public class MyApp extends Application {
    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "com.appjishu.starzone";

    public static MyApp getInstance() {
        return instance;
    }

    private static MyApp instance;

    private static DemoHandler sHandler = null;
    private static ChatWindowActivity mChatWindowActivity = null;


    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.initARouter();
        initLocalHostInfo();
        LocalPhotoManager.INSTANCE.registerContentObserver(null);

        initMiSdk();
        instance = this;
    }

    private void initLocalHostInfo() {
        LocalHostManager.INSTANCE.init();
    }

    public void initMiSdk() {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit()) {
            MiPushClient.registerPush(this, MiSdkConstant.APP_ID, APP_KEY);
        }
//        MiPushClient.subscribe(this, MiSdkConstant.TOPIC_ALL, null);

        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };

        Logger.setLogger(this, newLogger);

        if (sHandler == null) {
            sHandler = new DemoHandler(getApplicationContext());
        }
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static void reInitPush(Context ctx) {
        MiPushClient.registerPush(ctx.getApplicationContext(),
                MiSdkConstant.APP_ID, MiSdkConstant.APP_KEY);
    }

    public static DemoHandler getHandler() {
        return sHandler;
    }

    public static class DemoHandler extends Handler {

        private Context context;

        public DemoHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {

        }
    }

    public static void setChatWindowActivity(ChatWindowActivity chatWindowActivity) {
        mChatWindowActivity = chatWindowActivity;
    }

    public void setAlias() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(this);
        if (loginResult != null) {
            long storedUserId = loginResult.getUserId();
            if (storedUserId > 0) {
                MiPushClient.setAlias(this, storedUserId + "", null);
            }
        }
    }
}
