package com.appjishu.starzone.ui.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.UserMsg;
import com.razerdp.github.com.common.constant.NetConstant;

import java.util.List;

import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;

public class UserMsgAdapter extends RecyclerView.Adapter<UserMsgAdapter.ViewHolder> {

    private List<UserMsg> dataList;
    private OnRecyclerViewItemClickListener<UserMsg> listener;

//    public UserMsgAdapter(List<UserMsg> dataList, OnRecyclerViewItemClickListener<UserMsg> listener
//            , Activity activity) {
//        this.dataList = dataList;
//        this.listener = listener;
//        this.activity = activity;
//    }

    public UserMsgAdapter(List<UserMsg> dataList, OnRecyclerViewItemClickListener<UserMsg> listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserMsgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_user_msg, parent, false);
        UserMsgAdapter.ViewHolder viewHolder = new UserMsgAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserMsgAdapter.ViewHolder holder, final int position) {
        final UserMsg userMsg = dataList.get(position);
        String headImgValue = userMsg.getTargetProfile().getHeadImg();
//        if (!TextUtils.isEmpty(headImgValue) && activity != null && !activity.isDestroyed()) {
//            ImageLoadMnanger.INSTANCE.loadImage(holder.headView,
//                    NetConstant.RESOURCES_BASE + headImgValue);
//        }
        if (!TextUtils.isEmpty(headImgValue)) {
            ImageLoadMnanger.INSTANCE.loadImage(holder.headView,
                    NetConstant.RESOURCES_BASE + headImgValue);
        }

        holder.nameView.setText(userMsg.getTargetProfile().getNickname());
        holder.msgView.setText(userMsg.getChatMsg().getMsgBody());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.itemView, position, userMsg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //添加一个销毁的方法，这样可以加快回收，可以解决内存泄漏的问题，
    //可以用android studio 进行查看内存，多次开启和关闭程序，查看内存内存走势，
    //如果不填加次方法，那么内存会一直增加，并且点GC 内存也不会下降。
    //在activity的销毁方法中添加该方法，就解决了内存泄漏的方法。(这个方法不是最好的，但目前想到的解决方案只有这个，欢迎指点留言)
    public void onDestroy() {
        dataList.clear();
        dataList = null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private ImageView headView;
        private TextView nameView;
        private TextView msgView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            headView = itemView.findViewById(R.id.target_head_image_view);
            nameView = itemView.findViewById(R.id.friend_nickname);
            msgView = itemView.findViewById(R.id.msg_item_text);
        }
    }
}
