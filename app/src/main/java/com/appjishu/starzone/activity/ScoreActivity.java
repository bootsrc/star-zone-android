package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.ResponseData;
import com.appjishu.starzone.model.UserScore;
import com.appjishu.starzone.singleton.GsonSingleton;
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

public class ScoreActivity extends BaseTitleBarActivity {

    private static final String MY_TAG = "ScoreActivity";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private ScoreActivity.MyHandler myHandler;

    private TextView scoreView;
    private TextView checkInCountView;

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        initData();
        initView();
        initEvent();
    }

    private void initData() {
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new ScoreActivity.MyHandler();
        firstGetProfile();
    }

    private void initView() {
        setTitle(getString(R.string.score_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        scoreView = findViewById(R.id.score_field);
        checkInCountView = findViewById(R.id.check_in_count_field);
    }

    private void initEvent() {
        loadScore();
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

    private void loadScore() {
        String url = NetConstant.URL_BASE_HTTPS + NetConstant.GET_SCORE;
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
                        UserScore userScore = GsonSingleton.getInstance().getGson()
                                .fromJson(responseStr, UserScore.class);

                        if (userScore != null) {
                            int scoreResult = userScore.getScore();
                            final int checkInCountResult = userScore.getCheckInCount();
                            final String scoreStr = scoreResult + "";
                            final String checkInCountStr = checkInCountResult + "";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scoreView.setText(scoreStr);
                                    checkInCountView.setText(checkInCountStr);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
        }
    }
}
