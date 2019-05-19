package com.appjishu.starzone.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.ChatActivity;
import com.appjishu.starzone.activity.LoginActivity;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.UserMsg;
import com.appjishu.starzone.model.UserProfile;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.ui.adapter.FriendAdapter;
import com.appjishu.starzone.ui.adapter.UserMsgAdapter;
import com.appjishu.starzone.ui.tool.LoadMoreDelegate;
import com.appjishu.starzone.util.NetworkStateUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.ToastUtil;
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
import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;

public class MsgFragment extends Fragment
        implements View.OnClickListener,
        OnRecyclerViewItemClickListener<UserMsg>,
        LoadMoreDelegate.LoadMoreSubject {

    private static final String MY_TAG = "MsgFragment";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private MsgFragment.MyHandler myHandler;

    // 分页----START------
    private LoadMoreDelegate loadMoreDelegate;
    private boolean loading = false;
    private int currentPage = 0;
    private static final String LIMIT_STR = "50";
    // 分页----END------

    // 消息列表----START------
    private SwipeRefreshLayout swipeRefreshView;
    private List<UserMsg> mDataList = new ArrayList<>();
    private UserMsgAdapter adapter;
    private RecyclerView recyclerView;
    private static final int MSG_WHAT_RELOAD_DATA = 0x01;
    private static final int MSG_WHAT_LOAD_MORE_DATA = 0x02;
    private static final int MSG_WHAT_STOP_REFRESH = 0x03;
    private static final String KEY_RELOADED_DATA = "RELOADED_DATA";
    // 消息列表----END------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg, container, false);

        initData();
        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadListData();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.onDestroy();
            adapter = null;
        }
        if (recyclerView != null) {
            recyclerView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(View v, int position, UserMsg data) {
        if (data != null && data.getTargetId() > 0) {
            long targetUserId = data.getTargetId();
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_DATA_KEY_USER_ID, targetUserId);
            intent.putExtra(ChatActivity.INTENT_DATA_KEY_TARGET_HEAD, data.getTargetProfile().getHeadImg());
            startActivity(intent);
        }
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void onLoadMore() {
        if (!checkNetwork()) {
            return;
        }

        currentPage++;
        String currentPageStr = currentPage + "";

        String loadingText = "StepInto_onLoadMore()";
        Log.i(MY_TAG, "StepInto_onLoadMore()");

        String url = NetConstant.URL_BASE_HTTPS + NetConstant.CHAT_GET_RECENT_USER;
        FormBody formBody = new FormBody.Builder()
                .add(NetConstant.PAGE, currentPageStr)
                .add(NetConstant.LIMIT, LIMIT_STR)
                .build();
        Request request = new Request.Builder().url(url)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .post(formBody).build();

        swipeRefreshView.setRefreshing(true);
        loading = true;
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resetForLoadMore();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshView.setRefreshing(false);
                        loading = false;
                        ToastUtil.showToast(getActivity(), SystemConstant.NETWORK_STATE_BAD);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    String addedStr = response.body().string();
                    if (!TextUtils.isEmpty(addedStr) && !"null".equals(addedStr)) {
                        //
                        Type type = new TypeToken<List<UserProfile>>() {
                        }.getType();
                        List<UserProfile> reloadedData;
                        try {
                            reloadedData = GsonSingleton.getInstance().getGson().fromJson(addedStr, type);
                        } catch (Exception e) {
                            Log.i(MY_TAG, e.getMessage());
                            resetForLoadMore();
                            return;
                        }

                        if (reloadedData == null || reloadedData.size() == 0) {
                            resetForLoadMore();
                            return;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_RELOADED_DATA, addedStr);
                        Message msg = new Message();
                        msg.what = MSG_WHAT_LOAD_MORE_DATA;
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                        return;
                    }
                }
                resetForLoadMore();
            }
        });
    }

    private void initData() {//必须调用
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new MsgFragment.MyHandler();
        firstGetProfile();
        loadMoreDelegate = new LoadMoreDelegate(this);
    }

    private void initView(View view) {
        initSwipeRefreshView(view);
    }

    private void initSwipeRefreshView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new UserMsgAdapter(mDataList, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        loadMoreDelegate.attach(recyclerView);

        swipeRefreshView = view.findViewById(R.id.swipe_refresh_view);

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

    /**
     * onCreate()被实执行的时候，如果用户没有登录，则userId=0, token =null
     * 这时候如果checkLoginStatus()和doGetProfile()同时执行的话，可能
     * 和doGetProfile()先执行，这个时候这时候如果checkLoginStatus()还没来得及redirect到
     * LoginActivity 就去执行和doGetProfile()，因为token=null addHead()会报错
     * 于是专门写了一个firstGetProfile()专门用于onCreate()的时候调用
     */
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

    private void resetForLoadMore() {
        if (currentPage > 0) {
            currentPage--;
        }
        stopRefresh();
    }

    private void stopRefresh() {
        loading = false;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshView.setRefreshing(false);
            }
        });
    }

    private void loadListData() {
        if (!checkNetwork()) {
            return;
        }

        currentPage = 0;
        String url = NetConstant.URL_BASE_HTTPS + NetConstant.CHAT_GET_RECENT_USER;
        FormBody formBody = new FormBody.Builder()
                .add(NetConstant.PAGE, "0")
                .add(NetConstant.LIMIT, LIMIT_STR)
                .build();
        Request request = new Request.Builder().url(url)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .post(formBody).build();

        swipeRefreshView.setRefreshing(true);
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                stopRefresh();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshView.setRefreshing(false);
                        loading = false;
                        ToastUtil.showToast(getActivity(), SystemConstant.NETWORK_STATE_BAD);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                stopRefresh();
                int code = response.code();
                if (code == 200) {
                    String dataStr = response.body().string();
                    if (!TextUtils.isEmpty(dataStr) && !"null".equals(dataStr)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_RELOADED_DATA, dataStr);
                        Message msg = new Message();
                        msg.what = MSG_WHAT_RELOAD_DATA;
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    } else {
//                        swipeRefreshView.setRefreshing(false);
//                        myHandler.sendEmptyMessage(MSG_WHAT_STOP_REFRESH);
                    }
                } else {
//                    swipeRefreshView.setRefreshing(false);
//                    myHandler.sendEmptyMessage(MSG_WHAT_STOP_REFRESH);
                }
            }
        });
    }

    private void refreshView(String reloadedStr) {
        Type type = new TypeToken<List<UserMsg>>() {
        }.getType();
        List<UserMsg> reloadedData = null;
        try {
            reloadedData = GsonSingleton.getInstance().getGson().fromJson(reloadedStr, type);
        } catch (Exception e) {
            Log.i(MY_TAG, e.getMessage());
            stopRefresh();
            return;
        }
        mDataList.clear();
        mDataList.addAll(reloadedData);
        adapter.notifyDataSetChanged();
        // 加载完数据设置为不刷新状态，将下拉进度收起来
//        stopRefresh();
    }

    private void loadMoreView(String reloadedStr) {
        Type type = new TypeToken<List<UserMsg>>() {
        }.getType();
        List<UserMsg> reloadedData = GsonSingleton.getInstance().getGson().fromJson(reloadedStr, type);
        mDataList.addAll(reloadedData);
        adapter.notifyDataSetChanged();
        // 加载完数据设置为不刷新状态，将下拉进度收起来
        swipeRefreshView.setRefreshing(false);
        loading = false;
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_RELOAD_DATA:
                    String reloadedStr = msg.getData().getString(KEY_RELOADED_DATA);
                    refreshView(reloadedStr);
                    break;
                case MSG_WHAT_LOAD_MORE_DATA:
                    String loadedMoreStr = msg.getData().getString(KEY_RELOADED_DATA);
                    loadMoreView(loadedMoreStr);
                    break;
                case MSG_WHAT_STOP_REFRESH:
                    swipeRefreshView.setRefreshing(false);
                    break;
            }
        }
    }

    private void disableTopicRefreshing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshView.setRefreshing(false);
            }
        });
    }

    private boolean checkNetwork() {
        boolean connected = NetworkStateUtil.isNetworkConnected(getActivity());
        if (!connected) {
            ToastUtil.showToast(getActivity(), SystemConstant.NETWORK_STATE_BAD);
            disableTopicRefreshing();
        }
        return connected;
    }
}
