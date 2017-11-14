package com.tianbao.mi.net;

import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.LiveCourseBean;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.bean.MotionData;
import com.tianbao.mi.bean.RecordBean;
import com.tianbao.mi.bean.CurrencyBean;
import com.tianbao.mi.bean.UploadDataBean;

import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.QueryMap;

/**
 * api
 * Created by edianzu on 2017/10/26.
 */
public interface ApiService {

    // 获取用户数据
    @POST(Api.RECORD)
    Call<RecordBean> requestRecord();

    // 获取用户绑定关系
    @POST(Api.GET_BUILD)
    Call<BuildBean> getBuild(@QueryMap Map<String, List<String>> param);

    // 登录
    @POST(Api.LOGIN)
    Call<LoginBean> login(@QueryMap Map<String, String> param);

    // 获取课程信息
    @POST(Api.GET_COURSE)
    Call<CourseInfoBean> getCourse(@QueryMap Map<String, String> param);

    // 获取课程列表
    @POST(Api.GET_LIVE_LIST)
    Call<LiveCourseBean> getLiveList(@QueryMap Map<String, String> param);

    // 选择直播课程
    @POST(Api.SELECT_COURSE_LIVE)
    Call<CurrencyBean> selectCourseLive(@QueryMap Map<String, String> param);

    // 更换直播课程
    @POST(Api.CHANGE_COURSE_LIVE)
    Call<CurrencyBean> changeCourseLive(@QueryMap Map<String, String> param);

    // 上传用户运动数据
    @POST(Api.SAVE_MOTION_DATA)
    Call<UploadDataBean> saveMotionData(@QueryMap Map<String, List<MotionData>> param);

    // 解除绑定 用户与单车的关系
    @POST(Api.USER_UNBINDING)
    Call<CurrencyBean> unbinding(@QueryMap Map<String,String> param);
}
