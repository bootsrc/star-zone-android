package com.appjishu.starzone.activity.circle;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appjishu.starzone.activity.LoginActivity;
import com.appjishu.starzone.constant.CustomConstant;
import com.appjishu.starzone.constant.MiscConstant;
import com.appjishu.starzone.http.MyMomentRequest;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.UserProfile;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.razerdp.github.com.common.MomentsType;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikeInfo;
import com.razerdp.github.com.common.entity.MomentInfo;
import com.razerdp.github.com.common.entity.UserInfo;
import com.razerdp.github.com.common.entity.other.ServiceInfo;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.razerdp.github.com.common.router.RouterList;
import com.socks.library.KLog;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.ActivityLauncher;
import com.appjishu.starzone.app.manager.ServiceInfoManager;
import com.appjishu.starzone.app.manager.UpdateInfoManager;
import com.appjishu.starzone.app.mvp.presenter.impl.MomentPresenter;
import com.appjishu.starzone.app.mvp.view.IMomentView;
import com.appjishu.starzone.ui.adapter.CircleMomentsAdapter;
import com.appjishu.starzone.ui.viewholder.EmptyMomentsVH;
import com.appjishu.starzone.ui.viewholder.MultiImageMomentsVH;
import com.appjishu.starzone.ui.viewholder.TextOnlyMomentsVH;
import com.appjishu.starzone.ui.viewholder.WebMomentsVH;
import com.appjishu.starzone.ui.widget.popup.RegisterPopup;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.common.entity.ImageInfo;
import razerdp.github.com.lib.helper.AppFileHelper;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.lib.interfaces.SingleClickListener;
import razerdp.github.com.lib.manager.KeyboardControlMnanager;
import razerdp.github.com.lib.network.base.OnResponseListener;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.helper.PhotoHelper;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.util.AnimUtils;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.widget.commentwidget.CommentBox;
import razerdp.github.com.ui.widget.commentwidget.CommentWidget;
import razerdp.github.com.ui.widget.commentwidget.IComment;
import razerdp.github.com.ui.widget.common.TitleBar;
import razerdp.github.com.ui.widget.popup.SelectPhotoMenuPopup;
import razerdp.github.com.ui.widget.pullrecyclerview.CircleRecyclerView;
import razerdp.github.com.ui.widget.pullrecyclerview.CircleRecyclerView.OnPreDispatchTouchListener;
import razerdp.github.com.ui.widget.pullrecyclerview.interfaces.OnRefreshListener2;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by liushaoming on 2016/10/26.
 * <p>
 * 朋友圈主界面
 */

public class FriendCircleActivity extends BaseTitleBarActivity implements OnRefreshListener2, IMomentView, OnPreDispatchTouchListener {
    private static final String MY_TAG = "FriendCircleActivity";
    public static final int REQUEST_REFRESH = 0x10;
    private static final int REQUEST_LOADMORE = 0x11;

    private int clickServiceCount = 0;
    private RelativeLayout mTipsLayout;
    private TextView mServiceTipsView;
    private ImageView mCloseImageView;
    //服务器消息检查，非项目所需↑

    private CircleRecyclerView circleRecyclerView;
    private CommentBox commentBox;
    private HostViewHolder hostViewHolder;
    private CircleMomentsAdapter adapter;
    private List<MomentInfo> momentInfoList;
    //request
    private MyMomentRequest momentRequest;
    private MomentPresenter presenter;

    private CircleViewHelper mViewHelper;
    private static final String MSG_KEY_MOMENT_LIST = "moment_list_str";
    private static final String MSG_KEY_RESPONSE_TYPE = "response_type";
    private Gson gson;
    private MyHandler myHandler;
    private static final int MSG_WHAT_MOMENT_LIST_RESPONSE = 0x01;
    private static final int MSG_WHAT_GET_PROFILE = 0x02;
    private static final int MSG_WHAT_LIKE_CHANGE = 0x03;
    private static final int MSG_WHAT_DELETE_MOMENT = 0x04;
    private static final int MSG_WHAT_COMMENT_CHANGE = 0x05;

    private OkHttpClient httpClient;
    private long userId;
    private String token;
    private UserProfile userProfile;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_circle);
        gson = new Gson();
        myHandler = new MyHandler();
        momentInfoList = new ArrayList<>();

//        httpClient = new OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
        httpClient = MySslUtil.newOkHttpClient();
        checkLoginStatus();
        setCurrentUser();
        momentRequest = new MyMomentRequest(userId, token);
        initView();
        initKeyboardHeightObserver();

//        UpdateInfoManager.INSTANCE.init(this, new BasePopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                delayCheckServiceInfo();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatus();
//        AppFileHelper.initStroagePath(this);
        doGetProfile(userId, token);
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private void initView() {
        if (mViewHelper == null) {
            mViewHelper = new CircleViewHelper(this);
        }
        setTitle("朋友圈");
        setTitleMode(TitleBar.MODE_BOTH);
        setTitleRightIcon(R.drawable.ic_camera);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);
        presenter = new MomentPresenter(this);

        hostViewHolder = new HostViewHolder(this);


        circleRecyclerView = (CircleRecyclerView) findViewById(R.id.recycler);
        circleRecyclerView.setOnRefreshListener(this);
        circleRecyclerView.setOnPreDispatchTouchListener(this);
        circleRecyclerView.addHeaderView(hostViewHolder.getView());

        mTipsLayout = (RelativeLayout) findViewById(R.id.tips_layout);
        mServiceTipsView = (TextView) findViewById(R.id.service_tips);
        mCloseImageView = (ImageView) findViewById(R.id.iv_close);

        commentBox = (CommentBox) findViewById(R.id.widget_comment);
        commentBox.setOnCommentSendClickListener(onCommentSendClickListener);

        adapter = new CircleMomentsAdapter(this, momentInfoList, presenter);
        adapter.addViewHolder(EmptyMomentsVH.class, MomentsType.EMPTY_CONTENT)
                .addViewHolder(MultiImageMomentsVH.class, MomentsType.MULTI_IMAGES)
                .addViewHolder(TextOnlyMomentsVH.class, MomentsType.TEXT_ONLY)
                .addViewHolder(WebMomentsVH.class, MomentsType.WEB);
        circleRecyclerView.setAdapter(adapter);
        circleRecyclerView.autoRefresh();

    }

    private void setCurrentUser() {
        UserInfo currentUser = SharedPreferencesMyUtil.queryUserInfo(this);
        LocalHostManager.INSTANCE.updateLocalHost(currentUser);
    }

    private void initKeyboardHeightObserver() {
        //观察键盘弹出与消退
        KeyboardControlMnanager.observerKeyboardVisibleChange(this, new KeyboardControlMnanager.OnKeyboardStateChangeListener() {
            View anchorView;

            @Override
            public void onKeyboardChange(int keyboardHeight, boolean isVisible) {
                int commentType = commentBox.getCommentType();
                if (isVisible) {
                    //定位评论框到view
                    anchorView = mViewHelper.alignCommentBoxToView(circleRecyclerView, commentBox, commentType);
                } else {
                    //定位到底部
                    commentBox.dismissCommentBox(false);
                    mViewHelper.alignCommentBoxToViewWhenDismiss(circleRecyclerView, commentBox, commentType, anchorView);
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        momentRequest.setOnResponseListener(momentsRequestCallBack);
        momentRequest.setRequestType(REQUEST_REFRESH);
        momentRequest.setCurPage(0);
        momentRequest.execute();
    }

    @Override
    public void onLoadMore() {
        momentRequest.setOnResponseListener(momentsRequestCallBack);
        momentRequest.setRequestType(REQUEST_LOADMORE);
        momentRequest.execute();
    }

    //titlebar click

    @Override
    public void onTitleDoubleClick() {
        super.onTitleDoubleClick();
        if (circleRecyclerView != null) {
            int firstVisibleItemPos = circleRecyclerView.findFirstVisibleItemPosition();
            circleRecyclerView.getRecyclerView().smoothScrollToPosition(0);
            if (firstVisibleItemPos > 1) {
                circleRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        circleRecyclerView.autoRefresh();
                    }
                }, 200);
            }
        }

    }

    @Override
    public void onTitleLeftClick() {
        super.onBackPressed();
    }

    @Override
    public void onTitleRightClick() {
        new SelectPhotoMenuPopup(this).setOnSelectPhotoMenuClickListener(new SelectPhotoMenuPopup.OnSelectPhotoMenuClickListener() {
            @Override
            public void onShootClick() {
                PhotoHelper.fromCamera(FriendCircleActivity.this, false);
            }

            @Override
            public void onAlbumClick() {
                ActivityLauncher.startToPhotoSelectActivity(getActivity(), RouterList.PhotoSelectActivity.requestCode);
            }
        }).showPopupWindow();
    }

    @Override
    public boolean onTitleRightLongClick() {
        ActivityLauncher.startToPublishActivityWithResult(this,
                RouterList.PublishActivity.MODE_TEXT,
                null,
                RouterList.PublishActivity.requestCode, userId, token);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoHelper.handleActivityResult(this, requestCode, resultCode, data, new PhotoHelper.PhotoCallback() {
            @Override
            public void onFinish(String filePath) {
                List<ImageInfo> selectedPhotos = new ArrayList<ImageInfo>();
                selectedPhotos.add(new ImageInfo(filePath, null, null, 0, 0));
                ActivityLauncher.startToPublishActivityWithResult(FriendCircleActivity.this,
                        RouterList.PublishActivity.MODE_MULTI,
                        selectedPhotos,
                        RouterList.PublishActivity.requestCode
                        , userId, token);
            }

            @Override
            public void onError(String msg) {
                UIHelper.ToastMessage(msg);
            }
        });
        if (requestCode == RouterList.PhotoSelectActivity.requestCode && resultCode == RESULT_OK) {
            List<ImageInfo> selectedPhotos = data.getParcelableArrayListExtra(RouterList.PhotoSelectActivity.key_result);
            if (selectedPhotos != null) {
                ActivityLauncher.startToPublishActivityWithResult(this,
                        RouterList.PublishActivity.MODE_MULTI, selectedPhotos,
                        RouterList.PublishActivity.requestCode,
                        userId,
                        token);
            }
        }

        if (requestCode == RouterList.PublishActivity.requestCode && resultCode == RESULT_OK) {
            circleRecyclerView.autoRefresh();
        }
    }

    //request
    //==============================================
    private OnResponseListener.SimpleResponseListener<List<MomentInfo>> momentsRequestCallBack = new OnResponseListener.SimpleResponseListener<List<MomentInfo>>() {
        @Override
        public void onStart(int requestType) {
            switch (requestType) {
                case REQUEST_REFRESH:

                    break;
                case REQUEST_LOADMORE:
                    break;
            }
        }

        @Override
        public void onSuccess(List<MomentInfo> response, int requestType) {
            Message msg = new Message();
            msg.what = MSG_WHAT_MOMENT_LIST_RESPONSE;
            Bundle bundle = new Bundle();
            String dataStr = gson.toJson(response);
            bundle.putString(MSG_KEY_MOMENT_LIST, dataStr);
            bundle.putInt(MSG_KEY_RESPONSE_TYPE, requestType);
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onError(AppjsException e, int requestType) {
            super.onError(e, requestType);
            circleRecyclerView.compelete();
        }
    };


    //=============================================================View's method
    @Override
    public void onLikeChange(int itemPos, List<LikeInfo> likeUserList) {
//        MomentInfo momentInfo = adapter.findData(itemPos);
//        if (momentInfo != null) {
//            momentInfo.setLikeList(likeUserList);
//            adapter.notifyItemChanged(itemPos);
//        }
        Bundle bundle = new Bundle();
        bundle.putInt("itemPos", itemPos);
        String likeUserListStr = GsonSingleton.getInstance().getGson().toJson(likeUserList);
        bundle.putString("likeUserList", likeUserListStr);
        Message msg = new Message();
        msg.what = MSG_WHAT_LIKE_CHANGE;
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    @Override
    public void onCommentChange(int itemPos, List<CommentInfo> commentInfoList) {
        Bundle bundle = new Bundle();
        bundle.putInt("itemPos", itemPos);
        String commentInfoListStr = GsonSingleton.getInstance().getGson().toJson(commentInfoList);
        bundle.putString("commentInfoList", commentInfoListStr);
        Message msg = new Message();
        msg.what = MSG_WHAT_COMMENT_CHANGE;
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    @Override
    public void showCommentBox(@Nullable View viewHolderRootView, int itemPos, long momentid, CommentWidget commentWidget) {
        if (viewHolderRootView != null) {
            mViewHelper.setCommentAnchorView(viewHolderRootView);
        } else if (commentWidget != null) {
            mViewHelper.setCommentAnchorView(commentWidget);
        }
        mViewHelper.setCommentItemDataPosition(itemPos);
        commentBox.toggleCommentBox(momentid, commentWidget == null ? null : commentWidget.getData(), false);
    }

    @Override
    public void onDeleteMomentsInfo(@NonNull MomentInfo momentInfo) {
        int pos = adapter.getDatas().indexOf(momentInfo);
        if (pos < 0) return;

        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", pos);
        msg.setData(bundle);
        msg.what = MSG_WHAT_DELETE_MOMENT;
        myHandler.sendMessage(msg);
    }

    @Override
    public boolean onPreTouch(MotionEvent ev) {
        if (commentBox != null && commentBox.isShowing()) {
            commentBox.dismissCommentBox(false);
            return true;
        }
        return false;
    }

    //=============================================================tool method
//    private void checkRegister() {
//        boolean hasCheckRegister = (boolean) AppSetting.loadBooleanPreferenceByKey(AppSetting.CHECK_REGISTER, false);
//        if (!hasCheckRegister) {
//            RegisterPopup registerPopup = new RegisterPopup(FriendCircleActivity.this);
//            registerPopup.setOnRegisterSuccess(new RegisterPopup.onRegisterSuccess() {
//                @Override
//                public void onSuccess(UserInfo userInfo) {
//                    hostViewHolder.loadHostData(userInfo);
//                    UpdateInfoManager.INSTANCE.showUpdateInfo();
//                }
//            });
//            registerPopup.showPopupWindow();
//        } else {
//            UpdateInfoManager.INSTANCE.showUpdateInfo();
//        }
//    }

//    private long lastClickBackTime;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //服务器消息检查，非项目所需↓
    private void delayCheckServiceInfo() {
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        checkServiceInfo();
                    }
                });
    }

    private void checkServiceInfo() {
        ServiceInfoManager.INSTANCE.check(new ServiceInfoManager.OnCheckServiceInfoListener() {
            @Override
            public void onCheckFinish(@Nullable final ServiceInfo serviceInfo) {
                if (serviceInfo != null) {
                    mServiceTipsView.setText(serviceInfo.getTips());
                    mServiceTipsView.setOnClickListener(new SingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            ActivityLauncher.startToServiceInfoActivity(FriendCircleActivity.this, serviceInfo);
                            clickServiceCount++;
                            applyClose();
                        }
                    });
                    mTipsLayout.animate()
                            .alpha(1)
                            .translationY(UIHelper.dipToPx(50))
                            .setDuration(800)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimUtils.SimpleAnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    mTipsLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mServiceTipsView.requestFocus();
                                }
                            }).start();
                }
            }
        });
    }

    private void applyClose() {
        if (clickServiceCount < 3) return;
        mCloseImageView.setImageResource(R.drawable.ic_close);
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTipsLayout.animate()
                        .alpha(0)
                        .translationY(0)
                        .setDuration(800)
                        .setInterpolator(new DecelerateInterpolator())
                        .setListener(new AnimUtils.SimpleAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mTipsLayout.setVisibility(View.GONE);
                            }
                        }).start();
            }
        });
    }
    //服务器消息检查，非项目所需↑

    //=============================================================call back
    private CommentBox.OnCommentSendClickListener onCommentSendClickListener = new CommentBox.OnCommentSendClickListener() {
        @Override
        public void onCommentSendClick(View v, IComment comment, String commentContent) {
            if (TextUtils.isEmpty(commentContent)) {
                commentBox.dismissCommentBox(true);
                return;
            }
            int itemPos = mViewHelper.getCommentItemDataPosition();
            if (itemPos < 0 || itemPos > adapter.getItemCount()) return;
            List<CommentInfo> commentInfos = adapter.findData(itemPos).getCommentList();
            long userid = (comment instanceof CommentInfo) ? ((CommentInfo) comment).getAuthor().getId() : 0;
            presenter.addComment(itemPos, commentBox.getMomentid(), userid, commentContent, commentInfos);
            commentBox.clearDraft();
            commentBox.dismissCommentBox(true);
        }
    };


    private class HostViewHolder {
        private View rootView;
        private ImageView friend_wall_pic;
        private ImageView friend_avatar;
        private ImageView message_avatar;
        private TextView message_detail;
        private TextView hostid;

        public HostViewHolder(Context context) {
            this.rootView = LayoutInflater.from(context).inflate(R.layout.circle_host_header, null);
            this.hostid = (TextView) rootView.findViewById(R.id.host_id);
            this.friend_wall_pic = (ImageView) rootView.findViewById(R.id.friend_wall_pic);
            this.friend_avatar = (ImageView) rootView.findViewById(R.id.friend_avatar);
            this.message_avatar = (ImageView) rootView.findViewById(R.id.message_avatar);
            this.message_detail = (TextView) rootView.findViewById(R.id.message_detail);
        }

        public void loadHostData(UserInfo hostInfo) {
            if (hostInfo == null) return;
            if (!FriendCircleActivity.this.isDestroyed()) {
                ImageLoadMnanger.INSTANCE.loadImage(friend_wall_pic, hostInfo.getCover());
                ImageLoadMnanger.INSTANCE.loadImage(friend_avatar, NetConstant.RESOURCES_BASE + hostInfo.getHeadImg());
                hostid.setText(hostInfo.getNickname());
            }
        }

        public View getView() {
            return rootView;
        }

    }

    //    内部类
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_MOMENT_LIST_RESPONSE) {
                Bundle msgBundle = msg.getData();
                String momentListStr = msgBundle.getString(MSG_KEY_MOMENT_LIST);
                int requestType = msgBundle.getInt(MSG_KEY_RESPONSE_TYPE);
                TypeToken<List<MomentInfo>> typeToken = new TypeToken<List<MomentInfo>>() {
                };
                List<MomentInfo> response = gson.fromJson(momentListStr, typeToken.getType());

                circleRecyclerView.compelete();
                switch (requestType) {
                    case REQUEST_REFRESH:
                        if (!ToolUtil.isListEmpty(response)) {
                            KLog.i("firstMomentid", "第一条动态ID   >>>   " + response.get(0).getId());


                            hostViewHolder.loadHostData(LocalHostManager.INSTANCE.getLocalHostUser());
                            adapter.updateData(response);
                        }
                        break;
                    case REQUEST_LOADMORE:
                        adapter.addMore(response);
                        break;
                }
            } else if (MSG_WHAT_GET_PROFILE == msg.what) {
                if (userProfile != null && !TextUtils.isEmpty(userProfile.getHeadImg())) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setId(userProfile.getUserId());
                    userInfo.setHeadImg(userProfile.getHeadImg());
                    userInfo.setUsername(userProfile.getNickname());
                    userInfo.setNickname(userProfile.getNickname());
                    userInfo.setCover(CustomConstant.COVER_DEMO);
                    LocalHostManager.INSTANCE.updateLocalHost(userInfo);
                    hostViewHolder.loadHostData(LocalHostManager.INSTANCE.getLocalHostUser());
//                    UpdateInfoManager.INSTANCE.showUpdateInfo();
                }
            } else if (MSG_WHAT_LIKE_CHANGE == msg.what) {
                Bundle likeChangeBundle = msg.getData();
                int itemPos = likeChangeBundle.getInt("itemPos");
                String likeUserList = likeChangeBundle.getString("likeUserList");
                updateLikeUI(itemPos, likeUserList);
            } else if (MSG_WHAT_DELETE_MOMENT == msg.what) {
                int pos = msg.getData().getInt("pos");
                adapter.deleteData(pos);
            } else if (MSG_WHAT_COMMENT_CHANGE == msg.what) {
                int itemPos = msg.getData().getInt("itemPos");
                String commentInfoListStr = msg.getData().getString("commentInfoList");
                Type typeValue = new TypeToken<List<CommentInfo>>() {
                }.getType();
                List<CommentInfo> commentInfoList = GsonSingleton.getInstance().getGson()
                        .fromJson(commentInfoListStr, typeValue);

                MomentInfo momentInfo = adapter.findData(itemPos);
                if (momentInfo != null) {
                    momentInfo.setCommentList(commentInfoList);
                    adapter.notifyItemChanged(itemPos);
                }
            }

            //
        }
    }

    private void updateLikeUI(int itemPos, String likeUserList) {
        MomentInfo momentInfo = adapter.findData(itemPos);
        if (momentInfo != null) {
            Type listType = new TypeToken<List<LikeInfo>>() {
            }.getType();
            List<LikeInfo> likeInfoListValue = GsonSingleton.getInstance().getGson().fromJson(likeUserList, listType);
            momentInfo.setLikeList(likeInfoListValue);
            adapter.notifyItemChanged(itemPos);
        }
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

    private void doGetProfile(long userId, String token) {
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

        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(MY_TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    final String responseStr = response.body().string();

                    UserProfile userProfile = GsonSingleton.getInstance().getGson().fromJson(responseStr, UserProfile.class);
                    FriendCircleActivity.this.setUserProfile(userProfile);
                    myHandler.sendEmptyMessage(MSG_WHAT_GET_PROFILE);
                }
            }
        });
    }
}
