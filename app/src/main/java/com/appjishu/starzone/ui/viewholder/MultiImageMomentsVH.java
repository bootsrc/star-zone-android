package com.appjishu.starzone.ui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.razerdp.github.com.common.MomentsType;
import com.razerdp.github.com.common.entity.MomentInfo;
import com.razerdp.github.com.common.entity.PhotoBrowseInfo;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import com.appjishu.starzone.R;
import com.appjishu.starzone.activity.ActivityLauncher;
import razerdp.github.com.ui.base.adapter.LayoutId;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.widget.imageview.ForceClickImageView;
import razerdp.github.com.widget.PhotoContents;
import razerdp.github.com.widget.adapter.PhotoContentsBaseAdapter;

/**
 * Created by liushaoming on 2016/11/3.
 * <p>
 * 九宮格圖片的vh
 *
 * @see MomentsType
 */

@LayoutId(id = R.layout.moments_multi_image)
public class MultiImageMomentsVH extends CircleBaseViewHolder implements PhotoContents.OnItemClickListener {


    private PhotoContents imageContainer;
    private InnerContainerAdapter adapter;

    public MultiImageMomentsVH(View itemView, int viewType) {
        super(itemView, viewType);
    }


    @Override
    public void onFindView(@NonNull View rootView) {
        imageContainer = (PhotoContents) findView(imageContainer, R.id.circle_image_container);
        if (imageContainer.getmOnItemClickListener() == null) {
            imageContainer.setmOnItemClickListener(this);
        }
    }

    @Override
    public void onBindDataToView(@NonNull MomentInfo data, int position, int viewType) {
        if (adapter == null) {
            adapter = new InnerContainerAdapter(getContext(), data.getContent().getPics());
            imageContainer.setAdapter(adapter);
        } else {
            KLog.i("update image" + data.getAuthor().getNickname() + "     :  " + data.getContent().getPics().size());
            adapter.updateData(data.getContent().getPics());
        }
    }

    @Override
    public void onItemClick(ImageView imageView, int i) {
        PhotoBrowseInfo info = PhotoBrowseInfo.create(adapter.datas, imageContainer.getContentViewsDrawableRects(), i);
        ActivityLauncher.startToPhotoBrosweActivity((Activity) getContext(), info);
    }


    private static class InnerContainerAdapter extends PhotoContentsBaseAdapter {


        private Context context;
        private List<String> datas;

        InnerContainerAdapter(Context context, List<String> datas) {
            this.context = context;
            this.datas = new ArrayList<>();
            this.datas.addAll(datas);
        }

        @Override
        public ImageView onCreateView(ImageView convertView, ViewGroup parent, int position) {
            if (convertView == null) {
                convertView = new ForceClickImageView(context);
                convertView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            return convertView;
        }

        @Override
        public void onBindData(int position, @NonNull ImageView convertView) {
            int width = convertView.getWidth();
            int height = convertView.getHeight();
            String imageUrl = datas.get(position);
            // 去掉缩略图的url的获取
            // 例如 http://resources.appjishu.com/star-sign/463490676928020480.jpg!/fxfn/540x303
//            if (width > 0 && height > 0) {
//                imageUrl = BmobUrlUtil.getThumbImageUrl(imageUrl, width, height);
//            } else {
//                imageUrl = BmobUrlUtil.getThumbImageUrl(imageUrl, 25);
//            }
            KLog.i("处理的url  >>>  " + imageUrl);
            ImageLoadMnanger.INSTANCE.loadImage(convertView, imageUrl);
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        public void updateData(List<String> datas) {
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataChanged();
        }

    }
}
