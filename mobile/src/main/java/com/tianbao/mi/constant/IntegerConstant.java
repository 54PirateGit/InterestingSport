package com.tianbao.mi.constant;

/**
 * Integer
 * Created by edianzu on 2017/10/26.
 */

public class IntegerConstant {

    // 网络请求成功
    public final static int RESULT_OK = 200;

    // 启动页停留时间
    public final static long SPLASH_INTO_TIME = 5 * 1000L;

    // 没有获取到数据时重复发送请求时的间隔时间
    public final static long RESTART_REQUEST_TIME = 2 * 1000L;

    // 当出现网络不好或者请求数据失败时重新获取数据的次数
    public static long RESTART_REQUEST_COUNT = 5;

    // Dynamic bike 系统类型
    public final static int DYNAMIC_SYSTEM_TYPE = 1;

    // Treadmill 系统类型
    public final static int TREADMILL_SYSTEM_TYPE = 2;

    // calisthenics 系统类型
    public final static int CALISTHENICS_SYSTEM_TYPE = 3;

    // storeId
    public static int STORE_ID = -1;

    // 动感单车周长
    public static float GIRTH = 0;

    // 踏频比例
    public static float RATIO = 0;

    // 排序刷新界面频率
    public static long SORT_FREQUENCY = 2 * 60 * 1000L;// 默认

    // 刷新用户关系频率
    public static long REFRESH_RELATION__FREQUENCY = 5 * 60 * 1000L;// 默认

    // 刷新数据频率
    public static long REFRESH_DATA_FREQUENCY = 5 * 1000L;// 默认

    // 获取直播课程直播状态
    public static long GET_LIVE_COURSE_STATUS = 5 * 60 * 1000L;

    // 获取直播列表时间
    public static long GET_LIVE_COURSE_LIST = 5 * 60 * 1000L;

    // 直播课程开始延迟进入直播界面时间
    public static long INTO_LIVE_COURSE_TIME = 15 * 1000L;

    // 待机页 隔一定时间去获取课程信息
    public final static long GET_COURSE_INFO_LOOP_TIME = 3 * 60 * 1000L;

    // 待机页 隔一定时间去获取课程信息
    public final static long APP_EXIT_TIME = 2 * 1000L;

    // 课程结束之后停留短暂时间跳转到课程结束界面
    public final static long INTO_COURSE_END = 5 * 1000L;

    // 用户数据空挡时间 超过这个时间则执行掉线逻辑
    public final static long DATA_NEUTRAL_GEAR_TIME = 5 * 60 * 1000L;

    // 用户数据前后面交换时间 需要跟前后翻动时间有个时间差所以多 2 秒
    public final static long FRONT_BACK_DATA_CHANGE_FIRST = 7 * 1000L;

    // 用户数据空挡时间 超过这个时间则执行掉线逻辑
    public final static long FRONT_BACK_DATA_CHANGE = 5 * 1000L;

    // 延时滚动时间
    public final static long DELAY_TIME = 5 * 1000L;

    // 延时滚动时间
    public final static long AUTO_ANIM_TIME = 1000L;

    // 自动翻动时间
    public final static long AUTO_SCROLL_TIME = 5 * 1000L;

    // view 类型 排序
    public final static int VIEW_TYPE_SORT = 1;

    // view 类型 提示
    public final static int VIEW_TYPE_TIP = 2;

    // 正常心率范围内的最小心率值
//    public final static int MIN_HEAR_RATE = 60;

    // 放松热身
    public final static int RELAX_HEAR_RATE = 1;

    // 燃烧脂肪
    public final static int BURNING_HEAR_RATE = 2;

    // 糖原消耗
    public final static int CONSUME_HEAR_RATE = 3;

    // 乳酸堆积
    public final static int ACCUMULATION_HEAR_RATE = 4;

    // 身体极限
    public final static int MAX_HEAR_RATE = 5;

    // 有新的瘾伙伴加入
    public final static int SOUND_PARTNER_JOIN = 1;

    // 开始加载
    public final static int SOUND_START_LOAD = 2;

    // 卡路里增加了
    public final static int SOUND_CALORIE_ADD = 3;

    // 课程开始
    public final static int SOUND_START_COURSE = 4;

    // 课程结束
    public final static int SOUND_COURSE_END = 5;

    // 第一名发生变化
    public final static int SOUND_YES = 6;

    // 心率过快
    public final static int SOUND_WARM = 7;

    // main 右边数量
    public static int MAIN_LEFT_COUNT = 7;

    // main 右边数量
    public static int MAIN_RIGHT_COUNT = 5;

    // main 放二维码时的数量
    public static int MAIN_QR_CODE_COUNT = 24;

    // 待机页瘾伙伴一组展示人数
    public static int STANDBY_YIN_NUMBER = 7;
}
