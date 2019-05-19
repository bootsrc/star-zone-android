package com.appjishu.starzone.util;

import android.text.TextUtils;
import android.util.Log;

import com.appjishu.starzone.activity.LoginActivity;
import com.appjishu.starzone.app.MyApp;
import com.appjishu.starzone.constant.AuthResponseCode;
import com.appjishu.starzone.model.LoginResult;
import com.google.gson.Gson;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;

public class MiPushUtil {
    private static final String MY_TAG = "MiPushUtil";

    public static void bindRegid() {
        String miRegid = SharedPreferencesMyUtil.queryMiRegid(MyApp.getInstance());
        Log.i(MyApp.TAG, "LoginActivity_storedRegid=" + miRegid);
        if (!TextUtils.isEmpty(miRegid)){
            LoginResult loginResult = SharedPreferencesMyUtil.queryToken(MyApp.getInstance());

            OkHttpClient client = MySslUtil.newOkHttpClient();

            FormBody formBody = new FormBody
                    .Builder()
                    .add("miRegid", miRegid)
                    .build();
            final String url = NetConstant.URL_BASE_HTTPS + NetConstant.PUSH_BIND_REGID;
            if (loginResult.getUserId() > 0 && !TextUtils.isEmpty(loginResult.getToken())) {
                final Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .addHeader("userId", loginResult.getUserId() + "")
                        .addHeader("token", loginResult.getToken())
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(MY_TAG, "BindRegid_failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(MY_TAG, "BindRegid_success");
                    }
                });
            }
        }
    }
}
