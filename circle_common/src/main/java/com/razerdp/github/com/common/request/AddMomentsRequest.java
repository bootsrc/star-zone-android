package com.razerdp.github.com.common.request;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.MomentContent;
import com.razerdp.github.com.common.entity.MomentParam;
import com.razerdp.github.com.common.entity.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import cn.bmob.v3.datatype.BmobRelation;
//import cn.bmob.v3.exception.BmobException;
//import cn.bmob.v3.listener.SaveListener;
//import cn.bmob.v3.listener.UpdateListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.network.base.BaseRequestClient;
import razerdp.github.com.lib.network.ssl.MySslUtil;

/**
 * Created by liushaoming on 2016/10/28.
 * <p>
 * 添加动态(暂时不处理文件上传)
 */

public class AddMomentsRequest extends BaseRequestClient<String> {

    private long authId;
    private String token;
//    private String hostId;
    private MomentContent momentContent;
//    private List<UserInfo> likesUserId;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public AddMomentsRequest() {
        momentContent = new MomentContent();
//        likesUserId = new ArrayList<>();
    }

    public AddMomentsRequest setAuthId(long authId) {
        this.authId = authId;
        return this;
    }

    public AddMomentsRequest setToken(String token) {
        this.token =token;
        return this;
    }

//    public AddMomentsRequest setHostId(String hostId) {
//        this.hostId = hostId;
//        return this;
//    }

    public AddMomentsRequest setMomentContent(MomentContent momentContent) {
        this.momentContent = momentContent;
        return this;
    }

    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
//        if (checkValided()) {
//            MomentInfo momentsInfo = new MomentInfo();
//
//            UserInfo author = new UserInfo();
//            author.setObjectId(authId);
//            momentsInfo.setAuthor(author);
//
//            UserInfo host = new UserInfo();
//            host.setObjectId(hostId);
//            momentsInfo.setHostinfo(host);
//
//            momentsInfo.setContent(momentContent);

//            momentsInfo.save(new SaveListener<String>() {
//                @Override
//                public void done(String s, BmobException e) {
//                    if (e == null) {
//                        if (ToolUtil.isListEmpty(likesUserId)) {
//                            onResponseSuccess(s, requestType);
//                        } else {
//                            MomentInfo resultMoment = new MomentInfo();
//                            resultMoment.setObjectId(s);
//
//                            //关联点赞的
//                            BmobRelation relation = new BmobRelation();
//                            for (UserInfo user : likesUserId) {
//                                relation.add(user);
//                            }
//                            resultMoment.setLikesBmobRelation(relation);
//                            resultMoment.update(new UpdateListener() {
//                                @Override
//                                public void done(BmobException e) {
//                                    if (e == null) {
//                                        onResponseSuccess("添加成功", requestType);
//                                    } else {
//                                        onResponseError(e, requestType);
//                                    }
//                                }
//                            });
//                        }
//
//                    }
//                }
//            });
//
//        }

        if (checkValided()) {
//            OkHttpClient httpClient = new OkHttpClient();
            OkHttpClient httpClient = MySslUtil.newOkHttpClient();
            MomentParam momentParam = new MomentParam();
            momentParam.setContent(momentContent.getText());
            momentParam.setImg(momentContent.getPics());
            String json = new Gson().toJson(momentParam);
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);
            Request request = new Request.Builder()
                    .url(NetConstant.URL_BASE_HTTPS + NetConstant.ADD_MOMENT)
                    .addHeader("content-type", "application/json;charset:utf-8")
                    .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, authId + "")
                    .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                    .post(body)
                    .build();

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onResponseError(new AppjsException("上传图片-网络错误"), requestType);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    onResponseSuccess("添加成功", requestType);
                }
            });
        }
    }

    private boolean checkValided() {
        return authId > 0 && momentContent.isValided();
    }

    public AddMomentsRequest addText(String text) {
        momentContent.addText(text);
        return this;
    }

    public AddMomentsRequest addPicture(String pic) {
        momentContent.addPicture(pic);
        return this;
    }

    public AddMomentsRequest addWebUrl(String webUrl) {
        momentContent.addWebUrl(webUrl);
        return this;
    }

    public AddMomentsRequest addWebTitle(String webTitle) {
        momentContent.addWebTitle(webTitle);
        return this;
    }

    public AddMomentsRequest addWebImage(String webImage) {
        momentContent.addWebImage(webImage);
        return this;
    }

    public AddMomentsRequest addLikes(UserInfo user) {
//        likesUserId.add(user);
        return this;
    }
}
