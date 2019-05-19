package com.appjishu.starzone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;

import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

/**
 * 设置界面Activity
 *
 * @author liushaoming
 * @use toActivity(SettingsActivity.createIntent ( ...));
 */
public class SettingsActivity extends BaseTitleBarActivity implements View.OnClickListener {
    private static final String MY_TAG = "SettingsActivity";

    private LinearLayout registerLayout;
    private LinearLayout signOutLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
        initEvent();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    public void onTitleLeftClick() {
        super.onBackPressed();
    }

    private void initView() {//必须调用
        setTitle(getString(R.string.title_activity_settings));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        registerLayout = findView(R.id.register_and_change_password_linear_layout);
        signOutLayout = findView(R.id.sign_out_linear_layout);
    }

    private void initEvent() {//必须调用
        registerLayout.setOnClickListener(this);
        signOutLayout.setOnClickListener(this);
    }

//	public void onDialogButtonClick(int requestCode, boolean isPositive) {
//		if (isPositive) {
//			doSignOut();
//		}
//	}



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_and_change_password_linear_layout:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.sign_out_linear_layout:
                signOut();
                break;
            default:
        }
    }

    private void signOut() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定退出登录？")
                .setNegativeButton("取消", null)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doSignOut();
                    }
                }).show();
    }

    private void doSignOut() {
        LoginResult loginResult = new LoginResult();
        loginResult.setUserId(0);
        loginResult.setUsername(null);
        loginResult.setToken(null);
        SharedPreferencesMyUtil.storeToken(this, loginResult);
        finish();
    }
}
