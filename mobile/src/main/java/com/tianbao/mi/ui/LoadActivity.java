package com.tianbao.mi.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.RecordBean;
import com.tianbao.mi.bean.UserDataBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.SoundPlayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 加载界面
 * 10/23
 */
public class LoadActivity extends AppCompatActivity {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.text_year)
    TextView textYear;// 当前年份
    @BindView(R.id.text_title)
    TextView textTitle;

    private Context mContext;

    private List<UserDataBean> mList;// 拼装之后的用户数据 -> 树莓派上获取的用户运动数据 + 用户的硬件的绑定关系数据

    Map<String, Map<String, String>> dMap;
    Map<String, Map<String, String>> rMap;

    private void setFront() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "font/FZKTJT.ttf");
        textTitle.setTypeface(tf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        mContext = this;
        ButterKnife.bind(this);
        setFront();
        initView();
    }

    boolean isRun = true;// 控制加载进度的线程
    int i = 0;

    // 初始化视图
    private void initView() {
        Picasso.with(mContext).load(R.drawable.touyin5).into(imageBackground);
        textYear.setText(StringConstant.TIME_YEAR);

        Intent intent = getIntent();
        if (intent != null) {
            dKey = intent.getStringArrayListExtra(StringConstant.USER_KEY);
        }

        new Thread(() -> {
            // 模拟加载进度
            while (isRun) {
                if (i < 100) {
                    i++;
                    try {
                        Thread.sleep(35L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (i == 100) {
                    requestUserData();// 请求服务器获取数据

                    isRun = false;// 停止线程
                }
            }
        }).start();
    }

    private List<String> dKey = new ArrayList<>();

    // 网络请求获取网络数据  获取用户数据
    private void requestUserData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL_PI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<RecordBean> model = service.requestRecord();
        model.enqueue(new Callback<RecordBean>() {
            @Override
            public void onResponse(Response<RecordBean> response, Retrofit retrofit) {
                RecordBean recordBean = response.body();
                int code = recordBean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    dMap = recordBean.getData();
                    for (Map.Entry<String, Map<String, String>> entry : dMap.entrySet()) {
                        System.out.println("key = " + entry.getKey() + " and value = " + entry.getValue());
                        if (!dKey.contains(entry.getKey())) dKey.add(entry.getKey());
                    }

                    requestUserInfo();// 根据 keyId 获取用户绑定关系
                } else {
                    Toast.makeText(mContext, "code == " + code, Toast.LENGTH_SHORT).show();

                    i = 0;
                    Toast.makeText(mContext, "加载失败，等待重新加载", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "errorCode != 0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 获取用户的绑定关系
    private void requestUserInfo() {
        Map<String, List<String>> param = new HashMap<>();
        param.put("headIds", dKey);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<BuildBean> model = service.getBuild(param);
        model.enqueue(new Callback<BuildBean>() {
            @Override
            public void onResponse(Response<BuildBean> response, Retrofit retrofit) {
                BuildBean buildBean = response.body();
                int code = buildBean.getCode();
                if (IntegerConstant.RESULT_OK == code) {
                    rMap = buildBean.getData();
                    mList = initData();// 成功获取数据之后对原始数据进行组装

                    SoundPlayUtils.play(4);// 背景音乐

                    // 跳转到主界面将数据展示
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra(StringConstant.USER_DATA_LIST, (Serializable) mList);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, "code == " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "errorCode != 0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 组装数据 树莓派 + 服务器
    private List<UserDataBean> initData() {
        if (dKey == null || dKey.size() <= 0) return null;
        List<UserDataBean> data = new ArrayList<>();
        UserDataBean userDataBean;
        for (String key : dKey) {
            userDataBean = new UserDataBean();
            Map<String, String> dData = dMap.get(key);
            Map<String, String> rData = rMap.get(key);

            userDataBean.setKey(key);

            if (rData == null) rData = new HashMap<>();
            if (dData == null) dData = new HashMap<>();

            userDataBean.setAvatar(rData.get("avatar"));
            userDataBean.setNick(rData.get("nick"));
            userDataBean.setSex(rData.get("sex"));
            userDataBean.setOpenId(rData.get("openId"));
            userDataBean.setUserId(rData.get("userId"));

            userDataBean.setHeartRate(dData.get("heartRate"));
            userDataBean.setRate(dData.get("rate"));

            data.add(userDataBean);
        }
        return data;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
