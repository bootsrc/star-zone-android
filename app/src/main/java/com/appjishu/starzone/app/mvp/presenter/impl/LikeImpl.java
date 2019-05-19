package com.appjishu.starzone.app.mvp.presenter.impl;

import com.razerdp.github.com.common.request.AddLikeRequest;
import com.razerdp.github.com.common.request.UnLikeRequest;

import com.appjishu.starzone.app.mvp.callback.OnLikeChangeCallback;
import com.appjishu.starzone.app.mvp.presenter.ILikePresenter;

import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.network.base.OnResponseListener;

/**
 * Created by liushaoming on 2016/12/7.
 * <p>
 * 点赞model
 */

public class LikeImpl implements ILikePresenter {

    @Override
    public void addLike(long momentid, final OnLikeChangeCallback onLikeChangeCallback) {
        if (onLikeChangeCallback == null) return;
        AddLikeRequest request = new AddLikeRequest(momentid);
        request.setOnResponseListener(new OnResponseListener<String>() {
            @Override
            public void onStart(int requestType) {

            }

            @Override
            public void onSuccess(String response, int requestType) {
                onLikeChangeCallback.onLike(response);
            }

            @Override
            public void onError(AppjsException e, int requestType) {

            }
        });
        request.execute();
    }

    @Override
    public void unLike(long momentId, final OnLikeChangeCallback onLikeChangeCallback) {
        if (onLikeChangeCallback == null) return;
        UnLikeRequest request = new UnLikeRequest(momentId);
        request.setOnResponseListener(new OnResponseListener<Boolean>() {
            @Override
            public void onStart(int requestType) {

            }

            @Override
            public void onSuccess(Boolean response, int requestType) {
                if (response) {
                    onLikeChangeCallback.onUnLike();
                }
            }

            @Override
            public void onError(AppjsException e, int requestType) {

            }
        });
        request.execute();
    }
}
