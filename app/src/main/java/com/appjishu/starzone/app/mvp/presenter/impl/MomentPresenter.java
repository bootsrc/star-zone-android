package com.appjishu.starzone.app.mvp.presenter.impl;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.appjishu.starzone.model.ResponseData;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikeInfo;
import com.razerdp.github.com.common.entity.MomentInfo;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.socks.library.KLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import cn.bmob.v3.datatype.BmobFile;
//import cn.bmob.v3.exception.BmobException;
//import cn.bmob.v3.listener.UpdateListener;
import com.appjishu.starzone.app.mvp.callback.OnCommentChangeCallback;
import com.appjishu.starzone.app.mvp.callback.OnLikeChangeCallback;
import com.appjishu.starzone.app.mvp.presenter.IMomentPresenter;
import com.appjishu.starzone.app.mvp.view.IMomentView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.lib.mvp.IBasePresenter;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.widget.commentwidget.CommentWidget;

/**
 * Created by liushaoming on 2016/12/7.
 * <p>
 * 朋友圈presenter
 */

public class MomentPresenter implements IMomentPresenter {
    private IMomentView momentView;
    private CommentImpl commentModel;
    private LikeImpl likeModel;
    private OkHttpClient httpClient;

    public MomentPresenter() {
        this(null);
    }

    public MomentPresenter(IMomentView momentView) {
//        httpClient = new OkHttpClient();
        httpClient = MySslUtil.newOkHttpClient();
        this.momentView = momentView;
        commentModel = new CommentImpl();
        likeModel = new LikeImpl();
    }

    @Override
    public IBasePresenter<IMomentView> bindView(IMomentView view) {
        this.momentView = view;
        return this;
    }

    @Override
    public IBasePresenter<IMomentView> unbindView() {
        return this;
    }

    //=============================================================动作控制
    @Override
    public void addLike(final int viewHolderPos, final long momentid, final List<LikeInfo> currentLikeList) {
        likeModel.addLike(momentid, new OnLikeChangeCallback() {
            @Override
            public void onLike(String likeinfoid) {
                List<LikeInfo> resultLikeList = new ArrayList<LikeInfo>();
                if (!ToolUtil.isListEmpty(currentLikeList)) {
                    resultLikeList.addAll(currentLikeList);
                }
                boolean hasLocalLiked = findLikeInfoPosByUserid(resultLikeList, LocalHostManager.INSTANCE.getUserid()) > -1;
                if (!hasLocalLiked && !TextUtils.isEmpty(likeinfoid)) {
                    LikeInfo info = new LikeInfo();
//                    info.setObjectId(likeinfoid);
//                    info.setMomentId(momentid);
                    info.setUserInfo(LocalHostManager.INSTANCE.getLocalHostUser());
                    resultLikeList.add(info);
                }
                if (momentView != null) {
                    momentView.onLikeChange(viewHolderPos, resultLikeList);
                }
            }

            @Override
            public void onUnLike() {

            }

        });
    }

    @Override
    public void unLike(final int viewHolderPos, long momentId, final List<LikeInfo> currentLikeList) {
        likeModel.unLike(momentId, new OnLikeChangeCallback() {
            @Override
            public void onLike(String likeinfoid) {

            }

            @Override
            public void onUnLike() {
                List<LikeInfo> resultLikeList = new ArrayList<LikeInfo>();
                if (!ToolUtil.isListEmpty(currentLikeList)) {
                    resultLikeList.addAll(currentLikeList);
                }
                final int localLikePos = findLikeInfoPosByUserid(resultLikeList, LocalHostManager.INSTANCE.getUserid());
                if (localLikePos > -1) {
                    resultLikeList.remove(localLikePos);
                }
                if (momentView != null) {
                    momentView.onLikeChange(viewHolderPos, resultLikeList);
                }
            }

        });
    }

    @Override
    public void addComment(final int viewHolderPos, long momentid, long replyUserid, String commentContent, final List<CommentInfo> currentCommentList) {
        if (TextUtils.isEmpty(commentContent)) return;
        commentModel.addComment(momentid, LocalHostManager.INSTANCE.getUserid(), replyUserid, commentContent, new OnCommentChangeCallback() {
            @Override
            public void onAddComment(CommentInfo response) {
                List<CommentInfo> commentList = new ArrayList<CommentInfo>();
                if (!ToolUtil.isListEmpty(currentCommentList)) {
                    commentList.addAll(currentCommentList);
                }
                commentList.add(response);
                KLog.i("comment", "评论成功 >>>  " + response.toString());
                if (momentView != null) {
                    momentView.onCommentChange(viewHolderPos, commentList);
                }

            }

            @Override
            public void onDeleteComment(String response) {

            }
        });
    }

    @Override
    public void deleteComment(final int viewHolderPos, long commentid, final List<CommentInfo> currentCommentList) {
        if (commentid == 0) return;
        commentModel.deleteComment(commentid, new OnCommentChangeCallback() {
            @Override
            public void onAddComment(CommentInfo response) {

            }

            @Override
            public void onDeleteComment(String response) {
                if (TextUtils.isEmpty(response)) return;
                List<CommentInfo> resultLikeList = new ArrayList<CommentInfo>();
                if (!ToolUtil.isListEmpty(currentCommentList)) {
                    resultLikeList.addAll(currentCommentList);
                }
                Iterator<CommentInfo> iterator = resultLikeList.iterator();
                while (iterator.hasNext()) {
                    CommentInfo info = iterator.next();
                    if (response.equals(info.getId() + "")) {
                        iterator.remove();
                        break;
                    }
                }
                KLog.i("comment", "删除评论成功 >>>  " + response);
                if (momentView != null) {
                    momentView.onCommentChange(viewHolderPos, resultLikeList);
                }

            }
        });

    }

    @Override
    public void deleteMoments(Context context, @NonNull final MomentInfo momentInfo) {
        assert momentInfo != null : "momentsInfo为空";
        new AlertDialog.Builder(context)
                .setTitle("删除动态")
                .setMessage("确定删除吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        momentInfo.delete(new UpdateListener() {
//                            @Override
//                            public void done(AppjsException e) {
//                                if (e == null && momentView != null) {
//                                    deleteFiles(momentInfo);
//                                    momentView.onDeleteMomentsInfo(momentInfo);
//                                } else {
//                                    UIHelper.ToastMessage("删除失败");
//                                }
//                            }
//                        });

                        long userId = AppSetting.getUserId();
                        String token = AppSetting.getToken();

                        FormBody formBody = new FormBody
                                .Builder()
                                .add("id", momentInfo.getId() + "")
                                .build();
                        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.DELETE_MOMENT;
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

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.code() == 200) {
                                    ResponseData responseData= GsonSingleton.getInstance().getGson().fromJson(
                                            response.body().string(), ResponseData.class);

                                    if (responseData.getCode() == 0 &&
                                            "OK".equals(responseData.getMsg()) && momentView != null) {
                                        deleteFiles(momentInfo);
                                        momentView.onDeleteMomentsInfo(momentInfo);
                                    } else {
                                        UIHelper.ToastMessage("删除失败");
                                    }
                                }
                            }
                        });

                    }
                }).show();
    }

    private void deleteFiles(MomentInfo momentInfo) {
        if (momentInfo == null) return;
        final List<String> pics = momentInfo.getContent().getPics();
        if (ToolUtil.isListEmpty(pics)) return;
        // TODO 删除了说说动态后，需要接着删除动态里服务器上的图片文件，否则会造成很多
        // 垃圾文件
//        for (final String pic : pics) {
//            BmobFile file = new BmobFile();
//            file.setUrl(pic);
//            file.delete(new UpdateListener() {
//                @Override
//                public void done(BmobException e) {
//                    if (e == null) {
//                        KLog.d("delPic", "文件删除成功 : " + pic);
//                    } else {
//                        KLog.d("delPic", "文件删除失败：" + e.getErrorCode() + "," + e.getMessage());
//                    }
//                }
//            });
//        }
    }


    public void showCommentBox(@Nullable View viewHolderRootView, int itemPos, long momentid, @Nullable CommentWidget commentWidget) {
        if (momentView != null) {
            momentView.showCommentBox(viewHolderRootView, itemPos, momentid, commentWidget);
        }
    }


    private int findLikeInfoPosByUserid(List<LikeInfo> infoList, long id) {

        int result = -1;
        if (ToolUtil.isListEmpty(infoList)) return result;
        for (int i = 0; i < infoList.size(); i++) {
            LikeInfo info = infoList.get(i);
            if (info.getUserInfo().getId() == id) {
                result = i;
                break;
            }
        }
        return result;
    }


    //------------------------------------------interface impl-----------------------------------------------
}
