package com.tianbao.mi.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * 吐司
 * Created by edianzu on 2017/9/12.
 */
public class T {

    private static boolean isDebug = true;// 是否调试测试

    // 私有
    private T() {

    }

    /**
     * 显示 short
     */
    public static void showShort(Context context, String content) {
        if (isDebug) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示 long
     */
    public static void showLong(Context context, String content) {
        if (isDebug) {
            Toast.makeText(context, content, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 一直显示 short
     */
    public static void alwaysShort(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 一直显示 long
     */
    public static void alwaysLong(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    /**
     * 没有网络提示
     */
    public static void noNetTip(final Context context) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "网络连接失败，请检查网络设置", Toast.LENGTH_LONG).show());
    }

    /**
     * 连接服务器失败
     */
    public static void connectFailTip(final Context context) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "连接服务器失败", Toast.LENGTH_LONG).show());
    }
}
