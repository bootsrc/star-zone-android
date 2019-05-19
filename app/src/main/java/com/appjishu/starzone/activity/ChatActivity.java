package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.ChatMsg;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.ui.adapter.MsgAdapter;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.google.gson.reflect.TypeToken;
import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class ChatActivity extends BaseTitleBarActivity {
    private static final String MY_TAG = "ChatActivity";
    public static final String INTENT_DATA_KEY_USER_ID = "INTENT_DATA_KEY_USER_ID";
    public static final String INTENT_DATA_KEY_TARGET_HEAD = "INTENT_DATA_KEY_TARGET_HEAD";

    private long targetUserId = 0;
    private String targetHeadImg = null;
    private List<ChatMsg> msgList = new ArrayList<>();
    private EditText inputText;
    private ImageView send;
    private RecyclerView recyclerView;
    private MsgAdapter adapter;
    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private SwipeRefreshLayout swipeRefreshView;
    private static final int MSG_WHAT_RELOAD_DATA = 0x01;
    //    private static final int MSG_WHAT_LOAD_MORE_DATA = 0x02;
    public static final int MSG_WHAT_RECEIVE_PUSH = 0x03;
    private static final String KEY_RELOADED_DATA = "RELOADED_DATA";
    private MyHandler myHandler;

    @Override
    public void onHandleIntent(Intent intent) {
        targetUserId = intent.getLongExtra(INTENT_DATA_KEY_USER_ID, targetUserId);
        if (targetUserId < 1) {
            finishWithError("对方用户不存在！");
            return;
        }
        targetHeadImg = intent.getStringExtra(INTENT_DATA_KEY_TARGET_HEAD);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        instance = this;
        initView();
        initEvent();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        if (recyclerView != null) {
            recyclerView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onTitleRightClick() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.INTENT_DATA_KEY_USER_ID, targetUserId);
        startActivity(intent);
    }

    private void initView() {
        setTitle(getString(R.string.chat_activity_title));
        setTitleMode(TitleBar.MODE_BOTH);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);
        setTitleRightIcon(R.drawable.ic_more_horiz_white_24dp);

        myHandler = new MyHandler();
//        DemoMessageReceiver.setChatHandler(myHandler);

        httpClient = MySslUtil.newOkHttpClient();
        checkLoginStatus();
        initInput();
        loadListData();
    }

    private void initInput() {
        inputText = (EditText) findViewById(R.id.input_text);
        send = (ImageView) findViewById(R.id.send_msg_btn);

        initSwipeRefreshView();
        send.setOnClickListener(new View.OnClickListener() {                 //发送按钮点击事件
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();              //获取EditText中的内容
                if (!"".equals(content)) {                                    //内容不为空则创建一个新的Msg对象，并把它添加到msgList列表中
                    ChatMsg msg = new ChatMsg();
                    msg.setSenderId(userId);
                    msg.setReceiverId(targetUserId);
                    msg.setMsgType(0);
                    msg.setMsgBody(content);
//                    msgList.add(msg);
//                    adapter.notifyItemInserted(msgList.size() - 1);           //调用适配器的notifyItemInserted()用于通知列表有新的数据插入，这样新增的一条消息才能在RecyclerView中显示
                    msgList.add(0, msg);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
//                    recyclerView.scrollToPosition(msgList.size() - 1);     //调用scrollToPosition()方法将显示的数据定位到最后一行，以保证可以看到最后发出的一条消息
                    inputText.setText("");                                  //调用EditText的setText()方法将输入的内容清空
                    sendChat(targetUserId, content, 0);
                }
            }
        });
    }

    private void sendChat(long receiverId, String msgBody, int msgType) {
        FormBody formBody = new FormBody.Builder()
                .add("receiverId", receiverId + "")
                .add("msgBody", msgBody)
                .add("msgType", msgType + "")
                .build();

        Request request = new Request.Builder()
                .url(NetConstant.URL_BASE_HTTPS + NetConstant.CHAT_SEND)
                .post(formBody)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .build();

        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, NetConstant.HTTP_REQUEST_FAILED,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(MY_TAG, "ChatSendSuccess.");
            }
        });
    }

    private void checkLoginStatus() {
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

    private void initEvent() {

    }

    private void finishWithError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void initSwipeRefreshView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, true);

        recyclerView = findViewById(R.id.msg_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new MsgAdapter(msgList, targetHeadImg);
        recyclerView.setAdapter(adapter);
        swipeRefreshView = findViewById(R.id.swipe_refresh_view);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshView.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshView.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_purple);

        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadListData();
            }
        });
    }

    private void loadListData() {
        FormBody formBody = new FormBody.Builder()
                .add("targetUserId", targetUserId + "")
                .build();

        Request request = new Request.Builder()
                .url(NetConstant.URL_BASE_HTTPS + NetConstant.CHAT_MSG_LIST)
                .post(formBody)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .build();

        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                disableTopicRefreshing();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    String addedStr = response.body().string();
                    if (!TextUtils.isEmpty(addedStr)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_RELOADED_DATA, addedStr);
                        Message msg = new Message();
                        msg.what = MSG_WHAT_RELOAD_DATA;
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    } else {
                        swipeRefreshView.setRefreshing(false);
                    }
                } else {
                    swipeRefreshView.setRefreshing(false);
                }
            }
        });
    }

    private void stopRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshView.setRefreshing(false);
            }
        });
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_RELOAD_DATA:
                    String reloadedStr = msg.getData().getString(KEY_RELOADED_DATA);
                    refreshView(reloadedStr);
                    break;
                case MSG_WHAT_RECEIVE_PUSH:
                    ChatMsg received = (ChatMsg) msg.obj;
                    if (received != null && received.getSenderId() > 0
                            && received.getReceiverId() > 0) {
                        msgList.add(0, received);
                        adapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);
                    }
            }
        }
    }


    private void refreshView(String reloadedStr) {
        Type type = new TypeToken<List<ChatMsg>>() {
        }.getType();
        List<ChatMsg> reloadedData = null;
        try {
            reloadedData = GsonSingleton.getInstance().getGson().fromJson(reloadedStr, type);
        } catch (Exception e) {
            Log.i(MY_TAG, e.getMessage());
            return;
        }
        msgList.clear();
        msgList.addAll(reloadedData);
        adapter.notifyDataSetChanged();
        // 加载完数据设置为不刷新状态，将下拉进度收起来
        swipeRefreshView.setRefreshing(false);
    }

    public void sendMessage(Message message) {
        myHandler.sendMessage(message);
    }

    private static ChatActivity instance;

    public static ChatActivity getInstance() {
        return instance;
    }

    private void disableTopicRefreshing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshView.setRefreshing(false);
            }
        });
    }
}
