package com.appjishu.starzone.constant;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.StarArchive;
import com.appjishu.starzone.util.StarArchiveUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StarSignConstant {
    private static List<HashMap<String, Object>> gridViewData;

    private static List<StarArchive> starArchiveList;

    public static final String IMAGE_ITEM = "imgage_item";
    public static final String TEXT_ITEM = "text_item";
    public static final String DATE_ITEM = "date_item";
    public static final String INDEX_ITEM = "index_item";
    public static final String[] arrText = new String[]{
            "白羊", "金牛", "双子",
            "巨蟹", "狮子", "处女",
            "天秤", "天蝎", "射手",
            "摩羯", "水瓶", "双鱼"
    };
    public static final String[] arrDate = new String[]{
            "3.21-4.19", "4.20-5.20", "5-21-6.21",
            "6.22-7.22", "7.23-8.22", "8.23-9.22",
            "9.23-10.23", "10.24-11.22", "11.23-12.21",
            "12.22-1.19", "1.20-2.18", "2.19-3.20"
    };

    public static int[] arrImages = new int[]{
            R.drawable.star_1_baiyang, R.drawable.star_2_jinniu, R.drawable.star_3_shuangzi,
            R.drawable.star_4_juxie, R.drawable.star_5_shizi, R.drawable.star_6_chunv,
            R.drawable.star_7_tiancheng, R.drawable.star_8_tianxie, R.drawable.star_9_sheshou,
            R.drawable.star_10_mojie, R.drawable.star_11_shuiping, R.drawable.star_12_shuangyu
    };

    static {
        gridViewData = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < StarSignConstant.arrText.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(StarSignConstant.IMAGE_ITEM, StarSignConstant.arrImages[i]);
            map.put(StarSignConstant.TEXT_ITEM, StarSignConstant.arrText[i]);
            map.put(StarSignConstant.DATE_ITEM, StarSignConstant.arrDate[i]);
            map.put(StarSignConstant.INDEX_ITEM, i);
            gridViewData.add(map);
        }

        //

    }
    /**
     * 获取GridView的数据
     */
    public static List<HashMap<String, Object>> getGridViewData() {
        return gridViewData;
    }

    public static List<StarArchive> getStarArchiveList() {
        if (starArchiveList == null || starArchiveList.size() == 0) {
            starArchiveList = StarArchiveUtil.getStarArchiveList();
        }
        return starArchiveList;
    }
}
