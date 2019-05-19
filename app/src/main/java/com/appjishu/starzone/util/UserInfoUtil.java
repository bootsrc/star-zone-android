package com.appjishu.starzone.util;

import com.appjishu.starzone.constant.CustomConstant;
import com.appjishu.starzone.model.UserProfile;
import com.razerdp.github.com.common.entity.UserInfo;

public class UserInfoUtil {
    public static UserInfo userProfile2UserInfo(UserProfile userProfile) {
        if (userProfile == null || userProfile.getUserId() < 1) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userProfile.getUserId());
        userInfo.setUsername(userProfile.getNickname());
        userInfo.setNickname(userProfile.getNickname());
        userInfo.setCover(CustomConstant.COVER_DEMO);
        userInfo.setHeadImg(userProfile.getHeadImg());
        return userInfo;
    }
}
