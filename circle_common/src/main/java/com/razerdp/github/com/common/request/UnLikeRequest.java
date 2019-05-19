package com.razerdp.github.com.common.request;

import com.razerdp.github.com.common.constant.NetConstant;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.lib.network.base.BaseRequestClient;
import razerdp.github.com.lib.network.ssl.MySslUtil;

/**
 * Created by liushaoming on 2016/12/8.
 */

public class UnLikeRequest extends BaseRequestClient<Boolean> {
    private long momentId;

    public UnLikeRequest(long momentId) {
        this.momentId = momentId;
    }

    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
        if (momentId < 1) {
            onResponseError(new AppjsException("likesinfoid < 1"), requestType);
            return;
        }

//        OkHttpClient httpClient = new OkHttpClient();
        OkHttpClient httpClient = MySslUtil.newOkHttpClient();
        long userId = AppSetting.getUserId();
        String token = AppSetting.getToken();
        FormBody formBody = new FormBody
                .Builder()
                .add("momentId", this.momentId + "")
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.LIKE_MOMENT;
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
                    //Handler处理
//                    onResponseSuccess(response.body().string(), requestType);
                    onResponseSuccess(true, requestType);
                }
            }
        });
    }
}
