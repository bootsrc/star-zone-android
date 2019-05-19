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
import com.appjishu.starzone.model.Topic;
import com.appjishu.starzone.util.DateUtil;
import com.razerdp.github.com.common.constant.NetConstant;

import java.util.Date;
import java.util.List;

import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {
    private List<Topic> dataList;
    private OnRecyclerViewItemClickListener<Topic> listener;

    public TopicAdapter(List<Topic> dataList, OnRecyclerViewItemClickListener<Topic> listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_topic, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Topic topic = dataList.get(position);
        String imgValue = topic.getImg();
        if (!TextUtils.isEmpty(imgValue)) {
            ImageLoadMnanger.INSTANCE.loadImage(holder.headView,
                    NetConstant.RESOURCES_BASE + imgValue);
        }
        holder.titleView.setText(topic.getTitle());
//        holder.titleView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 listener.onItemClick(holder.itemView, position, topic);
//            }
//        });

        if (topic.getCreateTime()>0) {
            String updateTimeStr = DateUtil.getDateString(new Date(topic.getUpdateTime()));
            holder.timeView.setText(updateTimeStr);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.itemView, position, topic);
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
        private TextView titleView;
        private TextView timeView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            headView = itemView.findViewById(R.id.target_head_image_view);
            titleView = itemView.findViewById(R.id.topic_title);
            timeView = itemView.findViewById(R.id.topic_time);
        }
    }
}
