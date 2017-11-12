package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.tianbao.mi.R;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.widget.banner.Banner;

import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;
        initView();
    }

    // 初始化视图
    private void initView() {
        Banner advertisement = findViewById(R.id.advertisement);
        advertisement.setAlpha(0.5f);

        init();
    }

    // 初始化
    private void init() {
        new Handler().postDelayed(() -> {
            int type = (int) SPUtils.get(mContext, StringConstant.DATA_TYPE_SP_KEY, 0);// 如果已经登录过则有数据
            if (type == 0) {
                startActivity(new Intent(mContext, LoginActivity.class));// 跳转到登录
                finish();
            } else {
                IntegerConstant.REFRESH_DATA_FREQUENCY = (long) SPUtils.get(mContext, StringConstant.REFRESH_DATA_FREQUENCY, 5 * 1000L);// 如果已经登录过则有数据
                IntegerConstant.REFRESH_RELATION__FREQUENCY = (long) SPUtils.get(mContext, StringConstant.REFRESH_RELATION__FREQUENCY, 5 * 60 * 1000L);// 如果已经登录过则有数据
                IntegerConstant.SORT_FREQUENCY = (long) SPUtils.get(mContext, StringConstant.SORT_FREQUENCY, 2 * 60 * 1000L);// 如果已经登录过则有数据
                IntegerConstant.STORE_ID = (int) SPUtils.get(mContext, StringConstant.STORE_ID_SP_KEY, 0);// 如果已经登录过则有数据

                String name = (String) SPUtils.get(mContext, StringConstant.STORE_NAME_KEY, "-1");
                if (name == null || name.equals("-1")) {
                    request(IntegerConstant.STORE_ID);
                } else {
                    StringConstant.STORE_NAME = name;

                    startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                    finish();
                }

                L.i("refresh", "用户数据刷新时间：" + IntegerConstant.REFRESH_DATA_FREQUENCY);
                L.i("refresh", "用户关系刷新时间：" + IntegerConstant.REFRESH_RELATION__FREQUENCY);
                L.i("refresh", "界面排序刷新时间：" + IntegerConstant.SORT_FREQUENCY);
            }
        }, IntegerConstant.INTO_MAIN_TIME);
    }

    // 首先获取一次课程信息  如果课程马上要开始  则进行数据加载  如果课程还有很长时间才开始则没必要加载太早
    private void request(int storeId) {
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

                startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                finish();
            }

            @Override
            public void onFailure(Throwable t) {
                startActivity(new Intent(mContext, StandbyActivity.class));// 没有课程信息
                finish();
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
