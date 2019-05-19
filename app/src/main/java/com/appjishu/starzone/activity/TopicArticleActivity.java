package com.appjishu.starzone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.Topic;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class TopicArticleActivity extends BaseTitleBarActivity {
    private static final String MY_TAG = "TopicArticleAct";

    public static final String INTENT_DATA_KEY_TOPICID = "INTENT_DATA_KEY_TOPICID";

    private long topicId = 0;
    private ImageView topicImageView;
    private TextView topicTitleTextView;
    private TextView topicContentTextView;

    @Override
    public void onHandleIntent(Intent intent) {
        topicId = intent.getLongExtra(INTENT_DATA_KEY_TOPICID, topicId);
        if (topicId <= 0) {
            finishWithError("文章不存在！");
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_article);

        initView();
        initData();
        initEvent();
    }

    private void initView() {
        setTitle(getString(R.string.topic_article_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        topicImageView = findView(R.id.topic_image_view);
        topicTitleTextView = findView(R.id.topic_title_textview);
        topicContentTextView = findView(R.id.topic_content_textview);
    }

    private void initData() {
        loadTopic();
    }

    private void initEvent() {

    }

    private void finishWithError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void refreshData(Topic topic) {
        topicTitleTextView.setText(topic.getTitle());
        topicContentTextView.setText(topic.getContent());

        String imgFullUrl = NetConstant.RESOURCES_BASE + topic.getImg();
        Glide.with(this).asBitmap().load(imgFullUrl).into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                topicImageView.setImageBitmap(CommonUtil.toRoundCorner(bitmap, bitmap.getWidth()));
                topicImageView.setImageBitmap(bitmap);
            }
        });

    }

    private void loadTopic() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody formBody = new FormBody.Builder()
                .add("topicId", topicId + "").build();

        String url = NetConstant.URL_BASE_HTTPS + NetConstant.TOPIC_FIND_BY_ID;
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(MY_TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                final Topic topic = GsonSingleton.getInstance().getGson().fromJson(responseStr, Topic.class);
                if (topic != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshData(topic);
                        }
                    });
                }
            }
        });
    }
}
