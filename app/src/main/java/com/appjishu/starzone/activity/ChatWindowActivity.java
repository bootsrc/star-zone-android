package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.app.MyApp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class ChatWindowActivity extends BaseTitleBarActivity {

    public static List<String> logList = new CopyOnWriteArrayList<String>();
    private TextView mLogView = null;

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        initView();
        initData();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLogInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.setChatWindowActivity(null);
    }

    private void initView(){
        setTitle(getString(R.string.chat_window_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        MyApp.setChatWindowActivity(this);
        mLogView = (TextView) findViewById(R.id.chat_window_log);
    }

    private void initData() {

    }

    private void initEvent(){

    }

    public void refreshLogInfo() {
        String AllLog = "";
        for (String log : logList) {
            AllLog = AllLog + log + "\n\n";
        }
        mLogView.setText(AllLog);
    }
}
