package com.appjishu.starzone.app.mvp.presenter;

import com.appjishu.starzone.app.mvp.callback.OnLikeChangeCallback;

/**
 * Created by liushaoming on 2016/12/6.
 */

public interface ILikePresenter {


    /**
     * 添加点赞
     */
    void addLike(long momentid, OnLikeChangeCallback onLikeChangeCallback);

    /**
     * 移除点赞
     */
    void unLike(long momentid, OnLikeChangeCallback onLikeChangeCallback);
}
