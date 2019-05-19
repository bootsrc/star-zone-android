package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.StarSignConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.MatchData;
import com.appjishu.starzone.model.TianapiStar;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.TianapiUtil;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class MatchActivity extends BaseTitleBarActivity {
    private static final String MY_TAG = "MatchActivity";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private MyHandler myHandler;

    private Spinner myStarSpinner;
    private Spinner hisStarSpinner;
    private int myStarValue = 0;
    private int hisStarValue = 0;
    private Button queryBtn;
    private TextView matchTitleView;
    private TextView matchGradleView;
    private TextView matchContentView;
    private static final int MSG_WHAT_LOAD_MATCH = 0x01;
    private static final String KEY_DATA = "KEY_DATA";

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        initData();
        initView();
    }

    private void initData() {
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new MyHandler();
        firstGetProfile();
    }

    private void initView() {
        setTitle(getString(R.string.match_activity_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        initMyStarSpinner();
        initHisStarSpinner();

        matchTitleView = findViewById(R.id.match_title);
        matchGradleView = findViewById(R.id.match_gradle);
        matchContentView = findViewById(R.id.match_content);
        queryBtn = findViewById(R.id.query_match_btn);
        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryMatch();
            }
        });
    }

    private void firstGetProfile() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
            startActivity(intentObj);
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();
        }
    }

    private void initMyStarSpinner() {
        myStarSpinner = findViewById(R.id.star_spinner_my);
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < StarSignConstant.arrText.length; i++) {
            list.add(StarSignConstant.arrText[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        myStarSpinner.setAdapter(adapter);
        myStarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myStarValue = position;
                //获取Spinner控件的适配器
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String selectedStr = adapter.getItem(position);
                String selectLog = "SelectMy: " + position + ", str=" + selectedStr;
//                Toast.makeText(MatchActivity.this, selectLog, Toast.LENGTH_SHORT).show();
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initHisStarSpinner() {
        hisStarSpinner = findViewById(R.id.star_spinner_his);
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < StarSignConstant.arrText.length; i++) {
            list.add(StarSignConstant.arrText[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        hisStarSpinner.setAdapter(adapter);
        hisStarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hisStarValue = position;
                //获取Spinner控件的适配器
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String selectedStr = adapter.getItem(position);
                String selectLog = "SelectHis: " + position + ", str=" + selectedStr;
//                Toast.makeText(MatchActivity.this, selectLog, Toast.LENGTH_SHORT).show();
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void queryMatch() {
        String url = TianapiUtil.getUrl(myStarValue, hisStarValue);
        Request request = new Request.Builder().url(url)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .get().build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(MY_TAG, "Match Failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                if (responseCode == 200) {
                    String responseStr = response.body().string();
                    if (!TextUtils.isEmpty(responseStr) && !"null".equals(responseStr)) {
                        Log.i(MY_TAG, responseStr);
                        TianapiStar tianapiStar = GsonSingleton.getInstance().getGson().fromJson(responseStr
                                , TianapiStar.class);

                        if (tianapiStar != null) {
                            if (tianapiStar.getCode() == 200) {
                                List<MatchData> matchDataList = tianapiStar.getNewslist();
                                if (matchDataList != null && matchDataList.size() > 0) {
                                    MatchData matchData = matchDataList.get(0);
                                    Message msg = new Message();
                                    msg.what = MSG_WHAT_LOAD_MATCH;
                                    Bundle bundle = new Bundle();
                                    String matchStr = GsonSingleton.getInstance().getGson().
                                            toJson(matchData, MatchData.class);
                                    bundle.putString(KEY_DATA, matchStr);
                                    msg.setData(bundle);
                                    myHandler.sendMessage(msg);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_LOAD_MATCH:
                    Bundle bundle = msg.getData();
                    String matchDataStr = bundle.getString(KEY_DATA);
                    MatchData matchData = GsonSingleton.getInstance().getGson().fromJson(matchDataStr, MatchData.class);
                    matchTitleView.setText(matchData.getTitle());
                    matchGradleView.setText(matchData.getGrade());
                    matchContentView.setText(matchData.getContent());
                    break;
            }
        }
    }
}
