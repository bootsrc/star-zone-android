package com.appjishu.starzone.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.UserProfile;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.UserInfo;

import java.util.List;

import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{
    private List<UserProfile> dataList;
    private OnRecyclerViewItemClickListener<UserProfile> listener;

    public FriendAdapter(List<UserProfile> dataList, OnRecyclerViewItemClickListener<UserProfile> listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_friend, parent, false);
        FriendAdapter.ViewHolder viewHolder = new FriendAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendAdapter.ViewHolder holder, final int position) {
        final UserProfile user = dataList.get(position);
        String headImgValue = user.getHeadImg();
        if (!TextUtils.isEmpty(headImgValue)) {
            ImageLoadMnanger.INSTANCE.loadImage(holder.headView,
                    NetConstant.RESOURCES_BASE + headImgValue);
        }
        holder.nameView.setText(user.getNickname());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.itemView, position, user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private ImageView headView;
        private TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            headView = itemView.findViewById(R.id.target_head_image_view);
            nameView = itemView.findViewById(R.id.friend_nickname);
        }
    }
}
