package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.fragment.MsgFragment;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.ResponseData;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.ui.tool.LoadMoreDelegate;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class CheckInActivity extends BaseTitleBarActivity {

    private static final String MY_TAG = "CheckInActivity";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private CheckInActivity.MyHandler myHandler;

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        initData();
        initView();
        initEvent();
    }

    private void initData() {//必须调用
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new CheckInActivity.MyHandler();
        firstGetProfile();
    }

    private void initEvent() {
    }

    private void initView() {
        setTitle(getString(R.string.check_in_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        checkIn();
    }

    private void firstGetProfile() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
            startActivity(intentObj);
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();
        }
    }

    private void checkIn() {
        String url = NetConstant.URL_BASE_HTTPS + NetConstant.CHECK_IN;
        FormBody formBody = new FormBody.Builder()
                .build();
        Request request = new Request.Builder().url(url)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .post(formBody).build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                if (responseCode == 200) {
                    String responseStr = response.body().string();
                    if (!TextUtils.isEmpty(responseStr) && !"null".equals(responseStr)) {
                        ResponseData responseData = GsonSingleton.getInstance().getGson()
                                .fromJson(responseStr, ResponseData.class);

                        if (responseData != null) {
                            String dataStr = (String) responseData.getData();
                            int resCode = Integer.valueOf(dataStr);
                            showCheckInResult(resCode);
                        }
                    }
                }
            }
        });
    }

    private void showCheckInResult(int resCode) {
        switch (resCode) {
            case 0:
                String text = "签到成功";
                toastInUI(text);
                break;
            case 2:
                String text1 = "签到失败，请稍后重试";
                toastInUI(text1);
                break;
            case 1:
                // 已经签到，无需提示
                break;
        }
    }

    private void toastInUI(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CheckInActivity.this, text,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        }
    }
}
