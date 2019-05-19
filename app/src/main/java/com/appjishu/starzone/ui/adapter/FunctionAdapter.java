package com.appjishu.starzone.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.FunctionData;
import com.appjishu.starzone.ui.tool.OnRecyclerItemClickListener;

import java.util.List;

import razerdp.github.com.ui.base.adapter.OnRecyclerViewItemClickListener;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder> {

    private List<FunctionData> dataList;
    private OnRecyclerItemClickListener<FunctionData> listener;

    public FunctionAdapter(List<FunctionData> dataList, OnRecyclerItemClickListener<FunctionData> listener) {
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FunctionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_function, parent, false);
        FunctionAdapter.ViewHolder viewHolder = new FunctionAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FunctionAdapter.ViewHolder holder, final int position) {
        final FunctionData functionData = dataList.get(position);
        int resId = functionData.getResId();
        if (resId > 0) {
            holder.imageView.setImageResource(resId);
        }
        holder.textView.setText(functionData.getText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerItemClick(holder.itemView, position, functionData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = itemView.findViewById(R.id.function_image);
            textView = itemView.findViewById(R.id.function_text);
        }
    }
}
