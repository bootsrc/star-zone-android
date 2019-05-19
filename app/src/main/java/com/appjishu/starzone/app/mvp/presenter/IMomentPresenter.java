package com.appjishu.starzone.app.mvp.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikeInfo;
import com.razerdp.github.com.common.entity.MomentInfo;

import java.util.List;

import com.appjishu.starzone.app.mvp.view.IMomentView;
import razerdp.github.com.lib.mvp.IBasePresenter;


/**
 * Created by liushaoming on 2016/12/7.
 */

public interface IMomentPresenter extends IBasePresenter<IMomentView> {


    void addLike(int viewHolderPos, long momentid, List<LikeInfo> currentLikeList);

    void unLike(int viewHolderPos, long likesid, List<LikeInfo> currentLikeList);

    void addComment(int viewHolderPos, long momentid, long replyUserid, String commentContent, List<CommentInfo> currentCommentList);

    void deleteComment(int viewHolderPos, long commentid, List<CommentInfo> currentCommentList);

    void deleteMoments(Context context,@NonNull MomentInfo momentInfo);

}
