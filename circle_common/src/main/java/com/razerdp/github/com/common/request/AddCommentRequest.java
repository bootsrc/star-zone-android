package com.razerdp.github.com.common.request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.CommentInfo;

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
 * Created by liushaoming on 2016/10/28.
 * <p>
 * 添加评论
 */

public class AddCommentRequest extends BaseRequestClient<CommentInfo> {

    private String content;
    private long authorId;
    private long replyUserId;
    private long momentsInfoId;
    private OkHttpClient httpClient;
    private Gson gson;

    public AddCommentRequest() {
        httpClient = new OkHttpClient();
        gson = new Gson();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public void setReplyUserId(long replyUserId) {
        this.replyUserId = replyUserId;
    }

    public void setMomentsInfoId(long momentsInfoId) {
        this.momentsInfoId = momentsInfoId;
    }

    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
//        if (TextUtils.isEmpty(authorId)) {
//            onResponseError(new BmobException("创建用户为空"), getRequestType());
//            return;
//        }
//        if (TextUtils.isEmpty(momentsInfoId)) {
//            onResponseError(new BmobException("动态为空"), getRequestType());
//            return;
//        }
//        CommentInfo commentInfo = new CommentInfo();
//        commentInfo.setContent(content);
//
//        UserInfo author = new UserInfo();
//        author.setObjectId(authorId);
//        commentInfo.setAuthor(author);
//
//        if (StringUtil.noEmpty(replyUserId)) {
//            UserInfo replyUser = new UserInfo();
//            replyUser.setObjectId(replyUserId);
//            commentInfo.setReply(replyUser);
//        }
//
//        MomentInfo momentsInfo = new MomentInfo();
//        momentsInfo.setObjectId(momentsInfoId);
//        commentInfo.setMomentId(momentsInfo);
//
//        commentInfo.save(new SaveListener<String>() {
//            @Override
//            public void done(String s, BmobException e) {
//                if (e == null) {
//                    BmobQuery<CommentInfo> commentQuery = new BmobQuery<CommentInfo>();
//                    commentQuery.include(CommentFields.AUTHOR_USER + "," + CommentFields.REPLY_USER + "," + CommentFields.MOMENT);
//                    commentQuery.getObject(s, new QueryListener<CommentInfo>() {
//                        @Override
//                        public void done(CommentInfo commentInfo, BmobException e) {
//                            if (e == null) {
//                                onResponseSuccess(commentInfo, requestType);
//                            } else {
//                                onResponseError(e, requestType);
//                            }
//                        }
//                    });
//                } else {
//                    onResponseError(e, requestType);
//                }
//            }
//        });

        if (authorId < 1) {
            onResponseError(new AppjsException("创建用户为空"), getRequestType());
            return;
        }
        if (momentsInfoId < 1) {
            onResponseError(new AppjsException("动态为空"), getRequestType());
            return;
        }
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setContent(content);

        //
//        OkHttpClient httpClient = new OkHttpClient();
        OkHttpClient httpClient = MySslUtil.newOkHttpClient();
        long userId = AppSetting.getUserId();
        String token = AppSetting.getToken();
        FormBody formBody = new FormBody
                .Builder()
                .add("commentText", this.content)
                .add("momentId", momentsInfoId + "")
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.MOMENT_ADD_COMMENT;
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
                    CommentInfo commentInfoResult = gson.fromJson(response.body().string(), CommentInfo.class);
                    if (commentInfoResult!=null && commentInfoResult.getId() > 0){
                        onResponseSuccess(commentInfoResult, requestType);
                    }
                }
            }
        });
    }
}
