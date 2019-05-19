package com.razerdp.github.com.common.request;

import com.google.gson.Gson;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.model.ResponseData;

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
 * Created by liushaoming on 2016/12/13.
 * <p>
 * 删除评论
 */

public class DeleteCommentRequest extends BaseRequestClient<String> {
    private OkHttpClient httpClient;
    private long commentid;
    private Gson gson;

    public DeleteCommentRequest() {
//        httpClient = new OkHttpClient();
        httpClient = MySslUtil.newOkHttpClient();
    }

    public void setCommentid(long commentid) {
        this.commentid = commentid;
        gson = new Gson();
    }

    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
//        if (TextUtils.isEmpty(commentid)) {
//            onResponseError(new AppjsException("评论ID为空"), getRequestType());
//            return;
//        }
//        CommentInfo commentInfo = new CommentInfo();
//        commentInfo.setObjectId(commentid);
//        commentInfo.delete(new UpdateListener() {
//            @Override
//            public void done(BmobException e) {
//                if (e == null) {
//                    onResponseSuccess(commentid, requestType);
//                } else {
//                    onResponseError(e, requestType);
//                }
//            }
//        });

        if (commentid < 1) {
            onResponseError(new AppjsException("评论ID为空"), getRequestType());
            return;
        }

        long userId = AppSetting.getUserId();
        String token = AppSetting.getToken();
        FormBody formBody = new FormBody
                .Builder()
                .add("commentId", this.commentid +"")
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.MOMENT_DELETE_ONE_COMMENT;
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
                    ResponseData responseData = gson.fromJson(response.body().string(),
                            ResponseData.class);
                    if (responseData!=null && responseData.getData() instanceof Double
                            && (double) responseData.getData() > 0){
                        onResponseSuccess(commentid + "", requestType);
                    }
                }
            }
        });
    }
}
