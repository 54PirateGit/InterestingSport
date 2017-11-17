package com.tianbao.mi.constant;

/**
 * Integer
 * Created by edianzu on 2017/10/26.
 */

public class IntegerConstant {

    // 网络请求成功
    public final static int RESULT_OK = 200;

    // storeId
    public static int STORE_ID = -1;

    // 排序刷新界面频率
    public static long SORT_FREQUENCY = 2 * 60 * 1000L;// 默认

    // 刷新用户关系频率
    public static long REFRESH_RELATION__FREQUENCY = 5 * 60 * 1000L;// 默认

    // 刷新数据频率
    public static long REFRESH_DATA_FREQUENCY = 5 * 1000L;// 默认

    // 获取直播课程直播状态
    public static long GET_LIVE_COURSE_STATUS = 5 * 60 * 1000L;

    // view 类型 排序
    public final static int VIEW_TYPE_SORT = 1;

    // view 类型 提示
    public final static int VIEW_TYPE_TIP = 2;

    // 正常心率范围内的最小心率值
//    public final static int MIN_HEAR_RATE = 60;

    // 放松热身
    public final static int RELAX_HEAR_RATE = 100;

    // 燃烧脂肪
    public final static int BURNING_HEAR_RATE = 144;

    // 糖原消耗
    public final static int CONSUME_HEAR_RATE = 170;

    // 乳酸堆积
    public final static int ACCUMULATION_HEAR_RATE = 195;

    // 高强度运动时最大心率值
//    public final static int MAX_HEAR_RATE = 195;

}
