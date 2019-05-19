package com.appjishu.starzone.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.AddProfileActivity;
import com.appjishu.starzone.activity.CheckInActivity;
import com.appjishu.starzone.activity.LoginActivity;
import com.appjishu.starzone.activity.MatchActivity;
import com.appjishu.starzone.activity.ScoreActivity;
import com.appjishu.starzone.activity.StarCharacterActivity;
import com.appjishu.starzone.activity.TopicArticleActivity;
import com.appjishu.starzone.activity.circle.FriendCircleActivity;
import com.appjishu.starzone.activity.ext.CampaignActivity;
import com.appjishu.starzone.constant.FunctionKey;
import com.appjishu.starzone.constant.FunctionKeyTitle;
import com.appjishu.starzone.constant.SystemConstant;
import com.appjishu.starzone.model.FunctionData;
import com.appjishu.starzone.model.IntroduceVO;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.SaveImageResult;
import com.appjishu.starzone.model.Topic;
import com.appjishu.starzone.model.UserProfile;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.ui.adapter.FunctionAdapter;
import com.appjishu.starzone.ui.adapter.TopicAdapter;
import com.appjishu.starzone.ui.tool.LoadMoreDelegate;
import com.appjishu.starzone.ui.tool.OnRecyclerItemClickListener;
import com.appjishu.starzone.util.CommonUtil;
import com.appjishu.starzone.util.ImgUtil;
import com.appjishu.starzone.util.NetworkStateUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.appjishu.starzone.util.UserInfoUtil;
import com.google.gson.reflect.TypeToken;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.UserInfo;

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
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;

public class HomeFragment extends Fragment implements View.OnClickListener,
        EasyPermissions.PermissionCallbacks,
        OnRecyclerViewItemClickListener<Topic>,
        LoadMoreDelegate.LoadMoreSubject,
        OnRecyclerItemClickListener<FunctionData> {
    private static final String MY_TAG = "HomeFragment";

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private String introduceContent;
    // 保存图片到系统相册
    private static final int REQUEST_CODE_SAVE_IMG = 10;
    private static final int ALERT_DIALOG_REQUEST_CODE_DONATE = 0;

    // 话题列表----START------
    private SwipeRefreshLayout swipeRefreshView;
    private List<Topic> mDataList = new ArrayList<>();
    private TopicAdapter topicAdapter;
    private RecyclerView topicRecyclerView;
    private static final int MSG_WHAT_RELOAD_DATA = 0x01;
    private static final int MSG_WHAT_LOAD_MORE_DATA = 0x02;
    private static final String KEY_RELOADED_DATA = "RELOADED_DATA";
    // 话题列表----END------
    private MyHandler myHandler;
    private LoadMoreDelegate loadMoreDelegate;
    private boolean loading = false;
    //    private boolean loadingMore = false;
    private int currentPage = 0;

    // 功能按钮，用RecyclerView做成的GridView-----START
    private RecyclerView functionRecyclerView;
    private FunctionAdapter functionAdapter;
    // 功能按钮，用RecyclerView做成的GridView-----END
    private TextView carouselView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        boolean logined = checkLoginStatus();

        if (logined) {
            carouselView = view.findViewById(R.id.carousel_view);
            String carouselText = "朋友圈互动中，小伙伴们快戳进来发一下自己的新动态吧" +
                    "^_^。 签到能赢更多积分哟！";
            carouselView.setText(carouselText);
            carouselView.setSelected(true);
            initData();
            initView(view);
            initEvent(view);
            loadTopicListData();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    @Override
    public void onDestroy() {
        if (topicAdapter != null) {
            topicAdapter = null;
        }
        if (functionAdapter != null) {
            functionAdapter = null;
        }
        if (topicRecyclerView != null) {
            topicRecyclerView = null;
        }
        if (functionRecyclerView != null) {
            functionRecyclerView = null;
        }
        super.onDestroy();
    }

    //同意授权
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Log.i(MY_TAG, "onPermissionsGranted:" + requestCode + ":" + list.size());
        saveImage();
    }

    //拒绝授权
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.i(MY_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //打开系统设置，手动授权
            Toast.makeText(getActivity(), SystemConstant.PERMISSION_SETTINGS_TIP
                    , Toast.LENGTH_SHORT).show();
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRecyclerItemClick(View v, int position, FunctionData data) {
        if (data != null) {
            if (FunctionKey.STAR_DEFINITION.equals(data.getKey())) {
                Intent intent = new Intent(getActivity(), StarCharacterActivity.class);
                startActivity(intent);
                return;
            } else if (FunctionKey.CHECK_IN.equals(data.getKey())) {
                Intent intent = new Intent(getActivity(), CheckInActivity.class);
                startActivity(intent);
                return;
            } else if (FunctionKey.SCORE.equals(data.getKey())) {
                Intent intent = new Intent(getContext(), ScoreActivity.class);
                startActivity(intent);
                return;
            } else if (FunctionKey.MATCH.equals(data.getKey())) {
                Intent intent = new Intent(getContext(), MatchActivity.class);
                startActivity(intent);
                return;
            } else if (FunctionKey.CAMPAIGN.equals(data.getKey())) {
                Intent intent = new Intent(getContext(), CampaignActivity.class);
                startActivity(intent);
                return;
            } else {
                String text = data.getText();
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkLoginStatus() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
            startActivity(intentObj);
            return false;
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();
            return true;
        }
    }

    private void initData() {//必须调用
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new MyHandler();
        firstGetProfile();
        loadMoreDelegate = new LoadMoreDelegate(this);
    }

    private void initView(View view) {
        initFunctionView(view);
        initSwipeRefreshView(view);
    }

    private void initFunctionView(View view) {
        functionRecyclerView = view.findViewById(R.id.function_recycler_view);
        List<FunctionData> functionDataList = new ArrayList<>();

        FunctionData functionDataStarDefinition = new FunctionData();
        functionDataStarDefinition.setKey(FunctionKey.STAR_DEFINITION);
        functionDataStarDefinition.setText(FunctionKeyTitle.STAR_DEFINITION);
        functionDataStarDefinition.setResId(R.drawable.star_1_baiyang);

        FunctionData functionDataCheckIn = new FunctionData();
        functionDataCheckIn.setKey(FunctionKey.CHECK_IN);
        functionDataCheckIn.setText(FunctionKeyTitle.CHECK_IN);
        functionDataCheckIn.setResId(R.drawable.ic_check_circle_blue_24dp);

        FunctionData functionDataScore = new FunctionData();
        functionDataScore.setKey(FunctionKey.SCORE);
        functionDataScore.setText(FunctionKeyTitle.SCORE);
        functionDataScore.setResId(R.drawable.ic_trending_up_red_24dp);

        FunctionData functionDataMatch = new FunctionData();
        functionDataMatch.setKey(FunctionKey.MATCH);
        functionDataMatch.setText(FunctionKeyTitle.MATCH);
        functionDataMatch.setResId(R.drawable.ic_favorite_red_24dp);

        FunctionData functionDataCampaign = new FunctionData();
        functionDataCampaign.setKey(FunctionKey.CAMPAIGN);
        functionDataCampaign.setText(FunctionKeyTitle.CAMPAIGN);
        functionDataCampaign.setResId(R.drawable.ic_nature_people_red_24dp);

        functionDataList.add(functionDataStarDefinition);
        functionDataList.add(functionDataCheckIn);
        functionDataList.add(functionDataScore);
        functionDataList.add(functionDataMatch);
        functionDataList.add(functionDataCampaign);

        functionAdapter = new FunctionAdapter(functionDataList, this);
        functionRecyclerView.setAdapter(functionAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        functionRecyclerView.setLayoutManager(gridLayoutManager);
    }

    /**
     * onCreate()被实执行的时候，如果用户没有登录，则userId=0, token =null
     * 这时候如果checkLoginStatus()和doGetProfile()同时执行的话，可能
     * 和doGetProfile()先执行，这个时候这时候如果checkLoginStatus()还没来得及redirect到
     * LoginActivity 就去执行和doGetProfile()，因为token=null addHead()会报错
     * 于是专门写了一个firstGetProfile()专门用于onCreate()的时候调用
     */
    private void firstGetProfile() {
//        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(getActivity());
//        if (loginResult == null || loginResult.getUserId() < 1
//                || TextUtils.isEmpty(loginResult.getToken())) {
//            Intent intentObj = new Intent(getActivity(), LoginActivity.class);
//            startActivity(intentObj);
//        } else {
//            userId = loginResult.getUserId();
//            token = loginResult.getToken();
//
//            doGetProfile();
//        }

        doGetProfile();
    }

    private void doGetProfile() {
        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.GET_PROFILE;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
//        progressWheel.spin();

        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        progressWheel.stopSpinning();
//                    }
//                });
                if (response.code() == 200) {
                    final String responseStr = response.body().string();
                    if (!TextUtils.isEmpty(responseStr) && !"null".equals(responseStr)) {
                        UserProfile userProfile = GsonSingleton.getInstance().getGson().fromJson(responseStr, UserProfile.class);
                        // 如果资料为空，就跳转到完善资料的页面
                        if (userProfile == null || userProfile.getUserId() == 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "请提交个人资料", Toast.LENGTH_SHORT)
                                            .show();
                                    Intent intent = new Intent(getActivity(), AddProfileActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                        UserInfo currentUser = UserInfoUtil.userProfile2UserInfo(userProfile);
                        if (currentUser != null) {
                            SharedPreferencesMyUtil.storeUserInfo(getActivity(), currentUser);
                        }
                    }
                }
            }
        });
    }

    private void initEvent(View view) {//必须调用
        view.findViewById(R.id.carousel_layout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.carousel_layout:
                Intent intent = new Intent(getActivity(), FriendCircleActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }

    private void shareApp() {
        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.INTRODUCE_FROM_SERVER;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.shareInfo(getActivity(), NetConstant.APP_INTRODUCE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    IntroduceVO introduceVO = GsonSingleton.getInstance().getGson()
                            .fromJson(response.body().string(), IntroduceVO.class);
                    if (introduceVO == null || TextUtils.isEmpty(introduceVO.getTextContent())) {
                        introduceContent = NetConstant.APP_INTRODUCE;
                    } else {
                        introduceContent = introduceVO.getTextContent();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonUtil.shareInfo(getActivity(), introduceContent);
                        }
                    });
                }
            }
        });
    }

    //保存图片
    private void saveImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alipay_receive);
        SaveImageResult saveImageResult = ImgUtil.saveImageToGallery(getActivity(), bitmap);
        boolean isSaveSuccess = saveImageResult.isSuccess();
        if (isSaveSuccess) {
            Toast.makeText(getActivity(), "保存成功，请打开支付宝识别二维码", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "保存图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下载支付图片
     */
    private void downloadAlipayImage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestPermission();
            }
        });
    }

    /**
     * 请求读取sd卡的权限. 保存支付宝照片到相册的时候
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //读取sd卡的权限
            String[] mPermissionList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(getActivity(), mPermissionList)) {
                //已经同意过
                saveImage();
            } else {
                //未同意过,或者说是拒绝了，再次申请权限
                EasyPermissions.requestPermissions(
                        this,  //上下文
                        "保存图片需要读取sd卡的权限", //提示文言
                        REQUEST_CODE_SAVE_IMG, //请求码
                        mPermissionList //权限列表
                );
            }
        } else {
            saveImage();
        }
    }

    private void initSwipeRefreshView(View view) {
        topicRecyclerView = view.findViewById(R.id.topic_recycler_view);
        topicRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        topicAdapter = new TopicAdapter(mDataList, this);
        topicRecyclerView.setItemAnimator(new DefaultItemAnimator());
        topicRecyclerView.setAdapter(topicAdapter);
        loadMoreDelegate.attach(topicRecyclerView);

        swipeRefreshView = view.findViewById(R.id.topic_swipe_refresh_view);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshView.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
//        swipeRefreshView.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
//        swipeRefreshView.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary,
//                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshView.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_purple);
        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTopicListData();
            }
        });
    }

    @Override
    public void onItemClick(View v, int position, Topic data) {
        if (data != null && data.getTopicId() > 0) {
            long topicId = data.getTopicId();
            String topicIdStr = "topicId=" + topicId;
            Intent intent = new Intent(getActivity(), TopicArticleActivity.class);
            intent.putExtra(TopicArticleActivity.INTENT_DATA_KEY_TOPICID, topicId);
            startActivity(intent);
        }
    }

    private void loadTopicListData() {
        if (!checkNetwork()) {
            return;
        }

        currentPage = 0;
        String url = NetConstant.URL_BASE_HTTPS + NetConstant.TOPIC_BY_PAGE;
        String currentPageStr = currentPage + "";
        FormBody formBody = new FormBody.Builder()
                .add(NetConstant.PAGE, currentPageStr)
                .add(NetConstant.LIMIT, "10")
                .build();
        Request request = new Request.Builder().url(url)
                .post(formBody).build();

        swipeRefreshView.setRefreshing(true);
        loading = true;
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshView.setRefreshing(false);
                        loading = false;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    String addedStr = response.body().string();
                    if (!TextUtils.isEmpty(addedStr) && !"null".equals(addedStr)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(KEY_RELOADED_DATA, addedStr);
                        Message msg = new Message();
                        msg.what = MSG_WHAT_RELOAD_DATA;
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    } else {
                        disableTopicRefreshing();
                    }
                } else {
                    disableTopicRefreshing();
                }
            }
        });
    }

    private void disableTopicRefreshing() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshView.setRefreshing(false);
                loading = false;
            }
        });
    }

    @Override
    public boolean isLoading() {
//        return loading || loadingMore;
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
        Toast.makeText(getContext(), loadingText, Toast.LENGTH_SHORT);

        String url = NetConstant.URL_BASE_HTTPS + NetConstant.TOPIC_BY_PAGE;
        FormBody formBody = new FormBody.Builder()
                .add(NetConstant.PAGE, currentPageStr)
                .add(NetConstant.LIMIT, "10")
                .build();
        Request request = new Request.Builder().url(url)
                .post(formBody).build();

        swipeRefreshView.setRefreshing(true);
        loading = true;
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshView.setRefreshing(false);
                        loading = false;
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
                        Type type = new TypeToken<List<Topic>>() {
                        }.getType();
                        List<Topic> reloadedData = GsonSingleton.getInstance().getGson().fromJson(addedStr, type);
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

    private void resetForLoadMore() {
        if (currentPage > 0) {
            currentPage--;
        }
        disableTopicRefreshing();
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
            }
        }
    }

    private void refreshView(String reloadedStr) {
        Type type = new TypeToken<List<Topic>>() {
        }.getType();
        List<Topic> reloadedData = GsonSingleton.getInstance().getGson().fromJson(reloadedStr, type);
        mDataList.clear();
        mDataList.addAll(reloadedData);
        topicAdapter.notifyDataSetChanged();
//        Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
        // 加载完数据设置为不刷新状态，将下拉进度收起来
        swipeRefreshView.setRefreshing(false);
        loading = false;
    }

    private void loadMoreView(String reloadedStr) {
        Type type = new TypeToken<List<Topic>>() {
        }.getType();
        List<Topic> reloadedData = GsonSingleton.getInstance().getGson().fromJson(reloadedStr, type);
        mDataList.addAll(reloadedData);
        topicAdapter.notifyDataSetChanged();
        // 加载完数据设置为不刷新状态，将下拉进度收起来
        swipeRefreshView.setRefreshing(false);
        loading = false;
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