package com.tianbao.mi.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.DevicesUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.widget.bdplayer.BDCloudVideoView;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.tianbao.mi.constant.StringConstant.APP_ID;
import static com.tianbao.mi.constant.StringConstant.APP_KEY;

/**
 * App
 * Created by edianzu on 2017/10/29.
 */
public class MyApp extends Application {

    private static MyApp mContext;

    private static CourseInfoBean mCourseInfo;

    private static int courseId = 0;
    private static String courseEndTime = "0";

    /**
     * 维护 Activity 的 list
     */
    private static List<Activity> mActivityList = Collections.synchronizedList(new LinkedList<Activity>());

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // 注册 Activity 管理监听
        registerActivityListener();
        setTimeYear();

        SoundPlayUtils.init(mContext);// 初始化背景音乐播放器

        StringConstant.DEVICE_ID = DevicesUtils.getUniqueID(mContext);
        L.d("DEVICES", "DEVICES == " + StringConstant.DEVICE_ID);
        if ( StringConstant.DEVICE_ID.equals("-1")) {
            L.w("devicesId error");
        }

        if (shouldInit()) MiPushClient.registerPush(mContext, APP_ID, APP_KEY);// 注册小米推送

        BDCloudVideoView.setAK(StringConstant.BD_PLAYER_APP_KEY);// 百度播放器 appKey
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 保存课程信息
     */
    public static void setCourseInfo(CourseInfoBean bean) {
        MyApp.mCourseInfo = bean;
    }

    /**
     * 获取课程信息
     */
    public static CourseInfoBean getCourseInfo() {
        return MyApp.mCourseInfo;
    }

    /**
     * 获取课程结束时间
     * @return 阶乘结束时间
     */
    public static String getCourseEndTime() {
        return MyApp.courseEndTime;
    }

    /**
     * 设置课程结束时间
     * @param endTime 课程结束时间
     */
    public static void setCourseEndTime(String endTime) {
        MyApp.courseEndTime = endTime;
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
