package com.appjishu.starzone.activity.ext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.model.Campaign;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.NetworkStateUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.razerdp.github.com.common.constant.NetConstant;

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

public class CampaignActivity extends BaseTitleBarActivity {
    private static final String MY_TAG = "CampaignActivity";

    private OkHttpClient httpClient;
    private TextView campaignText;
    private TextView readCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);

        initView();
        initData();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    public void onTitleLeftClick() {
        super.onBackPressed();
    }

    private void initView() {//必须调用
        setTitle(getString(R.string.title_activity_campaign));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        campaignText = findViewById(R.id.campaign_text);
        readCountView = findViewById(R.id.campaign_read_count);
    }

    private void initData() {
        httpClient = MySslUtil.newOkHttpClient();

        if (!checkNetwork()) {
            return;
        }

        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.LATEST_CAMPAIGN;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                readCountView.setText("0人阅读");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.code() == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String responseStr = response.body().string();
//                                campaignText.setText
                                Campaign campaign = GsonSingleton.getInstance().getGson().fromJson(responseStr, Campaign.class);
                                if (campaign != null) {
                                    campaignText.setText(campaign.getText());
                                    int readCount = campaign.getReadCount();
                                    readCountView.setText(readCount + "人阅读");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean checkNetwork() {
        boolean connected = NetworkStateUtil.isNetworkConnected(this);
        if (!connected) {
            ToastUtil.showToast(this, SystemConstant.NETWORK_STATE_BAD);
        }
        return connected;
    }
}
