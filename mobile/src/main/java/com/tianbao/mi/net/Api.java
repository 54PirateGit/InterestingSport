package com.tianbao.mi.net;

/**
 * 接口地址
 * Created by edianzu on 2017/10/26.
 */
public class Api {

    // 树莓派地址
    public final static String BASE_URL_PI = "http://192.168.31.58:8088";

    // 云服务器地址
    public final static String BASE_URL = "http://yin.nat200.top";

    // 树莓派获取数据
    public final static String RECORD = "/business/record";

    // 获取用户与车的绑定关系信息
    public final static String GET_BUILD = "/api/pi/getBinding";

    // 第一次打开应用需要登录
    public final static String LOGIN = "/api/pi/login";

    // 获取课程、门店、教练信息
    public final static String GET_COURSE = "/api/pi/getCourse";

    // 获取直播列表
    public final static String GET_LIVE_LIST = "/api/pi/getLiveList";

    // 选择直播课程
    public final static String SELECT_COURSE_LIVE = "/api/pi/selectCourseLive";

    // 更换直播课程
    public final static String CHANGE_COURSE_LIVE = "/api/pi/changeCourseLive";
}