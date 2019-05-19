package com.appjishu.starzone.ui.tool;

import android.view.View;

public interface OnRecyclerItemClickListener<T> {
    void  onRecyclerItemClick(View v, int position, T data);
}
