package com.tianbao.mi.net;

/**
 * 接口地址
 * Created by edianzu on 2017/10/26.
 */
public class Api {

    // 树莓派地址
//    public final static String BASE_URL_PI = "http://192.168.31.58:8088";

    // 树莓派地址
    public final static String BASE_URL_PI = "http://192.168.2.58:8088";

    // 云服务器地址
    public final static String BASE_URL = "http://yin.nat200.top";

    // 树莓派获取数据
    public final static String RECORD = "/business/record";

    // 获取用户与车的绑定关系信息
    public final static String GET_BINDING = "/api/pi/getBinding";

    // 获取全部用户与车的绑定关系信息
    public final static String GET_BINDINGS = "/api/pi/getBindings";

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

    // 上传用户运动数据
    public final static String SAVE_GYM_DATA = "/api/pi/saveGymData";

    // 解除绑定 用户与单车的关系
    public final static String USER_UNBINDING = "/api/pi/unbinding";

    // 获取配置信息
    public final static String GET_APP = "/api/pi/getApp";

    // 获取点播列表
    public final static String GET_ON_DEMAND_LIST = "/api/pi/getOnDemandList";

    // 添加心率
    public final static String ADD_HEART = "/api/user/addHeart";
}
