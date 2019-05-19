package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.razerdp.github.com.common.constant.NetConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.ResponseData;
import com.appjishu.starzone.util.Md5Util;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.google.gson.Gson;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class RegisterActivity extends BaseTitleBarActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";

    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        initEvent();

    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private AutoCompleteTextView mPhoneView;
    private EditText mCaptchaView;
    private EditText mRegisterPasswordView;
    private Button timerBtn;
    private Button mPhoneRegisterButton;

    private void initView() {
        setTitle(getString(R.string.register_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        progressWheel = findViewById(R.id.progress_wheel_register);
        progressWheel.stopSpinning();
        mPhoneView = findViewById(R.id.register_phone);
        mCaptchaView = findViewById(R.id.captcha);
        mRegisterPasswordView = findViewById(R.id.register_password);
        mPhoneRegisterButton = findViewById(R.id.goto_register_button);
        //timer
        timerBtn = findViewById(R.id.timerBtn);
    }

    private void initEvent() {
        mRegisterPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mPhoneRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        //new倒计时对象,总共的时间,每隔多少秒更新一次时间
        final MyCountDownTimer myCountDownTimer = new MyCountDownTimer(60000, 1000);

        //设置Button点击事件触发倒计时
        timerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myCountDownTimer.start();
                fetchCaptcha();
            }
        });
    }

    private void fetchCaptcha() {
        String mobile = mPhoneView.getText().toString();
//        OkHttpClient client = new OkHttpClient.Builder()
//                .readTimeout(2, TimeUnit.MINUTES)
//                .build();
        OkHttpClient client = MySslUtil.newOkHttpClient();

        // 构建FormBody，传入要提交的参数
        FormBody formBody = new FormBody
                .Builder()
                .add("mobile", mobile)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.FETCH_CAPTCHA;
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
                        ToastUtil.showToast(RegisterActivity.this, "获取验证码失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                Gson gson = new Gson();
                ResponseData responseData = gson.fromJson(responseStr, ResponseData.class);
                if (responseData != null) {
                    if (responseData.getCode() == AuthResponseCode.SUCCESS) {
                        Log.i(TAG, "---Fetch Captcha success.");
                    } else if (responseData.getCode() == AuthResponseCode.USER_CAPTCHA_TOOFAST) {
                        Log.i(TAG, AuthResponseCode.USER_CAPTCHA_TOOFAST_DESC);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, AuthResponseCode.USER_CAPTCHA_TOOFAST_DESC
                                        , Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        printMsg(responseData.getMsg());
                    }
                }
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        mPhoneView.setError(null);
        mCaptchaView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String captcha = mCaptchaView.getText().toString();
        String password = mRegisterPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(captcha) && !isCaptchaValid(captcha)) {
            mCaptchaView.setError(getString(R.string.error_invalid_captcha));
            focusView = mCaptchaView;
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

        if (TextUtils.isEmpty(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_field_required));
            focusView = mRegisterPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRegisterPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            mAuthTask = new UserLoginTask(email, password);
            doRegister(phone, captcha, password);
        }
    }

    private void attemptChangePassword() {
        mPhoneView.setError(null);
        mCaptchaView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String captcha = mCaptchaView.getText().toString();
        String password = mRegisterPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(captcha) && !isCaptchaValid(captcha)) {
            mCaptchaView.setError(getString(R.string.error_invalid_captcha));
            focusView = mCaptchaView;
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

        if (TextUtils.isEmpty(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_field_required));
            focusView = mRegisterPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRegisterPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            doChangePassword(phone, captcha, password);
        }
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11;
    }

    private boolean isCaptchaValid(String password) {
        return password.length() == 6;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    private void doRegister(final String mobile, String captcha, String password) {
        progressWheel.spin();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        // 构建FormBody，传入要提交的参数
        String encodedPassword = Md5Util.md5(password);
        FormBody formBody = new FormBody
                .Builder()
                .add("mobile", mobile)
                .add("captcha", captcha)
                .add("password", encodedPassword)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.REGISTER;
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
                        ToastUtil.showToast(RegisterActivity.this, "注册失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progressWheel.stopSpinning();
                final String responseStr = response.body().string();
                Gson gson = new Gson();
                LoginResult loginResult = gson.fromJson(responseStr, LoginResult.class);
                if (loginResult != null) {
                    switch (loginResult.getCode()) {
                        case AuthResponseCode.SUCCESS:
                            onRegisterSuccess(loginResult);
                            break;
                        case AuthResponseCode.USER_MOBILE_REPEAT:
                            onRegisterMobileRepeat();
                            break;
                        default:
                            printMsg(loginResult.getCodeDesc());
                    }
                }
            }
        });

    }

    private void onRegisterSuccess(LoginResult loginResult) {
        long userId = loginResult.getUserId();
        String token = loginResult.getToken();
        if (userId > 0 && !TextUtils.isEmpty(token)) {
            // userId,token, mobile保存到本地
            SharedPreferencesMyUtil.storeToken(RegisterActivity.this, loginResult);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(RegisterActivity.this, AddProfileActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myIntent);
                }
            });
        }
    }

    private void onRegisterMobileRepeat() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, AuthResponseCode.USER_MOBILE_REPEAT_DESC, Toast.LENGTH_SHORT);
            }
        });
    }

    private void doChangePassword(final String mobile, String captcha, String password) {
        progressWheel.spin();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        // 构建FormBody，传入要提交的参数
        String encodedPassword = Md5Util.md5(password);
        FormBody formBody = new FormBody
                .Builder()
                .add("mobile", mobile)
                .add("captcha", captcha)
                .add("password", encodedPassword)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.CHANGE_PASSWORD;
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
                        ToastUtil.showToast(RegisterActivity.this, "修改密码失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                progressWheel.stopSpinning();
                final String responseStr = response.body().string();
                Gson gson = new Gson();
                LoginResult loginResult = gson.fromJson(responseStr, LoginResult.class);
                if (loginResult != null) {
                    switch (loginResult.getCode()) {
                        case AuthResponseCode.SUCCESS:
                            onChangePasswordSuccess(loginResult);
                            break;
                        default:
                            printMsg(loginResult.getCodeDesc());
                    }
                }
            }
        });

    }

    private void printMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onChangePasswordSuccess(LoginResult loginResult) {
        long userId = loginResult.getUserId();
        String token = loginResult.getToken();

        if (userId > 0 && !TextUtils.isEmpty(token)) {
            // userId,token, mobile保存到本地
            SharedPreferencesMyUtil.storeToken(RegisterActivity.this, loginResult);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RegisterActivity.this, "修改密码成功", Toast.LENGTH_SHORT)
                            .show();
                    Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    RegisterActivity.this.startActivity(myIntent);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {

    }

    //系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    //Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //倒计时函数
    private class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //计时过程
        @Override
        public void onTick(long l) {
            //防止计时过程中重复点击
            timerBtn.setClickable(false);
            timerBtn.setText(l / 1000 + "秒");

        }

        //计时完毕的方法
        @Override
        public void onFinish() {
            //重新给Button设置文字
            timerBtn.setText("重新获取");
            //设置可点击
            timerBtn.setClickable(true);
        }
    }
}

