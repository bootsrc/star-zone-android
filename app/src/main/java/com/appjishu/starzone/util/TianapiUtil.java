package com.appjishu.starzone.util;

import com.appjishu.starzone.constant.StarSignConstant;
import com.appjishu.starzone.constant.TianapiConstant;

public class TianapiUtil {
    public static String getUrl(int meValue, int heValue) {
        String me = StarSignConstant.arrText[meValue];
        String he = StarSignConstant.arrText[heValue];
        String url = TianapiConstant.URL + "?key=" + TianapiConstant.APIKEY + "&me="
                + me + "&he="+ he;
        return url;
    }
}
