package razerdp.github.com.ui.base.adapter;

import android.view.View;

/**
 * Created by liushaoming on 2016/11/1.
 *
 */

public interface OnRecyclerViewLongItemClickListener<T> {
    boolean onItemLongClick(View v, int position, T data);
}
