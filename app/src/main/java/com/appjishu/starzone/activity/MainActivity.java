package com.appjishu.starzone.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.FragmentCode;
import com.appjishu.starzone.fragment.FindFragment;
import com.appjishu.starzone.fragment.HomeFragment;
import com.appjishu.starzone.fragment.MineFragment;
import com.appjishu.starzone.fragment.MsgFragment;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.UserProfile;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.ui.tool.BottomNavigationViewHelper;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.UserInfoUtil;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.constant.PermissionConstant;
import com.razerdp.github.com.common.entity.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.helper.AppFileHelper;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.widget.common.TitleBar;

public class MainActivity extends BaseTitleBarActivity {
    private HomeFragment homeFragment;
    private FindFragment findFragment;
    private MsgFragment msgFragment;
    private MineFragment mineFragment;
    private FragmentManager fManager;
    private long lastClickBackTime;
    //动态请求权限
//    private static final int REQUEST_PERMISSION_CODE = 1;
    private OkHttpClient httpClient;
    private long userId;
    private String token;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setChioceItem(FragmentCode.HOME);
                    return true;
                case R.id.navigation_find:
                    setChioceItem(FragmentCode.FIND);
                    return true;
                case R.id.navigation_msg:
                    setChioceItem(FragmentCode.MSG);
                    return true;
                case R.id.navigation_mine:
                    setChioceItem(FragmentCode.MINE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("星座空间");
        setTitleMode(TitleBar.MODE_TITLE);

        fManager = getSupportFragmentManager();
        //
        setChioceItem(0);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initView();
        requestWritePermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppFileHelper.initStroagePath(this);
        checkLoginStatus();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastClickBackTime > 2000) { // 后退阻断
            UIHelper.ToastMessage("再按一次退出");
            lastClickBackTime = System.currentTimeMillis();
        } else { // 关掉app
            super.onBackPressed();
        }
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }


    public void setChioceItem(int index) {
        //
        FragmentTransaction transaction = fManager.beginTransaction();
        hideFragments(transaction);
        switch (index) {
            case FragmentCode.HOME:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                    transaction.add(R.id.content, homeFragment);
                } else {
                    transaction.show(homeFragment);
                }
                break;

            case FragmentCode.FIND:
                if (findFragment == null) {
                    findFragment = new FindFragment();
                    transaction.add(R.id.content, findFragment);
                } else {
                    transaction.show(findFragment);
                }
                break;

            case FragmentCode.MSG:
                if (msgFragment == null) {
                    msgFragment = new MsgFragment();
                    transaction.add(R.id.content, msgFragment);
                } else {
                    transaction.show(msgFragment);
                }
                break;

            case FragmentCode.MINE:
                if (mineFragment == null) {
                    mineFragment = new MineFragment();
                    transaction.add(R.id.content, mineFragment);
                } else {
                    transaction.show(mineFragment);
                }
                break;
        }
        transaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (findFragment != null) {
            transaction.hide(findFragment);
        }
        if (msgFragment != null) {
            transaction.hide(msgFragment);
        }
        if (mineFragment != null) {
            transaction.hide(mineFragment);
        }
    }

    private void initView() {
//        httpClient = new OkHttpClient();
        httpClient = MySslUtil.newOkHttpClient();
        firstGetProfile();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PermissionManager.handlePermissionsResult(this, type, invokeParam, this);
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//
//                } else {
//
//                }
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                if (!b) {
                    // 用户还是想用我的 APP 的
                    // 提示用户去应用设置界面手动开启权限
//                        showDialogTipUserGoToAppSettting();
                    Toast.makeText(this, "请到系统设置里去增加权限,否则无法上传照片", Toast.LENGTH_SHORT).show();
                } else {
//                        finish();
                }
            } else {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestWritePermission() {
        if (Build.VERSION.SDK_INT > 22) {
            List<String> needGrantList = new ArrayList<String>();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needGrantList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                needGrantList.add(Manifest.permission.CAMERA);
            }
            if (needGrantList != null && needGrantList.size() > 0) {
                String[] needGrant = needGrantList.toArray(new String[0]);
                ActivityCompat.requestPermissions(getActivity(), needGrant, PermissionConstant.OWN_PERMISSION);
            }
        }
    }

    /**
     * onCreate()被实执行的时候，如果用户没有登录，则userId=0, token =null
     * 这时候如果checkLoginStatus()和doGetProfile()同时执行的话，可能
     * 和doGetProfile()先执行，这个时候这时候如果checkLoginStatus()还没来得及redirect到
     * LoginActivity 就去执行和doGetProfile()，因为token=null addHead()会报错
     * 于是专门写了一个firstGetProfile()专门用于onCreate()的时候调用
     */
    private void firstGetProfile() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
            startActivity(intentObj);
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();

            doGetProfile();
        }
    }

    private void doGetProfile() {
        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.GET_PROFILE;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
//        progressWheel.spin();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        progressWheel.stopSpinning();
//                    }
//                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        progressWheel.stopSpinning();
//                    }
//                });
                if (response.code() == 200) {
                    final String responseStr = response.body().string();
                    UserProfile userProfile = GsonSingleton.getInstance().getGson().fromJson(responseStr, UserProfile.class);
                    UserInfo currentUser = UserInfoUtil.userProfile2UserInfo(userProfile);
                    if (currentUser != null) {
                        SharedPreferencesMyUtil.storeUserInfo(getActivity(), currentUser);
                    }
                }
            }
        });
    }

    private boolean checkLoginStatus() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
            startActivity(intentObj);
            return false;
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();
            return true;
        }
    }
}
