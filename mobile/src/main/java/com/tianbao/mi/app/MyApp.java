package com.tianbao.mi.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.service.BackstageService;
import com.tianbao.mi.utils.DateUtils;
import com.tianbao.mi.utils.DevicesUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.widget.bdplayer.BDCloudVideoView;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.tianbao.mi.constant.ConfigConstant.APP_ID;
import static com.tianbao.mi.constant.ConfigConstant.APP_KEY;
import static com.tianbao.mi.constant.ConfigConstant.BD_PLAYER_APP_KEY;
import static com.tianbao.mi.constant.ConfigConstant.DEVICE_ID;

/**
 * App
 * Created by edianzu on 2017/10/29.
 */
public class MyApp extends Application {

    private static MyApp mContext;

    private static int courseId = 0;

    private static long OPEN_APP_TIME = System.currentTimeMillis();// 打开 app 的时间
    private static List<String> loadImageUrl;// 加载界面图片地址
    private static List<String> upUrl;// 待机界面图片地址
    private static List<String> downUrl;// 待机界面图片二维码

    /**
     * 时间验证
     */
    public static boolean verification() {
        try {
            String date = DateUtils.dateToString(new Date(MyApp.OPEN_APP_TIME), "yyyy-MM-dd");
            if (DateUtils.isToday(date)) {
                L.d("isToday", date + " -> 今天");
                return true;
            } else {
                L.d("isToday", date + "-> 不是今天");
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取图片地址
     * @return 图片地址  没有就返回 null
     */
    public static List<String> getLoadUrl() {
        return loadImageUrl;
    }

    /**
     * 设置图片地址
     */
    public static void setLoadUrl(String url) {
        if (MyApp.loadImageUrl == null) MyApp.loadImageUrl = new ArrayList<>();
        MyApp.loadImageUrl.add(url);
    }

    public static List<String> getUpUrl() {
        return upUrl;
    }

    public static void setUpUrl(List<String> upUrl) {
        MyApp.upUrl = upUrl;
    }

    public static List<String> getDownUrl() {
        return downUrl;
    }

    public static void setDownUrl(List<String> downUrl) {
        MyApp.downUrl = downUrl;
    }

    /**
     * 维护 Activity 的 list
     */
    private static List<Activity> mActivityList = Collections.synchronizedList(new LinkedList<Activity>());

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // 启动服务
        startService(new Intent(mContext, BackstageService.class));

        // 注册 Activity 管理监听
        registerActivityListener();
        setTimeYear();

        SoundPlayUtils.init(mContext);// 初始化背景音乐播放器

        DEVICE_ID = DevicesUtils.getUniqueID(mContext);
        L.d("DEVICES", "DEVICES == " + DEVICE_ID);
        if ( DEVICE_ID.equals("-1")) {
            L.w("devicesId error");
        }

        if (shouldInit()) MiPushClient.registerPush(mContext, APP_ID, APP_KEY);// 注册小米推送

        BDCloudVideoView.setAK(BD_PLAYER_APP_KEY);// 百度播放器 appKey
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 添加选择的课程 ID
     * @param courseId 课程 ID
     */
    public static void setCourseId(int courseId) {
        MyApp.courseId = courseId;
    }

    /**
     * 获取选择的课程 ID
     */
    public static int getCourseId() {
        return courseId;
    }

    // 初始化小米推送
    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    // 获取当前年份
    private void setTimeYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        StringConstant.TIME_YEAR = String.valueOf(year);
    }

    /**
     * @param activity 作用说明 ：添加一个 activity 到管理里
     */
    public void pushActivity(Activity activity) {
        mActivityList.add(activity);
        L.d("activityList:size:" + mActivityList.size());
    }

    /**
     * @param activity 作用说明 ：删除一个 activity 在管理里
     */
    public void popActivity(Activity activity) {
        mActivityList.remove(activity);
        L.d("activityList:size:" + mActivityList.size());
    }

    /**
     * 结束所有 Activity
     */
    public static void finishAllActivity() {
        if (mActivityList == null) {
            return;
        }
        for (Activity activity : mActivityList) {
            activity.finish();
        }
        mActivityList.clear();
    }

    /**
     * 退出应用程序
     */
    public static void appExit() {
        try {
            L.e("app exit");
            mContext.stopService(new Intent(mContext, BackstageService.class));// 关闭服务
            finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            L.w("exit exception");
        }
    }

    // 注册 Activity 管理监听  android 4.0 以上
    private void registerActivityListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    // 监听到 Activity 创建事件 将该 Activity 加入 list
                    pushActivity(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (null == mActivityList || mActivityList.isEmpty()) {
                        return;
                    }
                    if (mActivityList.contains(activity)) {
                        /**
                         *  监听到 Activity 销毁事件 将该 Activity 从 list 中移除
                         */
                        popActivity(activity);
                    }
                }
            });
        }
    }
}
