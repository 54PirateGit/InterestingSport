package com.tianbao.mi.constant;

/**
 * Integer
 * Created by edianzu on 2017/10/26.
 */

public class IntegerConstant {

    // 网络请求成功
    public final static int RESULT_OK = 200;

    // 启动页停留时间
    public final static long INTO_MAIN_TIME = 4000L;

    // storeId
    public static int STORE_ID = -1;

    // 房间容量
    public static int ROOM_COUNT = 28;

    // 排序刷新界面频率
    public static long SORT_FREQUENCY = 2 * 60 * 1000L;// 默认

    // 刷新用户关系频率
    public static long REFRESH_RELATION__FREQUENCY = 5 * 60 * 1000L;// 默认

    // 刷新数据频率
    public static long REFRESH_DATA_FREQUENCY = 5 * 1000L;// 默认

    // view 类型 排序
    public final static int VIEW_TYPE_SORT = 1;

    // view 类型 提示
    public final static int VIEW_TYPE_TIP = 2;

    // 直播课程选中
    public final static int LIVE_COURSE_SELECT_TYPE = 1;

    // 直播课程为选中
    public final static int LIVE_COURSE_NO_SELECT_TYPE = 2;

}
