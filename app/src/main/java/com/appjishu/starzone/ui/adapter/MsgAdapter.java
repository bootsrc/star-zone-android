package com.appjishu.starzone.ui.adapter;

import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.app.MyApp;
import com.appjishu.starzone.model.ChatMsg;
import com.appjishu.starzone.model.Msg;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.UserInfo;

import org.w3c.dom.Text;

import java.util.List;

import razerdp.github.com.lib.api.AppContext;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<ChatMsg> mMsgList;
    private long userId;
    private String myHeadImg;
    private String targetHeadImg;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        private ImageView myHeadView;
        private ImageView targetHeadView;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            leftMsg = (TextView) view.findViewById(R.id.left_msg);
            rightMsg = (TextView) view.findViewById(R.id.right_msg);
            targetHeadView = view.findViewById(R.id.target_head_image_view);
            myHeadView = view.findViewById(R.id.my_head_image_view);
        }
    }

    public MsgAdapter(List<ChatMsg> msgList, String targetHeadImg) {
        userId = AppSetting.getUserId();
        UserInfo userInfo = SharedPreferencesMyUtil.queryUserInfo(MyApp.getInstance());
        myHeadImg = userInfo.getHeadImg();
        this.targetHeadImg = targetHeadImg;
        mMsgList = msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {               //onCreateViewHolder()用于创建ViewHolder实例
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        return new ViewHolder(view);                                                   //把加载出来的布局传到构造函数中，再返回
    }

    @Override
    public void onBindViewHolder(ViewHolder Holder, int position) {                     //onBindViewHolder()用于对RecyclerView子项的数据进行赋值，会在每个子项被滚动到屏幕内的时候执行
        ChatMsg msg = mMsgList.get(position);
//        if (msg.getType() == Msg.TYPE_RECEIVED) {                                         //增加对消息类的判断，如果这条消息是收到的，显示左边布局，是发出的，显示右边布局
//            Holder.leftLayout.setVisibility(View.VISIBLE);
//            Holder.rightLayout.setVisibility(View.GONE);
//            Holder.leftMsg.setText(msg.getContent());
//
//        } else if (msg.getType() == Msg.TYPE_SENT) {
//            Holder.rightLayout.setVisibility(View.VISIBLE);
//            Holder.leftLayout.setVisibility(View.GONE);
//            Holder.rightMsg.setText(msg.getContent());
//
//        }

        if (msg != null) {
            if (msg.getSenderId() == userId) {
                Holder.rightLayout.setVisibility(View.VISIBLE);
                Holder.leftLayout.setVisibility(View.GONE);
                Holder.rightMsg.setText(msg.getMsgBody());
                if (!TextUtils.isEmpty(myHeadImg)) {
                    ImageLoadMnanger.INSTANCE.loadImage(Holder.myHeadView,
                            NetConstant.RESOURCES_BASE + myHeadImg);
                }
            } else {
                Holder.leftLayout.setVisibility(View.VISIBLE);
                Holder.rightLayout.setVisibility(View.GONE);
                Holder.leftMsg.setText(msg.getMsgBody());
                if (!TextUtils.isEmpty(targetHeadImg)) {
                    ImageLoadMnanger.INSTANCE.loadImage(Holder.targetHeadView,
                            NetConstant.RESOURCES_BASE + targetHeadImg);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
