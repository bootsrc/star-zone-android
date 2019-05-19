package com.appjishu.starzone.app.mvp.callback;


import com.razerdp.github.com.common.entity.CommentInfo;

/**
 * Created by liushaoming on 2016/12/9.
 * <p>
 * 评论Callback
 */

public interface OnCommentChangeCallback {

    void onAddComment(CommentInfo response);

    void onDeleteComment(String response);

}
