package com.appjishu.starzone.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import razerdp.github.com.lib.utils.StringUtil;

public class CommonUtil {
    private static final String TAG = "CommonUtil";

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 分享信息
     *
     * @param context
     * @param toShare
     */
    public static void shareInfo(Activity context, String toShare) {
        if (context == null || TextUtils.isEmpty(toShare)) {
            Log.e(TAG, "shareInfo  context == null || StringUtil.isNotEmpty(toShare, true) == false >> return;");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "选择分享方式");
        intent.putExtra(Intent.EXTRA_TEXT, toShare.trim());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 复制文字
     *
     * @param context
     * @param value
     */
    public static void copyText(Context context, String value) {
        if (context == null || TextUtils.isEmpty(value)) {
            Log.e(TAG, "copyText  context == null || TextUtils.isEmpty(value) >> return;");
            return;
        }

        ClipData cD = ClipData.newPlainText("simple text", value);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(cD);
        }
        Toast.makeText(context, "已复制\n" + value, Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送邮件
     *
     * @param context
     * @param emailAddress
     */
    public static void sendEmail(Activity context, String emailAddress) {
        if (context == null || TextUtils.isEmpty(emailAddress)) {
            Log.e(TAG, "sendEmail  context == null || TextUtils.isEmpty(emailAddress) >> return;");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + emailAddress));//缺少"mailto:"前缀导致找不到应用崩溃
        intent.putExtra(Intent.EXTRA_TEXT, "内容");  //最近在MIUI7上无内容导致无法跳到编辑邮箱界面
        context.startActivity(intent);
    }
}
