package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.bean.MotionData;
import com.tianbao.mi.bean.UploadDataBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 启动界面
 */
public class SplashActivity extends Activity {

    private Context mContext;
    private ImageView advertisement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;
        initView();

//        uploadData();
    }

    // 初始化视图
    private void initView() {
        advertisement = findViewById(R.id.advertisement);

//        Picasso.with(mContext).load(R.drawable.d3).into(advertisement);

        init();
    }

    // 初始化
    private void init() {
        int type = (int) SPUtils.get(mContext, StringConstant.DATA_TYPE_SP_KEY, 0);// 如果已经登录过则有数据
        if (type == 0) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(mContext, LoginActivity.class));
                finish();
            }, 4000L);// 跳转到登录
        } else {
            IntegerConstant.STORE_ID = (int) SPUtils.get(mContext, StringConstant.STORE_ID_SP_KEY, 0);// 如果已经登录过则有数据

            String name = (String) SPUtils.get(mContext, StringConstant.STORE_NAME_KEY, "-1");
            if (name == null || name.equals("-1")) {
                requestCourse(IntegerConstant.STORE_ID);
            } else {
                StringConstant.STORE_NAME = name;
            }
            requestApp(StringConstant.DEVICE_ID);
        }
    }

    // 首先获取一次课程信息  如果课程马上要开始  则进行数据加载  如果课程还有很长时间才开始则没必要加载太早
    private void requestCourse(int storeId) {
        Map<String, String> param = new HashMap<>();
        param.put("storeId", String.valueOf(storeId));
        param.put("courseId", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<CourseInfoBean> model = service.getCourse(param);

        model.enqueue(new Callback<CourseInfoBean>() {
            @Override
            public void onResponse(Response<CourseInfoBean> response, Retrofit retrofit) {
                CourseInfoBean courseInfo = response.body();
                int code = courseInfo.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    L.v("courseInfo", "courseInfo -> > > " + courseInfo.toString());
                    try {
                        String storeName = courseInfo.getData().getStore().getName();
                        if (TextUtils.isEmpty(storeName)) StringConstant.STORE_NAME = storeName;
                        SPUtils.put(mContext, StringConstant.STORE_NAME_KEY, StringConstant.STORE_NAME);
                    } catch (Exception e) {
                        e.printStackTrace();
                        L.w("数据获取失败或保存数据发生错误");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    // 获取配置信息
    private void requestApp(String deviceId) {
        Map<String, String> param = new HashMap<>();
        param.put("deviceId", deviceId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<LoginBean> model = service.getApp(param);
        model.enqueue(new Callback<LoginBean>() {
            @Override
            public void onResponse(Response<LoginBean> response, Retrofit retrofit) {
                LoginBean bean = response.body();
                if (bean != null) {
                    Intent intent = new Intent(mContext, StandbyActivity.class);
                    ArrayList<String> upList = null;
                    ArrayList<String> downList = null;
                    int code = bean.getCode();
                    if (code == IntegerConstant.RESULT_OK) {
                        LoginBean.DataBean dBean = bean.getData();
                        long df = dBean.getRefreshDataFrequency();
                        if (df > 0) {
                            IntegerConstant.REFRESH_DATA_FREQUENCY = df;
                        }
                        long rf = dBean.getRefreshRelationFrequency();
                        if (rf > 0) {
                            IntegerConstant.REFRESH_RELATION__FREQUENCY = rf;
                        }
                        long sf = dBean.getSortFrequency();
                        if (sf > 0) {
                            IntegerConstant.SORT_FREQUENCY = sf;
                        }

                        String urlString = dBean.getSplashAdUrl();
                        if (!TextUtils.isEmpty(urlString)) {
                            initBanner(urlString);
                        } else {
                            advertisement.setVisibility(View.GONE);
                        }

                        L.i("refresh", "用户数据刷新时间：" + IntegerConstant.REFRESH_DATA_FREQUENCY);
                        L.i("refresh", "用户关系刷新时间：" + IntegerConstant.REFRESH_RELATION__FREQUENCY);
                        L.i("refresh", "界面排序刷新时间：" + IntegerConstant.SORT_FREQUENCY);

                        // 待机页上面轮播图地址
                        String upUrls = dBean.getStandbyUpAdUrl();
                        if (!TextUtils.isEmpty(upUrls)) {
                            if (upUrls.contains("，")) {
                                upUrls = upUrls.replace("，", ",");
                            }
                            String[] adUrlArr = upUrls.split(",");
                            for (String url : adUrlArr) {
                                if (upList == null) upList = new ArrayList<>();
                                upList.add(url);
                            }
                        }

                        if (upList != null && upList.size() > 0) {
                            L.d("upList", "upList size - > " + upList.size());
                            intent.putStringArrayListExtra(StringConstant.BANNER_LIST_UP, upList);
                        }

                        // 待机页下面轮播图地址
                        String downUrl = dBean.getStandbyDownAdUrl();
                        if (!TextUtils.isEmpty(downUrl)) {
                            if (downUrl.contains("，")) {
                                downUrl = downUrl.replace("，", ",");
                            }
                            String[] downUrlArr = downUrl.split(",");
                            for (String url : downUrlArr) {
                                if (downList == null) downList = new ArrayList<>();
                                downList.add(url);
                            }
                        }

                        // 加载界面图片地址
                        String leftUrl = dBean.getLoadLeftAdUrl();
                        String rightUrl = dBean.getLoadRightAdUrl();
                        if (!TextUtils.isEmpty(leftUrl)) {
                            MyApp.setLoadUrl(leftUrl);
                        }
                        if (!TextUtils.isEmpty(rightUrl)) {
                            MyApp.setLoadUrl(rightUrl);
                        }

                        if (downList != null && downList.size() > 0) {
                            L.d("downList", "downList size - > " + downList.size());
                            intent.putStringArrayListExtra(StringConstant.BANNER_LIST_DOWN, downList);
                        }

                        new Handler().postDelayed(() -> {
                            startActivity(intent);
                            finish();
                        }, 8000L);
                    } else {
                        startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                        finish();
                    }
                } else {
                    startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                finish();
            }
        });
    }

    // 设置图片
    private void initBanner(String urls) {
        advertisement.setVisibility(View.VISIBLE);
        Picasso.with(mContext).load(urls).into(advertisement);
    }

    // 上传用户数据
    private void uploadData() {
        UploadData uploadData = new UploadData();
        List<MotionData> mDataList = new ArrayList<>();
        MotionData mData = new MotionData();
        mData.setCourseId(99);
        mData.setAverageHeartRate(100);// 平均心率
        mData.setAverageVelocity(100);// 平均速度
        mData.setCalorie(98.9f);
        mData.setStatus(1);
        mData.setMaximumHeartRate(100);
        mData.setExerciseDuration(20);
        mData.setMileage(100);
        mData.setTopSpeed(90);
        mData.setUserId(47);
        mDataList.add(mData);

        mData = new MotionData();
        mData.setCourseId(99);
        mData.setAverageHeartRate(78);
        mData.setAverageVelocity(34);
        mData.setCalorie(37.9f);
        mData.setStatus(1);
        mData.setMaximumHeartRate(36);
        mData.setExerciseDuration(20);
        mData.setMileage(100);
        mData.setTopSpeed(90);
        mData.setUserId(47);
        mDataList.add(mData);

        uploadData.setMotionDataList(mDataList);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<UploadDataBean> model = service.saveMotionData(uploadData);

        model.enqueue(new Callback<UploadDataBean>() {
            @Override
            public void onResponse(Response<UploadDataBean> response, Retrofit retrofit) {
                UploadDataBean bean = response.body();
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    Toast.makeText(mContext, "数据上传成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "数据上传失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "连接服务器失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
