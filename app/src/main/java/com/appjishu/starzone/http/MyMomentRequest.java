package com.appjishu.starzone.http;

import com.razerdp.github.com.common.constant.NetConstant;
import com.appjishu.starzone.util.ObjectConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.razerdp.github.com.common.entity.MomentInfo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import razerdp.github.com.lib.network.base.BaseRequestClient;
import razerdp.github.com.lib.network.ssl.MySslUtil;

public class MyMomentRequest extends BaseRequestClient<List<MomentInfo>> {

    private int count = 10;
    private int curPage = 0;

    private static boolean isFirstRequest = true;
    private OkHttpClient httpClient = null;
    private Gson gson = new Gson();
    private long userId;
    private String token;

    public MyMomentRequest(long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
//            httpClient = new OkHttpClient.Builder()
//                    .connectTimeout(5, TimeUnit.SECONDS)
//                    .writeTimeout(10, TimeUnit.SECONDS)
//                    .readTimeout(30, TimeUnit.SECONDS)
//                    .build();
            httpClient = MySslUtil.newOkHttpClient();
        }
        return httpClient;
    }

    public MyMomentRequest setCount(int count) {
        this.count = (count <= 0 ? 10 : count);
        return this;
    }

    public MyMomentRequest setCurPage(int page) {
        this.curPage = page;
        return this;
    }

    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
        String requestUrl = NetConstant.URL_BASE_HTTPS + NetConstant.MOMENT_BY_PAGE;
        String pageStr = this.curPage + "";
        FormBody formBody = new FormBody
                .Builder()
                .add(NetConstant.PAGE, pageStr)
                .add(NetConstant.LIMIT, this.count + "")
                .build();
        final Request request = new Request.Builder()
                .url(requestUrl)
                .post(formBody)
                .addHeader(NetConstant.HTTP_HEADER_KEY_USER_ID, userId + "")
                .addHeader(NetConstant.HTTP_HEADER_KEY_TOKEN, token)
                .build();
        Call call = getHttpClient().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String responseStr = response.body().string();

                    TypeToken<List<MomentInfo>> typeToken = new TypeToken<List<MomentInfo>>() {
                    };
                    List<MomentInfo> momentList = gson.fromJson(responseStr, typeToken.getType());
//                    List<MomentInfo> momentInfoListValue = new  ArrayList<MomentInfo>();
//                    if (voList != null && voList.size() > 0) {
//                        for (MomentMobileVO vo : voList) {
//                            vo.setDeletable(CustomConstant.userId == vo.getUserId());
//                            MomentInfo info = ObjectConverter.momentMVO2Info(vo);
//                            momentInfoListValue.add(info);
//                        }
//                    }

                    // TODO deletable需要设置
                    if (momentList != null && momentList.size() > 0) {
                        curPage++;
                        for (MomentInfo momentInfo : momentList) {
                            if (momentInfo != null) {
                                ObjectConverter.processMoment(momentInfo);
                                if (momentInfo.getAuthor() != null && momentInfo.getAuthor().getId() > 0) {
                                    momentInfo.setDeletable(userId == momentInfo.getAuthor().getId());
                                }
                            }
                        }
                    }

                    onResponseSuccess(momentList, getRequestType());
                }
            }
        });

    }

    private void queryCommentAndLikes(final List<MomentInfo> momentsList) {
//        /**
//         * 因为bmob不支持在查询时把关系表也一起填充查询，因此需要手动再查一次，同时分页也要手动实现。。
//         * oRz，果然没有自己写服务器来的简单，好吧，都是在下没钱的原因，我的锅
//         */
//        final List<CommentInfo> commentInfoList = new ArrayList<>();
//        final List<LikeInfo> likesInfoList = new ArrayList<>();
//
//        final boolean[] isCommentRequestFin = {false};
//        final boolean[] isLikesRequestFin = {false};
//
//        BmobQuery<CommentInfo> commentQuery = new BmobQuery<>();
//        commentQuery.include(MOMENT + "," + REPLY_USER + "," + AUTHOR_USER);
//        List<String> id = new ArrayList<>();
//        for (MomentInfo momentsInfo : momentsList) {
//            id.add(momentsInfo.getObjectId());
//        }
//        commentQuery.addWhereContainedIn(MOMENT, id);
//        commentQuery.order("createdAt");
//        commentQuery.setLimit(1000);//默认只有100条数据，最多1000条
//        commentQuery.setCachePolicy(isFirstRequest? BmobQuery.CachePolicy.CACHE_ELSE_NETWORK: BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
//        commentQuery.findObjects(new FindListener<CommentInfo>() {
//            @Override
//            public void done(List<CommentInfo> list, BmobException e) {
//                isCommentRequestFin[0] = true;
//                if (!ToolUtil.isListEmpty(list)) {
//                    commentInfoList.addAll(list);
//                }
//                mergeData(isCommentRequestFin[0], isLikesRequestFin[0], commentInfoList, likesInfoList, momentsList, e);
//            }
//        });
//
//        BmobQuery<LikeInfo> likesInfoBmobQuery = new BmobQuery<>();
//        likesInfoBmobQuery.include(LikeInfo.LikesField.MOMENTID + "," + LikeInfo.LikesField.USERID);
//        likesInfoBmobQuery.addWhereContainedIn(LikeInfo.LikesField.MOMENTID, id);
//        likesInfoBmobQuery.order("createdAt");
//        likesInfoBmobQuery.setLimit(1000);
//        likesInfoBmobQuery.setCachePolicy(isFirstRequest? BmobQuery.CachePolicy.CACHE_ELSE_NETWORK: BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
//        likesInfoBmobQuery.findObjects(new FindListener<LikeInfo>() {
//            @Override
//            public void done(List<LikeInfo> list, BmobException e) {
//                isLikesRequestFin[0] = true;
//                if (!ToolUtil.isListEmpty(list)) {
//                    likesInfoList.addAll(list);
//                }
//                mergeData(isCommentRequestFin[0], isLikesRequestFin[0], commentInfoList, likesInfoList, momentsList, e);
//            }
//        });

    }

    @Override
    protected void onResponseSuccess(List<MomentInfo> response, int requestType) {
        super.onResponseSuccess(response, requestType);
        isFirstRequest = false;
    }
}

