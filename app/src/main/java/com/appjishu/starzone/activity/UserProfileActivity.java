package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.StarSignConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.UserProfileDetail;
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
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.widget.common.TitleBar;

public class UserProfileActivity extends BaseTitleBarActivity implements View.OnClickListener {

    private static final String MY_TAG = "UserProfileAct";
    public static final String INTENT_DATA_KEY_USER_ID = "INTENT_DATA_KEY_USER_ID";
    private static final int MSG_WHAT_LOAD_PROFILE = 0x01;
    private static final String KEY_LOAD_PROFILE = "KEY_LOAD_PROFILE";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private MyHandler myHandler;

    private long targetUserId;

    // UI Control
    private ImageView headImageView;
    private TextView nicknameView;
    private TextView sexView;
    private TextView starView;
    private TextView ageView;
    private TextView phoneView;
    private Button closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initView();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onHandleIntent(Intent intent) {
        targetUserId = intent.getLongExtra(INTENT_DATA_KEY_USER_ID, targetUserId);
        if (targetUserId < 1) {
            finishWithError("对方用户不存在！");
            return;
        }
    }

    @Override
    public void onTitleRightClick(){
        finish();
    }

    private void initView() {
        setTitle(getString(R.string.user_profile_title));
        setTitleMode(TitleBar.MODE_BOTH);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);
        setTitleRightIcon(R.drawable.ic_close_white_24dp);

        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new MyHandler();
        checkLoginStatus();

        createControl();
        loadProfile();
    }

    private void createControl() {
        headImageView = findViewById(R.id.profile_my_head_image_view);
        nicknameView = findViewById(R.id.nickname_field);
        sexView = findViewById(R.id.profile_sex_field);
        starView = findViewById(R.id.star_field);
        ageView = findViewById(R.id.age_field);
        phoneView = findViewById(R.id.phone_field);
        closeBtn = findViewById(R.id.profile_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void finishWithError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void checkLoginStatus() {
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

    private void loadProfile() {
        FormBody formBody = new FormBody.Builder()
                .add("otherUserId", targetUserId + "")
                .build();

        Request request = new Request.Builder()
                .url(NetConstant.URL_BASE_HTTPS + NetConstant.OTHER_PROFILE_DETAIL)
                .post(formBody)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .build();

        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    String dataStr = response.body().string();
                    if (!TextUtils.isEmpty(dataStr)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_LOAD_PROFILE, dataStr);
                        Message msg = new Message();
                        msg.what = MSG_WHAT_LOAD_PROFILE;
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    } else {

                    }
                } else {

                }
            }
        });
    }

    private void renderView(String dataStr) {
        UserProfileDetail detail = GsonSingleton.getInstance().getGson().fromJson(dataStr, UserProfileDetail.class);
        if (detail != null) {
            String headImg = detail.getHeadImg();
            if (!TextUtils.isEmpty(headImg)) {
                ImageLoadMnanger.INSTANCE.loadImage(headImageView,
                        NetConstant.RESOURCES_BASE + headImg);
            }

            if (!TextUtils.isEmpty(detail.getNickname())) {
                nicknameView.setText(detail.getNickname());
            }

            String sexText = detail.getSex() == 1 ? "男" : "女";
            sexView.setText(sexText);

            String starSignText = StarSignConstant.arrText[detail.getStarSign()] + "座";
            starView.setText(starSignText);

            if (detail.getAge() > 0) {
                String ageStr = detail.getAge() + "";
                ageView.setText(ageStr);
            }
            phoneView.setText(detail.getMobile());
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_LOAD_PROFILE:
                    String dataStr = msg.getData().getString(KEY_LOAD_PROFILE);
                    renderView(dataStr);
                    break;
            }
        }
    }
}
