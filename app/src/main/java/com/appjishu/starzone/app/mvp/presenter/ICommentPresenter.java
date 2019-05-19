package com.appjishu.starzone.app.mvp.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appjishu.starzone.app.mvp.callback.OnCommentChangeCallback;

/**
 * Created by liushaoming on 2016/12/6.
 */

public interface ICommentPresenter {


    /**
     * 添加评论
     */
    void addComment(@NonNull long momentsId,
                    @NonNull long authorId,
                    @Nullable long replyUserId,
                    @NonNull String content,
                    @NonNull OnCommentChangeCallback onCommentChangeCallback);

    void deleteComment(@NonNull long commentid, @NonNull final OnCommentChangeCallback onCommentChangeCallback);
}
