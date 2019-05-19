package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.StarSignConstant;
import com.appjishu.starzone.model.StarArchive;
import com.appjishu.starzone.util.StarArchiveUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class SignItemActivity extends BaseTitleBarActivity implements View.OnClickListener{
//    private static final String MY_TAG = "SignItemActivity";

    private int mIndex;
    private StarArchive mStarArchive;
    private ImageView iconImageView;
    private TextView iconTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private GridView fieldGridView;

    private static final String TEXT_VALUE = "textValue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_item);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mIndex = bundle.getInt(StarSignConstant.INDEX_ITEM);
        mStarArchive = StarArchiveUtil.getStarArchiveList().get(mIndex);

        initView();
        initData();
        initEvent();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private void initView() {
        setTitle(getString(R.string.star_character_detail_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        iconImageView = findViewById(R.id.sign_item_image);
        iconImageView.setImageResource(StarSignConstant.arrImages[mIndex]);
        iconTextView = findViewById(R.id.sign_item_text);
    }

    private void initData() {
        if (mStarArchive != null) {
            renderField();
            renderDescription();
        }
    }

    private void renderDescription() {
        iconTextView.setText(mStarArchive.getTitle());
        dateTextView = findViewById(R.id.sign_item_date);
        dateTextView.setText(mStarArchive.getDate());
        descriptionTextView = findViewById(R.id.sign_item_description);
        descriptionTextView.setText(mStarArchive.getDescription());
    }

    private void renderField(){
        // GridView
        fieldGridView = findViewById(R.id.star_field);

        SimpleAdapter saImageItems = new SimpleAdapter(this,
                getGridViewData(),
                R.layout.grid_view_star_field,
                new String[]{TEXT_VALUE},
                new int[]{R.id.star_field_text});
        // 设置GridView的adapter。GridView继承于AbsListView。
        fieldGridView.setAdapter(saImageItems);
    }

    private List<Map<String, Object>> getGridViewData(){
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        String positiveStr = mStarArchive.isPositive() ? "阳性" : "阴性";

        String[] textArray = new String[] {
                "特点：" +  mStarArchive.getCharacteristic(),
                "掌管宫位：" + "第" + mStarArchive.getIndex() + "宫",
                "阴阳性：" + positiveStr,
                "最大特征：" + mStarArchive.getMaxFeature(),
                "主管星：" + mStarArchive.getSupervisorStar(),
                "颜色：" + mStarArchive.getColor(),
                "珠宝：" + mStarArchive.getJewelry(),
                "幸运号码：" + mStarArchive.getLuckyNumber(),
                "金属：" + mStarArchive.getMetal()
        };
        for (int i=0;i<textArray.length; i++){
            Map<String, Object> field = new HashMap<String, Object>();
            field.put(TEXT_VALUE, textArray[i]);
            data.add(field);
        }
        return data;
    }

    private void initEvent() {

    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.ivAboutQRCode:
//                downloadApp();
//                break;
//            default:
//                break;
//        }
    }
}


