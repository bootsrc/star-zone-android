package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.AuthResponseCode;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.util.MiPushUtil;
import com.appjishu.starzone.util.NetworkStateUtil;
import com.razerdp.github.com.common.constant.NetConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.util.Md5Util;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.google.gson.Gson;
import com.pnikosis.materialishprogress.ProgressWheel;

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

public class LoginActivity extends BaseTitleBarActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private AutoCompleteTextView mPhoneView;
    private EditText mPasswordView;
    private Button mPhoneSignInBtn;
    private Button mPhoneRegisterBtn;
    private Button mResetPasswordBtn;
    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
//        initData();
        initEvent();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private void initView() {
        setTitle(getString(R.string.login_activity_title));
        setTitleMode(TitleBar.MODE_TITLE);
//        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        progressWheel = findViewById(R.id.progress_wheel_login);
        progressWheel.stopSpinning();
        mPhoneView =  findViewById(R.id.phone);
        mPasswordView = findViewById(R.id.captcha);
        mPhoneSignInBtn = findViewById(R.id.phone_sign_in_button);
        mPhoneRegisterBtn = findViewById(R.id.goto_register_button);
        mResetPasswordBtn = findViewById(R.id.reset_password_button);
    }

    private void initEvent() {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPhoneSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mPhoneRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoRegister();
            }
        });
        mResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoResetPassword();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            postParameter(phone, password);
        }
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void postParameter(final String phone, String password) {
        if (!checkNetwork()) {
            return;
        }

        progressWheel.spin();
        OkHttpClient client = MySslUtil.newOkHttpClient();

        //构建FormBody，传入要提交的参数
        String encodedPassword = Md5Util.md5(password);
        FormBody formBody = new FormBody
                .Builder()
                .add("mobile", phone)
                .add("password", encodedPassword)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.LOGIN;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWheel.stopSpinning();
                    }
                });
                Log.i(TAG, "handshakeTimeout----->");
                Log.i(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressWheel.stopSpinning();
                    }
                });

                final String responseStr = response.body().string();
                Gson gson = new Gson();
                LoginResult loginResult = gson.fromJson(responseStr, LoginResult.class);
                if (loginResult != null) {
                    switch (loginResult.getCode()) {
                        case AuthResponseCode.SUCCESS:
                            onLoginSuccess(loginResult);
                            break;
                        default:
                            printMsg(loginResult.getCodeDesc());
                    }
                }
            }
        });
    }

    private void onLoginSuccess(LoginResult loginResult) {
        long userId = loginResult.getUserId();
        String token = loginResult.getToken();

        if (userId > 0 && !TextUtils.isEmpty(token)) {
            // userId,token, mobile保存到本地
            SharedPreferencesMyUtil.storeToken(LoginActivity.this, loginResult);
            MiPushUtil.bindRegid();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    LoginActivity.this.startActivity(myIntent);
                }
            });
        } else {
            LoginActivity.this.loginFailedProcess();
        }
    }

    private void printMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginFailedProcess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void gotoRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void gotoResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }

    private boolean checkNetwork() {
        boolean connected = NetworkStateUtil.isNetworkConnected(LoginActivity.this);
        if (!connected) {
            ToastUtil.showToast(this, SystemConstant.NETWORK_STATE_BAD);
        }
        return connected;
    }
}

