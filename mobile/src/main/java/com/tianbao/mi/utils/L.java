package com.tianbao.mi.utils;

import android.util.Log;

/**
 * 打印日志
 * Created by edianzu on 2017/9/12.
 */

public class L {

    private static boolean isDebug = true;// 是否调试测试

    private final static String TAG = "TAG";

    // 私有化对象
    private L() {

    }

    // 默认 TAG
    public static void i(Object o) {
        if (isDebug) {
            Log.i(TAG, o + "");
        }
    }

    public static void v(Object o) {
        if (isDebug) {
            Log.v(TAG, o + "");
        }
    }

    public static void w(Object o) {
        if (isDebug) {
            Log.w(TAG, o + "");
        }
    }

    public static void e(Object o) {
        if (isDebug) {
            Log.e(TAG, o + "");
        }
    }

    public static void d(Object o) {
        if (isDebug) {
            Log.d(TAG, o + "");
        }
    }

    // 自定义 TAG
    public static void i(String tag, Object o) {
        if (isDebug) {
            Log.i(tag, o + "");
        }
    }

    public static void v(String tag, Object o) {
        if (isDebug) {
            Log.v(tag, o + "");
        }
    }

    public static void w(String tag, Object o) {
        if (isDebug) {
            Log.w(tag, o + "");
        }
    }

    public static void e(String tag, Object o) {
        if (isDebug) {
            Log.e(tag, o + "");
        }
    }

    public static void d(String tag, Object o) {
        if (isDebug) {
            Log.d(tag, o + "");
        }
    }

    /**
     * request run exception
     */
    public static void ep() {
        if (isDebug) {
            L.w(TAG, "request run exception");
        }
    }
}