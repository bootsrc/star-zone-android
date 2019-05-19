package com.appjishu.starzone.util;

import android.text.TextUtils;

import com.appjishu.starzone.constant.CustomConstant;
import com.razerdp.github.com.common.constant.NetConstant;
import com.appjishu.starzone.model.MomentMobileVO;
import com.razerdp.github.com.common.entity.MomentContent;
import com.razerdp.github.com.common.entity.MomentInfo;
import com.razerdp.github.com.common.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class ObjectConverter {
    public static MomentInfo momentMVO2Info(MomentMobileVO vo) {
        MomentInfo info = new MomentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.setHeadImg(NetConstant.RESOURCES_BASE + vo.getUserHead());
        userInfo.setCover(CustomConstant.COVER_DEMO);
        userInfo.setNickname(vo.getNickname());
        userInfo.setUsername(vo.getNickname());
        userInfo.setId(vo.getUserId());
        info.setAuthor(userInfo);

        //
        MomentContent content = new MomentContent();
        content.setText(vo.getContent());
        List<String> pics = new ArrayList<String>();
        pics.add(NetConstant.RESOURCES_BASE + vo.getImg());
        content.setPics(pics);
        info.setContent(content);
        info.setId(vo.getId());

//        LikeInfo likesInfo = new LikeInfo();
//        likesInfo.setMomentId(vo.getUserId() +"");
//        likesInfo.setUserInfo(vo.getUserId() +"");
//        likesInfo.setObjectId("1111111");
//
//        List<LikeInfo> likesList = new ArrayList<LikeInfo>();
//        likesList.add(likesInfo);
//        info.setLikeList(likesList);
        return info;
    }

    public static void processMoment(MomentInfo info){
        if (info!= null && info.getAuthor()!= null
                && !TextUtils.isEmpty(info.getAuthor().getHeadImg())) {
            String fullHeadUrl = NetConstant.RESOURCES_BASE + info.getAuthor().getHeadImg();
            info.getAuthor().setHeadImg(fullHeadUrl);
        }

        if (info.getContent() != null) {
            List<String> pics = info.getContent().getPics();
            if (pics != null && pics.size() >0) {
                for (int i=0; i< pics.size(); i++) {
                    pics.set(i, NetConstant.RESOURCES_BASE + pics.get(i));
                }
            }
        }
    }
}
