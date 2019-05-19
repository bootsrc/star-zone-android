package com.appjishu.starzone.app.mvp.presenter.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.request.AddCommentRequest;
import com.razerdp.github.com.common.request.DeleteCommentRequest;
import com.razerdp.github.com.common.request.SimpleResponseListener;

import com.appjishu.starzone.app.mvp.callback.OnCommentChangeCallback;
import com.appjishu.starzone.app.mvp.presenter.ICommentPresenter;

/**
 * Created by liushaoming on 2016/12/7.
 * <p>
 * 评论Model
 */

public class CommentImpl implements ICommentPresenter {
    @Override
    public void addComment(@NonNull long momentid,
                           @NonNull long authorid,
                           @Nullable long replyUserId,
                           @NonNull String content,
                           @NonNull final OnCommentChangeCallback onCommentChangeCallback) {
        if (onCommentChangeCallback == null) return;
        AddCommentRequest addCommentRequest = new AddCommentRequest();
        addCommentRequest.setContent(content);
        addCommentRequest.setMomentsInfoId(momentid);
        addCommentRequest.setAuthorId(authorid);
        if (replyUserId > 0) {
            addCommentRequest.setReplyUserId(replyUserId);
        }
        addCommentRequest.setOnResponseListener(new SimpleResponseListener<CommentInfo>() {
            @Override
            public void onSuccess(CommentInfo response, int requestType) {
                onCommentChangeCallback.onAddComment(response);
            }
        });
        addCommentRequest.execute();
    }

    @Override
    public void deleteComment(long commentid, @NonNull final OnCommentChangeCallback onCommentChangeCallback) {
        if (onCommentChangeCallback == null) return;
        DeleteCommentRequest deleteCommentRequest = new DeleteCommentRequest();
        deleteCommentRequest.setCommentid(commentid);
        deleteCommentRequest.setOnResponseListener(new SimpleResponseListener<String>() {
            @Override
            public void onSuccess(String response, int requestType) {
                onCommentChangeCallback.onDeleteComment(response);
            }
        });
        deleteCommentRequest.execute();

    }
}
