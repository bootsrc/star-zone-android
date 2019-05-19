package com.razerdp.github.com.common.entity;

import com.razerdp.github.com.common.MomentsType;
import com.socks.library.KLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import razerdp.github.com.ui.base.adapter.MultiType;

/**
 * Created by liushaoming on 2016/10/27.
 * <p>
 * 朋友圈动态
 */

public class MomentInfo implements MultiType, Serializable {

    @Override
    public int getItemType() {
        return getMomentType();
    }

    private long id;
    private long createTime;
    private UserInfo author;
//    private UserInfo hostinfo;
    private List<LikeInfo> likeList;
    private List<CommentInfo> commentList;
    private MomentContent content;
    private boolean deletable;

    public MomentInfo() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public UserInfo getAuthor() {
        return author;
    }

    public void setAuthor(UserInfo author) {
        this.author = author;
    }

    public List<LikeInfo> getLikeList() {
        return likeList;
    }

    public void setLikeList(List<LikeInfo> likeList) {
        this.likeList = likeList;
    }

    public List<CommentInfo> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<CommentInfo> commentList) {
        this.commentList = commentList;
    }

    public MomentContent getContent() {
        return content;
    }

    public void setContent(MomentContent content) {
        this.content = content;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public void addComment(CommentInfo commentInfo) {
        if (commentInfo == null) return;
        if (this.commentList == null) {
            this.commentList = new ArrayList<>();
        }
        this.commentList.add(commentInfo);
    }

    public void addLikes(LikeInfo likeInfo) {
        if (likeInfo == null) return;
        if (this.likeList == null) {
            this.likeList = new ArrayList<>();
        }
        this.likeList.add(likeInfo);
    }

    public int getMomentType() {
        if (content == null) {
            KLog.e("朋友圈是空的");
            return MomentsType.EMPTY_CONTENT;
        }
        return content.getMomentType();
    }
}
