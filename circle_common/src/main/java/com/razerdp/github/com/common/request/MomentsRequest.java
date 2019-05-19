package com.razerdp.github.com.common.request;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikeInfo;
import com.razerdp.github.com.common.entity.MomentInfo;

import java.util.List;

import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.network.base.BaseRequestClient;


/**
 * Created by liushaoming on 2016/10/27.
 * <p>
 * 朋友圈时间线请求
 */

public class MomentsRequest extends BaseRequestClient<List<MomentInfo>> {

    private int count = 10;
    private int curPage = 0;

    private static boolean isFirstRequest = true;

    public MomentsRequest() {
    }

    public MomentsRequest setCount(int count) {
        this.count = (count <= 0 ? 10 : count);
        return this;
    }

    public MomentsRequest setCurPage(int page) {
        this.curPage = page;
        return this;
    }


    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
//        BmobQuery<MomentInfo> query = new BmobQuery<>();
//        query.order("-createdAt");
//        query.include(MomentsFields.AUTHOR_USER + "," + MomentsFields.HOST);
//        query.setLimit(count);
//        query.setSkip(curPage * count);
//        query.setCachePolicy(isFirstRequest? BmobQuery.CachePolicy.CACHE_ELSE_NETWORK: BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
//        query.findObjects(new FindListener<MomentInfo>() {
//            @Override
//            public void done(List<MomentInfo> list, BmobException e) {
//                if (!ToolUtil.isListEmpty(list)) {
//                    queryCommentAndLikes(list);
//                }
//            }
//        });

    }

    private void queryCommentAndLikes(final List<MomentInfo> momentsList) {
        /**
         * 因为bmob不支持在查询时把关系表也一起填充查询，因此需要手动再查一次，同时分页也要手动实现。。
         * oRz，果然没有自己写服务器来的简单，好吧，都是在下没钱的原因，我的锅
         */
//        final List<CommentInfo> commentInfoList = new ArrayList<>();
//        final List<LikeInfo> likesInfoList = new ArrayList<>();
//
//        final boolean[] isCommentRequestFin = {false};
//        final boolean[] isLikesRequestFin = {false};
//
//        BmobQuery<CommentInfo> commentQuery = new BmobQuery<>();
//        commentQuery.include(MOMENT + "," + REPLY_USER + "," + AUTHOR_USER);
//        List<String> id = new ArrayList<>();
////        for (MomentInfo momentsInfo : momentsList) {
////            id.add(momentsInfo.getObjectId());
////        }
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


    private void mergeData(boolean isCommentRequestFin,
                           boolean isLikeRequestFin,
                           List<CommentInfo> commentInfoList,
                           List<LikeInfo> likeInfoList,
                           List<MomentInfo> momentsList,
                           AppjsException e) {
        if (!isCommentRequestFin || !isLikeRequestFin) return;
//        if (e != null) {
//            onResponseError(e, getRequestType());
//            return;
//        }
//        if (ToolUtil.isListEmpty(momentsList)) {
//            onResponseError(new BmobException("动态数据为空"), getRequestType());
//            return;
//        }
//        curPage++;
//
//        HashMap<String, MomentInfo> map = new HashMap<>();
//        for (MomentInfo momentsInfo : momentsList) {
//            map.put(momentsInfo.getMomentid(), momentsInfo);
//        }
//
//        for (CommentInfo commentInfo : commentInfoList) {
//            MomentInfo info = map.get(commentInfo.getMomentId().getMomentid());
//            if (info != null) {
//                info.addComment(commentInfo);
//            }
//        }
//
//        for (LikeInfo likesInfo : likeInfoList) {
//            MomentInfo info = map.get(likesInfo.getMomentId());
//            if (info != null) {
//                info.addLikes(likesInfo);
//            }
//        }

        onResponseSuccess(momentsList, getRequestType());

    }

    @Override
    protected void onResponseSuccess(List<MomentInfo> response, int requestType) {
        super.onResponseSuccess(response, requestType);
        isFirstRequest = false;
    }
}
