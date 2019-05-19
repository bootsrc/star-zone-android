package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.AboutInfoConstant;

import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class AboutAppActivity extends BaseTitleBarActivity {
    private static final String MY_TAG = "AboutAppAct";

    private TextView aboutAppTextView;
    private TextView websiteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        initView();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    public void onTitleLeftClick() {
        super.onBackPressed();
    }

    private void initView() {//必须调用
        setTitle(getString(R.string.title_activity_about_app));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        aboutAppTextView = findViewById(R.id.about_app_text);
        websiteView=findViewById(R.id.website_view);
        aboutAppTextView.setText(AboutInfoConstant.ABOUT_APP_INTRODUCE);
        websiteView.setText(AboutInfoConstant.PORTAL_WEBSITE);
    }
}

