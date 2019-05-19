package com.razerdp.github.com.common.request;

import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.network.base.OnResponseListener;
import razerdp.github.com.ui.util.UIHelper;

/**
 * Created by liushaoming on 2016/10/28.
 */

public abstract class SimpleResponseListener<T> implements OnResponseListener<T> {

    @Override
    public void onStart(int requestType) {

    }

    @Override
    public void onError(AppjsException e, int requestType) {
        UIHelper.ToastMessage(e.getMessage());
        e.printStackTrace();
    }
}
