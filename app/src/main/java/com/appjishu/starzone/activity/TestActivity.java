package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.razerdp.github.com.common.entity.CommentInfo;

import java.util.List;

import com.appjishu.starzone.R;
import com.tuo.customview.VerificationCodeView;

import razerdp.github.com.lib.base.BaseActivity;
import razerdp.github.com.lib.utils.GsonUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.commentwidget.CommentContentsLayout;

/**
 * Created by liushaoming on 2018/3/28.
 */
public class TestActivity extends BaseTitleBarActivity {
    private LinearLayout content;
    private VerificationCodeView icv;


    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        content = findViewById(R.id.test_content);
        icv = (VerificationCodeView) findViewById(R.id.icv);

        final VerificationCodeView codeView = new VerificationCodeView(this);


        content.addView(codeView);

        icv.setInputCompleteListener(new VerificationCodeView.InputCompleteListener() {
            @Override
            public void inputComplete() {
                Log.i("icv_input", icv.getInputContent());
            }

            @Override
            public void deleteContent() {
                Log.i("icv_delete", icv.getInputContent());
            }
        });


        codeView.postDelayed(new Runnable() {
            @Override
            public void run() {
                codeView.setEtNumber(5);
            }
        }, 5000);



        codeView.setInputCompleteListener(new VerificationCodeView.InputCompleteListener() {
            @Override
            public void inputComplete() {
                Log.i("icv_input", codeView.getInputContent());
            }

            @Override
            public void deleteContent() {
                Log.i("icv_delete", codeView.getInputContent());
            }
        });


    }

    public void onClick(View view) {
        icv.clearInputContent();
    }
}