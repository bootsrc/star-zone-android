package com.appjishu.starzone.ui.viewholder;

import android.support.annotation.NonNull;
import android.view.View;

import com.razerdp.github.com.common.entity.MomentInfo;

import com.appjishu.starzone.R;
import razerdp.github.com.ui.base.adapter.LayoutId;


/**
 * Created by liushaoming on 2016/11/3.
 * <p>
 * 空内容的vh
 *
 * @see MomentsType
 */

@LayoutId(id = R.layout.moments_empty_content)
public class EmptyMomentsVH extends CircleBaseViewHolder {


    public EmptyMomentsVH(View itemView, int viewType) {
        super(itemView, viewType);
    }

    @Override
    public void onFindView(@NonNull View rootView) {

    }

    @Override
    public void onBindDataToView(@NonNull MomentInfo data, int position, int viewType) {

    }
}
