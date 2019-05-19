package com.appjishu.starzone.util;

import android.app.Application;
import android.util.Log;

import com.appjishu.starzone.R;
import com.appjishu.starzone.model.StarArchive;
import com.appjishu.starzone.app.MyApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class StarArchiveUtil {
    private static String getStarArchiveStr() {
        // Test CustomApplication
        Application app = MyApp.getInstance();
        InputStream inputStream = app.getResources().openRawResource(R.raw.star_archive);
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                // 添加代码
                Log.i("RawTest", line);
                stringBuffer.append(line);
            }
            Log.i("RawTest", "RawTest--Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    public static List<StarArchive> getStarArchiveList() {
        String str = getStarArchiveStr();
        Gson gson = new Gson();
        List<StarArchive> starArchiveList = gson.fromJson(str, new TypeToken<List<StarArchive>>() {
        }.getType());
        Log.i("ArchiveList", starArchiveList.toString());
        return starArchiveList;
    }
}
