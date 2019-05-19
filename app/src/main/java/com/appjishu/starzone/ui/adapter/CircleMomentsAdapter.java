package com.appjishu.starzone.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.razerdp.github.com.common.entity.MomentInfo;

import java.util.List;

import com.appjishu.starzone.app.mvp.presenter.impl.MomentPresenter;
import com.appjishu.starzone.ui.viewholder.CircleBaseViewHolder;
import razerdp.github.com.ui.base.adapter.BaseMultiRecyclerViewAdapter;
import razerdp.github.com.ui.base.adapter.BaseRecyclerViewHolder;

/**
 * Created by liushaoming on 2016/11/1.
 * <p>
 * 朋友圈adapter
 */

public class CircleMomentsAdapter extends BaseMultiRecyclerViewAdapter<CircleMomentsAdapter, MomentInfo> {

    private MomentPresenter momentPresenter;

    public CircleMomentsAdapter(@NonNull Context context, @NonNull List<MomentInfo> datas, MomentPresenter presenter) {
        super(context, datas);
        this.momentPresenter = presenter;
    }

    @Override
    protected void onInitViewHolder(BaseRecyclerViewHolder holder) {
        if (holder instanceof CircleBaseViewHolder) {
            ((CircleBaseViewHolder) holder).setPresenter(momentPresenter);
        }
    }
}
