package com.appjishu.starzone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.StarSignConstant;

import java.util.HashMap;

import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.widget.common.TitleBar;

public class StarCharacterActivity extends BaseTitleBarActivity
        implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final String MY_TAG = "StarCharacterActivity";

    private GridView mGridView;
    private static final int MSG_WHAT_LOAD_GRID_DATA = 0x01;
    private SimpleAdapter saImageItems;
    private MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_character);

        initView();
        initData();
        initEvent();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    private void initView() {
        setTitle(getString(R.string.star_character_title));
        setTitleMode(TitleBar.MODE_LEFT);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);

        mGridView = findViewById(R.id.gridview_star);
    }

    private void initData() {
        myHandler = new MyHandler();
        loadGridData();
    }

    private void loadGridData() {
        saImageItems = new SimpleAdapter(this,
                StarSignConstant.getGridViewData(),
                R.layout.grid_view_item,
                new String[]{StarSignConstant.IMAGE_ITEM, StarSignConstant.TEXT_ITEM
                        , StarSignConstant.DATE_ITEM},
                new int[]{R.id.itemImage, R.id.itemText, R.id.itemDate});


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendLoadMsg();
            }
        }).start();
    }

    private void sendLoadMsg() {
        myHandler.sendEmptyMessage(MSG_WHAT_LOAD_GRID_DATA);
    }

    private void initEvent() {

    }

    @Override
    public void onClick(View v) {

    }

    /**
     * GridView的点击回调函数
     *
     * @param adapter  -- GridView对应的dapterView
     * @param view     -- AdapterView中被点击的视图(它是由adapter提供的一个视图)。
     * @param position -- 视图在adapter中的位置。
     * @param rowid    -- 被点击元素的行id。
     */
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long rowid) {
        // 根据元素位置获取对应的值
        HashMap<String, Object> item = (HashMap<String, Object>) adapter.getItemAtPosition(position);

        String itemText = (String) item.get(StarSignConstant.TEXT_ITEM);
        int index = (int) item.get(StarSignConstant.INDEX_ITEM);
        Intent intent = new Intent(this, SignItemActivity.class);
        intent.putExtra(StarSignConstant.INDEX_ITEM, index);
        startActivity(intent);
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_LOAD_GRID_DATA:
                    // 设置GridView的adapter。GridView继承于AbsListView。
                    if (saImageItems != null) {
                        mGridView.setAdapter(saImageItems);
                        mGridView.setOnItemClickListener(StarCharacterActivity.this);
                    }
                    break;
            }
        }
    }
}
