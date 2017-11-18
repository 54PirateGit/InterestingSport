package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.bean.MotionData;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.bean.UploadDataBean;
import com.tianbao.mi.bean.UserHeart;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.tianbao.mi.constant.IntegerConstant.RESTART_REQUEST_TIME;

/**
 * 启动界面
 */
public class SplashActivity extends Activity {

    private Handler mHandler = new Handler();
    private Context mContext;
    private ImageView advertisement;
    private ImageView imageBackground;

    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;
        initView();

//        uploadData();
//        uploadUser();
    }

    // 初始化视图
    private void initView() {
        imageBackground = findViewById(R.id.image_background);
        Bitmap bitmap = BitmapUtils.readBitMap(mContext, R.drawable.splash);
        imageBackground.setImageBitmap(bitmap);

        advertisement = findViewById(R.id.advertisement);
        init();
    }

    // 初始化
    private void init() {
        int type = (int) SPUtils.get(mContext, StringConstant.DATA_TYPE_SP_KEY, 0);// 如果已经登录过则有数据
//        type = IntegerConstant.CALISTHENICS_SYSTEM_TYPE;
        if (type == 0) {// == 0 说明是安装之后第一次打开 app
            mHandler.postDelayed(() -> {
                startActivity(new Intent(mContext, LoginActivity.class));
                finish();
            }, IntegerConstant.SPLASH_INTO_TIME);// 跳转到登录
        } else if (type == IntegerConstant.DYNAMIC_SYSTEM_TYPE) {// 动感单车
            IntegerConstant.STORE_ID = (int) SPUtils.get(mContext, StringConstant.STORE_ID_SP_KEY, 0);// 如果已经登录过则有数据

            String name = (String) SPUtils.get(mContext, StringConstant.STORE_NAME_KEY, "-1");
            if (name == null || name.equals("-1")) {
                requestCourse(IntegerConstant.STORE_ID);
            } else {
                StringConstant.STORE_NAME = name;
            }
            requestApp(StringConstant.DEVICE_ID);
        } else if (type == IntegerConstant.CALISTHENICS_SYSTEM_TYPE) {// 团操
            mHandler.postDelayed(() -> {
                startActivity(new Intent(mContext, CalisthenicsActivity.class));
                finish();
            }, IntegerConstant.SPLASH_INTO_TIME);
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
                if (isCancelRequest) return;
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

    // 没有获取到数据时重复发送请求获取数据  5 次没有获取到数据时提示检查网络环境
    private int count = 0;
    private Runnable mLoopRequestRunnable = () -> {
        if (count < IntegerConstant.RESTART_REQUEST_COUNT) {
            requestApp(StringConstant.DEVICE_ID);
            count++;
            if (count == IntegerConstant.RESTART_REQUEST_COUNT) {
                T.noNetTip(mContext);
            }
        }
    };

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
                if (isCancelRequest) return;
                LoginBean bean = response.body();
                if (bean != null) {
                    ArrayList<String> upList = null;
                    ArrayList<String> downList = null;
                    int code = bean.getCode();
                    if (code == IntegerConstant.RESULT_OK) {
                        LoginBean.DataBean dBean = bean.getData();
                        long df = dBean.getRefreshDataFrequency();
                        if (df > 0) {
                            IntegerConstant.REFRESH_DATA_FREQUENCY = df;// 用户数据刷新时间
                        }
                        long rf = dBean.getRefreshRelationFrequency();
                        if (rf > 0) {
                            IntegerConstant.REFRESH_RELATION__FREQUENCY = rf;// 用户关系刷新时间
                        }
                        long sf = dBean.getSortFrequency();
                        if (sf > 0) {
                            IntegerConstant.SORT_FREQUENCY = sf;// 用户数据排序刷新时间
                        }

                        String urlString = dBean.getSplashAdUrl();// 启动页图片地址
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
                            MyApp.setUpUrl(upList);
//                            intent.putStringArrayListExtra(StringConstant.BANNER_LIST_UP, upList);
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
                        if (downList != null && downList.size() > 0) {
                            L.d("downList", "downList size - > " + downList.size());
                            MyApp.setDownUrl(downList);
//                            intent.putStringArrayListExtra(StringConstant.BANNER_LIST_DOWN, downList);
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

                        mHandler.postDelayed(() -> {
                            Intent intent = new Intent(mContext, StandbyActivity.class);
                            startActivity(intent);
                            finish();
                        }, IntegerConstant.SPLASH_INTO_TIME);
                    } else {
                        if (count == IntegerConstant.RESTART_REQUEST_COUNT) {// 重复请求依然获取不到数据时也需要进入界面
                            startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                            finish();
                        } else {
                            mHandler.postDelayed(mLoopRequestRunnable, RESTART_REQUEST_TIME);
                        }
                    }
                } else {
                    if (count == IntegerConstant.RESTART_REQUEST_COUNT) {
                        startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                        finish();
                    } else {
                        mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.RESTART_REQUEST_TIME);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (count == IntegerConstant.RESTART_REQUEST_COUNT) {
                    startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                    finish();
                } else {
                    mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.RESTART_REQUEST_TIME);
                }
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
        mData.setCalorie(98.9f);
        mData.setStatus(1);
        mData.setMaximumHeartRate(100);
        mData.setExerciseDuration(20);
        mData.setMileage(100);
        mData.setUserId(47);
        mDataList.add(mData);

        mData = new MotionData();
        mData.setCourseId(99);
        mData.setAverageVelocity(34);
        mData.setCalorie(37.9f);
        mData.setStatus(1);
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

        L.i("uploadData", "uploadData -> " + uploadData.toString());

        model.enqueue(new Callback<UploadDataBean>() {
            @Override
            public void onResponse(Response<UploadDataBean> response, Retrofit retrofit) {
                UploadDataBean bean = response.body();
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    T.showShort(mContext, "数据上传成功");
                } else {
                    T.showShort(mContext, "数据上传失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                T.connectFailTip(mContext);
            }
        });
    }

    // 上传用户数据
    private void uploadUser() {
        UploadData uploadData = new UploadData();
        List<UserHeart> mDataList = new ArrayList<>();
        UserHeart mData = new UserHeart();
        mData.setUserId(47);
        mData.setHeart(76);
        mDataList.add(mData);

        mData = new UserHeart();
        mData.setUserId(48);
        mData.setHeart(78);
        mDataList.add(mData);

        uploadData.setUserHeartList(mDataList);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.111:8080")
//                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<UploadDataBean> model = service.addHeart(uploadData);

        L.i("uploadData", "uploadData -> " + uploadData.toString());

        model.enqueue(new Callback<UploadDataBean>() {
            @Override
            public void onResponse(Response<UploadDataBean> response, Retrofit retrofit) {
                UploadDataBean bean = response.body();
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    T.showShort(mContext, "数据上传成功");
                } else {
                    T.showShort(mContext, "数据上传失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                T.connectFailTip(mContext);
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            L.d("onKeyDown", "此时正在加载数据 所以拦截返回键");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = true;
        if (mHandler != null) {
            if (mLoopRequestRunnable != null) {
                mHandler.removeCallbacks(mLoopRequestRunnable);
                mLoopRequestRunnable = null;
            }
            mHandler = null;
        }

        imageBackground = null;
        advertisement = null;
        mContext = null;
    }
}
