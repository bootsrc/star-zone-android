/*Copyright ©2015 TommyLemon(https://github.com/flylib)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package com.appjishu.starzone.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.model.IntroduceVO;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.SaveImageResult;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.APKVersionCodeUtil;
import com.appjishu.starzone.util.CommonUtil;
import com.appjishu.starzone.util.ImgUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

/**
 * 关于界面
 *
 * @author liushaoming
 */
public class AboutActivity extends BaseTitleBarActivity implements OnClickListener, OnLongClickListener,
        EasyPermissions.PermissionCallbacks {
    private static final String MY_TAG = "AboutActivity";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private String introduceContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initView();
        initData();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private void checkLoginStatus() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(this);
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(this, LoginActivity.class);
            startActivity(intentObj);
        } else{
            userId = loginResult.getUserId();
            token = loginResult.getToken();
        }
    }

    private TextView appInfoTextView;

    private void initView() {
        setTitle(getString(R.string.about_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        appInfoTextView = findViewById(R.id.app_info_text_view);
    }

    private void initData() {
        String appInfoStr = getString(R.string.app_name) + APKVersionCodeUtil.getVerName(this);
        appInfoTextView.setText(appInfoStr);
//		setQRCode();
//        httpClient = new OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(10,TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
        httpClient = MySslUtil.newOkHttpClient();
    }

    private Bitmap qRCodeBitmap;

    /**
     * 下载支付图片
     */
    private void downloadAlipayImage() {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                requestPermission();
            }
        });
    }

    private void initEvent() {
        findViewById(R.id.about_app_linear_layout).setOnLongClickListener(this);
        findViewById(R.id.about_app_linear_layout).setOnClickListener(this);
        findViewById(R.id.about_email_linear_layout).setOnLongClickListener(this);
        findViewById(R.id.about_share_linear_layout).setOnClickListener(this);
        findViewById(R.id.feedback_wechat_linear_layout).setOnLongClickListener(this);
    }

    private static final int REQUEST_CODE_SAVE_IMG = 10;

    /**
     * 请求读取sd卡的权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //读取sd卡的权限
            String[] mPermissionList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, mPermissionList)) {
                //已经同意过
                saveImage();
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        this,  //上下文
                        "保存图片需要读取sd卡的权限", //提示文言
                        REQUEST_CODE_SAVE_IMG, //请求码
                        mPermissionList //权限列表
                );
            }
        } else {
            saveImage();
        }
    }


    //保存图片
    private void saveImage() {
        Bitmap x;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alipay_receive);
        SaveImageResult saveImageResult = ImgUtil.saveImageToGallery(this, bitmap);
        boolean isSaveSuccess = saveImageResult.isSuccess();
        if (isSaveSuccess) {
            Toast.makeText(this, "保存成功，请打开支付宝/微信识别二维码", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "保存图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    //授权结果，分发下去
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        //跳转到onPermissionsGranted或者onPermissionsDenied去回调授权结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //同意授权
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Log.i(MY_TAG, "onPermissionsGranted:" + requestCode + ":" + list.size());
        saveImage();
    }

    //拒绝授权
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i(MY_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //打开系统设置，手动授权
            Toast.makeText(this, SystemConstant.PERMISSION_SETTINGS_TIP
            , Toast.LENGTH_SHORT).show();
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            //拒绝授权后，从系统设置了授权后，返回APP进行相应的操作
            Log.i(MY_TAG, "onPermissionsDenied:------>自定义设置授权后返回APP");
            saveImage();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_app_linear_layout:
                Intent aboutAppIntent = new Intent(this, AboutAppActivity.class);
                startActivity(aboutAppIntent);
                break;
            case R.id.about_email_linear_layout:
                CommonUtil.sendEmail(this, NetConstant.APP_OFFICIAL_EMAIL);
                break;
            case R.id.about_share_linear_layout:
                shareApp();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
//            case R.id.about_developer_linear_layout:
//                CommonUtil.copyText(this, NetConstant.APP_DEVELOPER_WEBSITE);
//                return true;
            case R.id.about_app_linear_layout:
                CommonUtil.copyText(this, NetConstant.APP_OFFICIAL_WEBSITE);
                return true;
            case R.id.about_email_linear_layout:
                CommonUtil.copyText(this, NetConstant.APP_OFFICIAL_EMAIL);
                return true;
//            case R.id.about_wechat_linear_layout:
//                CommonUtil.copyText(this, NetConstant.WECHAT_PUBLIC_NAME);
//                return true;
            case R.id.feedback_wechat_linear_layout:
                CommonUtil.copyText(this, getString(R.string.contact_wechat_value));
                return true;
            default:
                break;
        }
        return false;
    }

    private void shareApp(){
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
                        CommonUtil.shareInfo(AboutActivity.this, NetConstant.APP_INTRODUCE);
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
                            CommonUtil.shareInfo(AboutActivity.this, introduceContent);
                        }
                    });
                }
            }
        });
    }
}
