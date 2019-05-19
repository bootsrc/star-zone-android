package com.appjishu.starzone.ui.viewholder;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.razerdp.github.com.common.entity.CommentInfo;
import com.razerdp.github.com.common.entity.LikeInfo;
import com.razerdp.github.com.common.entity.MomentInfo;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.socks.library.KLog;

import java.util.List;

import com.appjishu.starzone.R;
import com.appjishu.starzone.app.mvp.presenter.impl.MomentPresenter;
import com.appjishu.starzone.ui.widget.popup.CommentPopup;
import com.appjishu.starzone.ui.widget.popup.DeleteCommentPopup;
import com.appjishu.starzone.ui.widget.praisewidget.PraiseWidget;
import razerdp.github.com.lib.utils.StringUtil;
import razerdp.github.com.lib.utils.TimeUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.base.adapter.BaseMultiRecyclerViewHolder;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.util.ViewUtil;
import razerdp.github.com.ui.widget.commentwidget.CommentContentsLayout;
import razerdp.github.com.ui.widget.commentwidget.CommentWidget;
import razerdp.github.com.ui.widget.commentwidget.IComment;
import razerdp.github.com.ui.widget.common.ClickShowMoreLayout;

/**
 * Created by liushaoming on 2016/11/1.
 * <p>
 * 朋友圈基本item
 */
public abstract class CircleBaseViewHolder extends BaseMultiRecyclerViewHolder<MomentInfo> implements BaseMomentVH<MomentInfo> {


    //头部
    protected ImageView avatar;
    protected TextView nick;
    protected ClickShowMoreLayout userText;

    //底部
    protected TextView createTime;
    protected TextView deleteMoments;
    protected ImageView commentImage;
    protected FrameLayout menuButton;
    protected LinearLayout commentAndPraiseLayout;
    protected PraiseWidget praiseWidget;
    protected View line;
    protected CommentContentsLayout commentLayout;

    //内容区
    protected LinearLayout contentLayout;

    private CommentPopup commentPopup;
    private DeleteCommentPopup deleteCommentPopup;

    private MomentPresenter momentPresenter;
    private int itemPosition;
    private MomentInfo momentInfo;


    public CircleBaseViewHolder(View itemView, int viewType) {
        super(itemView, viewType);
        onFindView(itemView);

        //header
        avatar = (ImageView) findView(avatar, R.id.headImg);
        nick = (TextView) findView(nick, R.id.nickname);
        userText = (ClickShowMoreLayout) findView(userText, R.id.item_text_field);
        userText.setOnStateKeyGenerateListener(new ClickShowMoreLayout.OnStateKeyGenerateListener() {
            @Override
            public int onStateKeyGenerated(int originKey) {
                return originKey + itemPosition;
            }
        });

        //bottom
        createTime = (TextView) findView(createTime, R.id.create_time);
        deleteMoments = (TextView) findView(deleteMoments, R.id.tv_delete_moment);
        commentImage = (ImageView) findView(commentImage, R.id.menu_img);
        menuButton = (FrameLayout) findView(menuButton, R.id.menu_button);
        commentAndPraiseLayout = (LinearLayout) findView(commentAndPraiseLayout, R.id.comment_praise_layout);
        praiseWidget = (PraiseWidget) findView(praiseWidget, R.id.praise);
        line = findView(line, R.id.divider);
        commentLayout = (CommentContentsLayout) findView(commentLayout, R.id.comment_layout);
        commentLayout.setMode(CommentContentsLayout.Mode.EXPANDABLE);
        commentLayout.setOnCommentItemClickListener(onCommentItemClickListener);
        commentLayout.setOnCommentItemLongClickListener(onCommentItemLongClickListener);
        commentLayout.setOnCommentWidgetItemClickListener(onCommentWidgetItemClickListener);
        // FIXME: 2018/1/3 暂时未开发完
//        commentLayout.setMode(CommentContentsLayout.Mode.EXPANDABLE);
        //content
        contentLayout = (LinearLayout) findView(contentLayout, R.id.content);

        if (commentPopup == null) {
            commentPopup = new CommentPopup((Activity) getContext());
            commentPopup.setOnCommentPopupClickListener(onCommentPopupClickListener);
        }

        if (deleteCommentPopup == null) {
            deleteCommentPopup = new DeleteCommentPopup((Activity) getContext());
            deleteCommentPopup.setOnDeleteCommentClickListener(onDeleteCommentClickListener);
        }
    }

    public void setPresenter(MomentPresenter momentPresenter) {
        this.momentPresenter = momentPresenter;
    }

    public MomentPresenter getPresenter() {
        return momentPresenter;
    }

    @Override
    public void onBindData(MomentInfo data, int position) {
        if (data == null) {
            KLog.e("数据是空的！！！！");
            findView(userText, R.id.item_text_field);
            userText.setText("这个动态的数据是空的。。。。OMG");
            return;
        }
        this.momentInfo = data;
        this.itemPosition = position;
        //通用数据绑定
        onBindMutualDataToViews(data);
        //点击事件
        menuButton.setOnClickListener(onMenuButtonClickListener);
        menuButton.setTag(R.id.momentinfo_data_tag_id, data);
        deleteMoments.setOnClickListener(onDeleteMomentClickListener);
        //传递到子类
        onBindDataToView(data, position, getViewType());
    }

    private void onBindMutualDataToViews(MomentInfo data) {
        //header
        ImageLoadMnanger.INSTANCE.loadImage(avatar, data.getAuthor().getHeadImg());
        nick.setText(data.getAuthor().getNickname());
        userText.setText(data.getContent().getText());
        ViewUtil.setViewsVisible(StringUtil.noEmpty(data.getContent().getText()) ?
                View.VISIBLE : View.GONE, userText);

        //bottom
        createTime.setText(TimeUtil.getTimeString(data.getCreateTime()));
        ViewUtil.setViewsVisible(momentInfo.getAuthor().getId() == LocalHostManager.INSTANCE.getUserid() ?
                View.VISIBLE : View.GONE, deleteMoments);
        boolean needPraiseData = addLikes(data.getLikeList());
        boolean needCommentData = commentLayout.addComments(data.getCommentList());
        praiseWidget.setVisibility(needPraiseData ? View.VISIBLE : View.GONE);
        commentLayout.setVisibility(needCommentData ? View.VISIBLE : View.GONE);
        line.setVisibility(needPraiseData && needCommentData ? View.VISIBLE : View.GONE);
        commentAndPraiseLayout.setVisibility(needCommentData || needPraiseData ? View.VISIBLE : View.GONE);

    }


    /**
     * 添加点赞
     *
     * @param likesList
     * @return ture=显示点赞，false=不显示点赞
     */
    private boolean addLikes(List<LikeInfo> likesList) {
        if (ToolUtil.isListEmpty(likesList)) {
            return false;
        }
        praiseWidget.setDatas(likesList);
        return true;
    }

    /**
     * ==================  click listener block
     */

    private CommentContentsLayout.OnCommentWidgetItemClickListener onCommentWidgetItemClickListener = new CommentContentsLayout.OnCommentWidgetItemClickListener() {
        @Override
        public void onCommentItemClicked(@NonNull IComment comment, CharSequence text) {
            if (comment instanceof CommentInfo) {
                UIHelper.ToastMessage("点击的用户 ： 【 " + text + " 】");
            }
        }
    };

    private CommentContentsLayout.OnCommentItemClickListener onCommentItemClickListener = new CommentContentsLayout.OnCommentItemClickListener() {
        @Override
        public void onCommentWidgetClick(@NonNull CommentWidget widget) {
            IComment comment = widget.getData();
            CommentInfo commentInfo = null;
            if (comment instanceof CommentInfo) {
                commentInfo = (CommentInfo) comment;
            }
            if (commentInfo == null) return;
            if (commentInfo.canDelete()) {
                deleteCommentPopup.showPopupWindow(commentInfo);
            } else {
                momentPresenter.showCommentBox(null, itemPosition, momentInfo.getId(), widget);
            }
        }
    };

    private CommentContentsLayout.OnCommentItemLongClickListener onCommentItemLongClickListener = new CommentContentsLayout.OnCommentItemLongClickListener() {
        @Override
        public boolean onCommentWidgetLongClick(@NonNull CommentWidget widget) {
            return false;
        }
    };

    private DeleteCommentPopup.OnDeleteCommentClickListener onDeleteCommentClickListener = new DeleteCommentPopup.OnDeleteCommentClickListener() {
        @Override
        public void onDelClick(CommentInfo commentInfo) {
            momentPresenter.deleteComment(itemPosition, commentInfo.getId(), momentInfo.getCommentList());
        }
    };

    private View.OnClickListener onDeleteMomentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            momentPresenter.deleteMoments(v.getContext(), momentInfo);
        }
    };


    private View.OnClickListener onMenuButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MomentInfo info = (MomentInfo) v.getTag(R.id.momentinfo_data_tag_id);
            if (info != null) {
                commentPopup.updateMomentInfo(info);
                commentPopup.showPopupWindow(commentImage);
            }
        }
    };


    private CommentPopup.OnCommentPopupClickListener onCommentPopupClickListener = new CommentPopup.OnCommentPopupClickListener() {
        @Override
        public void onLikeClick(View v, @NonNull MomentInfo info, boolean hasLiked) {
            if (hasLiked) {
                momentPresenter.unLike(itemPosition, info.getId(), info.getLikeList());
            } else {
                momentPresenter.addLike(itemPosition, info.getId(), info.getLikeList());
            }

        }

        @Override
        public void onCommentClick(View v, @NonNull MomentInfo info) {
            momentPresenter.showCommentBox(itemView, itemPosition, info.getId(), null);
        }
    };

    /**
     * ============  tools method block
     */


    protected final View findView(View view, int resid) {
        if (resid > 0 && itemView != null && view == null) {
            return itemView.findViewById(resid);
        }
        return view;
    }


}
