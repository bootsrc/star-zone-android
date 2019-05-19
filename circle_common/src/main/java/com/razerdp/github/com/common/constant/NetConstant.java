package com.razerdp.github.com.common.constant;

public class NetConstant {

    //////// 最终环境appjishu.com   ---start---------------
    public static final String URL_BASE_HTTPS = "https://starzone-back.appjishu.com";
    public static final String RESOURCES_BASE = "http://resources.appjishu.com";
    public static final String APP_DOWNLOAD_WEBSITE = "http://resources.appjishu.com/app/star-sign.apk";
    public static final String DONATE_ALIPAY_IMAGE_URL = "http://resources.appjishu.com/app/photo/pay/alipay_receive.png";
    //////// 最终环境appjishu.com   ---end---------------

    public static final String LOGIN = "/passport/login";
    public static final String FETCH_CAPTCHA = "/captcha/fetchCaptcha";
    public static final String REGISTER = "/passport/register";
    public static final String CHANGE_PASSWORD = "/passport/changePassword";
    public static final String SAVE_PROFILE = "/mobile/user/saveProfile";
    public static final String GET_PROFILE = "/mobile/user/getProfile";
    public static final String GET_PROFILE_DETAIL = "/mobile/user/getProfileDetail";
    public static final String TOPIC_BY_PAGE = "/mobile/topic/byPageVisible";
    public static final String TOPIC_FIND_BY_ID = "/mobile/topic/findById";
    public static final String RESOURCE_UPLOAD = "/resourceapi/upload";
    public static final String ADD_MOMENT = "/mobile/moment/add";
    public static final String MOMENT_BY_PAGE = "/mobile/moment/byPageForMobile";
    public static final String DELETE_MOMENT = "/mobile/moment/delete";
    public static final String INTRODUCE_FROM_SERVER = "/promote/introduce";
    public static final String LIKE_MOMENT = "/mobile/moment/like";
    public static final String MOMENT_ADD_COMMENT = "/mobile/moment/addComment";
    public static final String MOMENT_DELETE_ONE_COMMENT = "/mobile/moment/deleteOneComment";
    public static final String PUSH_BIND_REGID = "/mobile/push/bindRegid";
    public static final String FRIEND_BY_PAGE = "/mobile/friend/byPage";
    public static final String CHAT_MSG_LIST = "/mobile/chat/msgList";
    public static final String CHAT_SEND = "/mobile/chat/send";
    public static final String CHAT_GET_RECENT_USER = "/mobile/chat/getRecentChatUser";
    public static final String OTHER_PROFILE = "/mobile/user/otherProfile";
    public static final String OTHER_PROFILE_DETAIL = "/mobile/user/otherProfileDetail";
    public static final String CHECK_IN = "/mobile/task/checkIn";
    public static final String IS_CHECKED = "/mobile/task/isChecked";
    public static final String GET_SCORE = "/mobile/misc/getScore";
    public static final String LATEST_CAMPAIGN = "/mobile/misc/latestCampaign";

    public static final String APP_OFFICIAL_EMAIL = "liushaomingdev@163.com";
    public static final String WECHAT_PUBLIC_NAME = "星座空间";
    public static final String APP_DEVELOPER_WEBSITE = "https://github.com/flylib";
    public static final String APP_OFFICIAL_WEBSITE = "http://appjishu.com";
    public static final String APP_INTRODUCE = "我发现了一款有趣的星座社区App '星座空间'    " +
            "星座解析，运势点评" +
            " 点击链接查看\n" + APP_OFFICIAL_WEBSITE;

    // http查询参数
    // star-sign 分页查询参数
    public static final String PAGE = "page";
    public static final String LIMIT = "limit";

    public static final String HTTP_HEADER_KEY_USER_ID = "userId";
    public static final String HTTP_HEADER_KEY_TOKEN = "token";
    public static final String HTTP_REQUEST_FAILED = "网络不好，请稍后重试";
}
