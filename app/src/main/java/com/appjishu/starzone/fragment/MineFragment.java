package com.appjishu.starzone.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.AboutActivity;
import com.appjishu.starzone.activity.AddProfileActivity;
import com.appjishu.starzone.activity.CheckInActivity;
import com.appjishu.starzone.activity.LoginActivity;
import com.appjishu.starzone.activity.SettingsActivity;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.constant.TextConstant;
import com.appjishu.starzone.model.UserProfileDetail;
import com.appjishu.starzone.util.NetworkStateUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.razerdp.github.com.common.constant.NetConstant;
import com.appjishu.starzone.model.IntroduceVO;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.CommonUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;

public class MineFragment extends Fragment
        implements View.OnClickListener,
        DialogInterface.OnClickListener {
    private static final String MY_TAG = "MineFragment";
    private static final String TAG = "MineFragment";

    private long userId;
    private String token;
    private OkHttpClient httpClient;
    private UserProfileDetail userProfileDetail;
    private String appDownloadWebsite;
    private String upgradeMsg;
    private ImageView mImageView;
    private TextView loginStatusView;
    private Button editProfileBtn;
    private Button loginBtn;
    private LinearLayout settingLayout;
    private ProgressWheel progressWheel;
    private static final int MSG_WHAT_SHOW_NETWORK_IMAGE = 0x01;
    private MyHandler myHandler;
    private static final int ACTIVITY_REQUEST_CODE_ADD_PROFILE = 1;
    private LinearLayout shareLinearLayout;
    private String introduceContent = null;
    private TextView scoreView;
    private TextView checkInCountView;
    private Button gotoCheckInBtn;

    public UserProfileDetail getUserProfileDetail() {
        return userProfileDetail;
    }

    public void setUserProfileDetail(UserProfileDetail userProfileDetail) {
        this.userProfileDetail = userProfileDetail;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        myHandler = new MyHandler();
        mImageView = view.findViewById(R.id.mine_head_image_view);
        mImageView.setOnClickListener(this);
        loginStatusView = view.findViewById(R.id.login_status_view);
        editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        loginBtn = view.findViewById(R.id.login_btn);
        settingLayout = view.findViewById(R.id.setting_layout);
        scoreView = view.findViewById(R.id.score_field);
        checkInCountView = view.findViewById(R.id.check_in_count_field);
        gotoCheckInBtn=view.findViewById(R.id.goto_check_in_btn);

        initData();
        mImageView.setOnClickListener(this);
        view.findViewById(R.id.setting_layout).setOnClickListener(this);
        view.findViewById(R.id.llSettingAbout).setOnClickListener(this);
        view.findViewById(R.id.upgrade_app_linear_layout).setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        editProfileBtn.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
        progressWheel = view.findViewById(R.id.progress_wheel_fg_mine);
        progressWheel.stopSpinning();

        // 分享
        shareLinearLayout = view.findViewById(R.id.mine_share_linear_layout);
        shareLinearLayout.setOnClickListener(this);

        gotoCheckInBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        checkLoginStatus();
        showLoginStatus();
    }

    public void initData() {//必须调用
//        httpClient = new OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
        httpClient = MySslUtil.newOkHttpClient();
    }

    private void editProfile() {
        Intent intent = new Intent(getActivity(), AddProfileActivity.class);
        startActivity(intent);
    }

    private void showLoginStatus() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(this.getActivity());
        String mobile = loginResult.getUsername();
        if (userId > 0 && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(mobile)) {
            editProfileBtn.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
            doGetProfile(userId, token, mobile);
        } else {
            editProfileBtn.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
            loginStatusView.setText(R.string.login_status_view_title);
        }
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

    private void showHead() {
        String headFilePath = SharedPreferencesMyUtil.queryHead(getActivity());
        if (!TextUtils.isEmpty(headFilePath)) {
            mImageView.setImageURI(Uri.fromFile(new File(headFilePath)));
        }
    }

//    public void onDialogButtonClick(int requestCode, boolean isPositive) {
//        if (!isPositive) {
//            return;
//        }
//
//        switch (requestCode) {
//            case 0:
//                upgradeApp();
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public void onClick(View v) {//直接调用不会显示v被点击效果
        switch (v.getId()) {
            case R.id.setting_layout:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.llSettingAbout:
                Intent intentObj = new Intent(getActivity(), AboutActivity.class);
                startActivity(intentObj);
                break;
            case R.id.upgrade_app_linear_layout:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle("升级App")
                        .setMessage(TextConstant.UPGRADE_INFO)
                        .setPositiveButton("确定", this)
                        .show();
                break;
            case R.id.login_btn:
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.edit_profile_btn:
            case R.id.mine_head_image_view:
                editProfile();
                break;
            case R.id.mine_share_linear_layout:
                shareApp();
                break;
            case R.id.goto_check_in_btn:
                Intent checkInIntent = new Intent(getActivity(), CheckInActivity.class);
                startActivity(checkInIntent);
                break;
            default:
        }
    }

    private void doGetProfile(long userId, String token, final String mobile) {
        if (!checkNetwork()) {
            return;
        }

        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.GET_PROFILE_DETAIL;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
        progressWheel.spin();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWheel.stopSpinning();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWheel.stopSpinning();
                    }
                });
                if (response.code() == 200) {
                    final String responseStr = response.body().string();
                    //
                    if (confirmPassport(responseStr)) {
                        UserProfileDetail userProfileDetail = GsonSingleton.getInstance().getGson().fromJson(responseStr, UserProfileDetail.class);
                        // 如果资料为空，就跳转到完善资料的页面
                        if (userProfileDetail == null || userProfileDetail.getUserId() == 0){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "请提交个人资料", Toast.LENGTH_SHORT)
                                    .show();
                                    Intent intent = new Intent(getActivity(), AddProfileActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }

                        MineFragment.this.setUserProfileDetail(userProfileDetail);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UserProfileDetail thisUserProfileDetail = MineFragment.this.getUserProfileDetail();
                                String defaultNickname = "用户:" + mobile;
                                if (thisUserProfileDetail == null) {
                                    loginStatusView.setText(defaultNickname);
                                } else {
                                    String nicknameResuslt = thisUserProfileDetail.getNickname();
                                    if (TextUtils.isEmpty(nicknameResuslt)) {
                                        loginStatusView.setText(defaultNickname);
                                    } else {
                                        loginStatusView.setText(nicknameResuslt);
                                    }
                                    String scoreText = thisUserProfileDetail.getScore() + "";
                                    scoreView.setText(scoreText);
                                    String checkInCountText = thisUserProfileDetail.getCheckInCount() + "";
                                    checkInCountView.setText(checkInCountText);
                                }
                            }
                        });

                        String headImg = userProfileDetail.getHeadImg();
                        long latestVersion = userProfileDetail.getHeadVersion();
                        String latestHeadImg = userProfileDetail.getHeadImg();
                        if (!TextUtils.isEmpty(latestHeadImg)) {
                            // 把服务器上最新的图片显示到界面上
                            String imgFullUrl = NetConstant.RESOURCES_BASE + latestHeadImg;
                            showNetworkImage(imgFullUrl);
                        }
                    }
                }
            }
        });
    }

    /**
     * 子线程操作,发送消息到Handler
     *
     * @param url
     */
    private void showNetworkImage(String url) {
        Message msg = Message.obtain();
        msg.what = MSG_WHAT_SHOW_NETWORK_IMAGE;
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    private void upgradeApp() {
        if (!checkNetwork()) {
            return;
        }

        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.INTRODUCE_FROM_SERVER;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    IntroduceVO introduceVO = GsonSingleton.getInstance().getGson()
                            .fromJson(response.body().string(), IntroduceVO.class);
                    if (introduceVO != null || !TextUtils.isEmpty(introduceVO.getAppDownloadWebsite())) {
                        appDownloadWebsite = introduceVO.getAppDownloadWebsite();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtil.shareInfo(getActivity(), appDownloadWebsite);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * 如何后台的LoginInterceptor验证失败，Android端就重定向到LoginActivity
     *
     * @param responseStr
     * @return
     */
    private boolean confirmPassport(String responseStr) {
        if (TextUtils.isEmpty(responseStr) || "null".equals(responseStr.toLowerCase())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "请上传头像并完善资料", Toast.LENGTH_SHORT)
                            .show();
                    Intent intentObj = new Intent(getActivity(), AddProfileActivity.class);
                    startActivity(intentObj);
                }
            });
            return false;
        }

        LoginResult loginResultData = GsonSingleton.getInstance().getGson().fromJson(responseStr, LoginResult.class);
        if (loginResultData == null || loginResultData.getCode() == 0) {
            // 当userId+token在后台验证成功的时候，后台返回的是UserProfile的对象的json string
            // 如果一定要把这个string反序列化到LoginResult，就是一个空对象。即除了userId > 0 外,其它的字段都是
            // 默认空值, 这时候code=0,    token==codeDesc==username==""
            return true;
        }
        if (loginResultData != null && loginResultData.getCode() != 0) {
            // 这时候是后台验证时候，需要跳转到LoginActivity
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intentObj = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intentObj);
                }
            });
            return false;
        }
        return true;
    }

    private void shareApp(){
        if (!TextUtils.isEmpty(introduceContent)) {
            CommonUtil.shareInfo(getActivity(), introduceContent);
        }

        if (!checkNetwork()) {
            return;
        }

        FormBody formBody = new FormBody
                .Builder()
                .add("userId", userId + "")
                .add("token", token)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.INTRODUCE_FROM_SERVER;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.shareInfo(getActivity(), NetConstant.APP_INTRODUCE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    IntroduceVO introduceVO = GsonSingleton.getInstance().getGson()
                            .fromJson(response.body().string(), IntroduceVO.class);
                    if (introduceVO == null || TextUtils.isEmpty(introduceVO.getTextContent())){
                        introduceContent = NetConstant.APP_INTRODUCE;
                    } else {
                        introduceContent = introduceVO.getTextContent();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonUtil.shareInfo(getActivity(), introduceContent);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                upgradeApp();
                break;
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_NETWORK_IMAGE:
                    String url = msg.getData().getString("url");
                    RequestOptions requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                            .override(150, 150)
                            .placeholder(R.mipmap.ic_launcher);

                    Glide.with(getActivity()).asBitmap().load(url)
                            .apply(requestOptions)
                            .thumbnail(0.5F)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                            OutputStream outputStream = null;
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                                    mImageView.setImageBitmap(bitmap);
                                }
                            });
                    break;
            }
        }
    }

    private boolean checkNetwork() {
        boolean connected = NetworkStateUtil.isNetworkConnected(getActivity());
        if (!connected) {
            ToastUtil.showToast(getActivity(), SystemConstant.NETWORK_STATE_BAD);
        }
        return connected;
    }
}
