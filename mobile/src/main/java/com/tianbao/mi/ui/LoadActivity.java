package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.utils.DialogUtils;
import com.tianbao.mi.utils.SendBroadUtil;
import com.tianbao.mi.utils.SoundPlayUtils;

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

import static com.tianbao.mi.constant.ConfigConstant.DEVICE_ID;
import static com.tianbao.mi.constant.IntegerConstant.RESTART_REQUEST_TIME;
import static com.tianbao.mi.constant.IntegerConstant.SOUND_START_COURSE;
import static com.tianbao.mi.constant.IntegerConstant.SOUND_START_LOAD;

/**
 * 加载界面
 * 10/23
 */
public class LoadActivity extends Activity {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.text_year)
    TextView textYear;// 当前年份
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.image_left)
    ImageView imageLeft;// 左边图片
    @BindView(R.id.image_right)
    ImageView imageRight;// 右边图片

    private Handler mHandler = new Handler();
    private Context mContext;
    private String playType;// 标识 点播 or 直播
    private String url;// 播放地址
    private boolean isRun = true;// 控制加载进度的线程
    private int i = 0;

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

        Intent intent = getIntent();
        if (intent == null) return ;
        playType = intent.getStringExtra(StringConstant.PLAY_TYPE);
        url = intent.getStringExtra(StringConstant.PLAY_URL);

        SendBroadUtil.sendPlayToService(mContext, SOUND_START_LOAD);
//        SoundPlayUtils.play(SOUND_START_LOAD);// 播放背景音乐
    }

    // 初始化视图
    private void initView() {
        Bitmap bitmap = BitmapUtils.readBitMap(mContext, R.drawable.touyin5);
        imageBackground.setImageBitmap(bitmap);
        textYear.setText(StringConstant.TIME_YEAR);

        // 图片
        List<String> urls = MyApp.getLoadUrl();
        if (urls == null || urls.size() <= 0) {
            imageLeft.setImageBitmap(BitmapUtils.readBitMap(mContext, R.drawable.d1));
            imageRight.setImageBitmap(BitmapUtils.readBitMap(mContext, R.drawable.d2));
        } else {
            if (urls.size() == 1) {
                if (mContext != null) {
                    imageLeft.setImageBitmap(BitmapUtils.readBitMap(mContext, R.drawable.d1));
                    Picasso.with(mContext).load(R.drawable.d2).into(imageRight);
                }
            } else if (urls.size() == 2) {
                if (mContext != null) {
                    Picasso.with(mContext).load(urls.get(0)).into(imageLeft);
                    Picasso.with(mContext).load(urls.get(1)).into(imageRight);
                }
            }
        }

        new Thread(() -> {
            // 模拟加载进度
            while (isRun) {
                if (i < 100) {
                    i++;
                    try {
                        Thread.sleep(40L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (i == 100) {
                    if (MyApp.verification()) {// 时间验证通过
//                        SoundPlayUtils.play(SOUND_START_COURSE);
                        SendBroadUtil.sendPlayToService(mContext, SOUND_START_COURSE);

                        // 跳转到主界面将数据展示
                        Intent intentToMain = new Intent(mContext, MainActivity.class);
                        intentToMain.putExtra(StringConstant.PLAY_TYPE, playType);
                        startActivity(intentToMain);
                        isRun = false;// 停止线程
                        finish();
                    } else {
                        requestApp();
                        isRun = false;
                    }
                }
            }
        }).start();
    }

    private int count4 = 0;
    private Runnable mGetAppInfoRunnable = () -> {
        if (count4 < IntegerConstant.RESTART_REQUEST_COUNT) {
            requestApp();
            count4++;
        }
    };

    // 获取配置信息
    private void requestApp() {
        Map<String, String> param = new HashMap<>();
        param.put("deviceId", DEVICE_ID);

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
                    int code = bean.getCode();
                    if (code == IntegerConstant.RESULT_OK) {
                        SoundPlayUtils.play(SOUND_START_COURSE);

                        // 跳转到主界面将数据展示
                        Intent intentToMain = new Intent(mContext, MainActivity.class);
                        intentToMain.putExtra(StringConstant.PLAY_TYPE, playType);
                        startActivity(intentToMain);
                        isRun = false;// 停止线程
                        finish();
                    } else {
                        String message = bean.getMessage();
                        if (!TextUtils.isEmpty(message) && message.equals("账户无效")) {
                            DialogUtils.showDialog(LoadActivity.this, "账号已过期，请联系管理员！");
                        } else {
                            if (count4 == IntegerConstant.RESTART_REQUEST_COUNT) {// 重复请求依然获取不到数据
                                DialogUtils.showDialog(LoadActivity.this, "系统启动失败，请联系恬宝科技公司！");
                            } else {
                                mHandler.postDelayed(mGetAppInfoRunnable, RESTART_REQUEST_TIME);
                            }
                        }
                    }
                } else {
                    if (count4 == IntegerConstant.RESTART_REQUEST_COUNT) {
                        DialogUtils.showDialog(LoadActivity.this, "系统启动失败，请联系恬宝科技公司！");
                    } else {
                        mHandler.postDelayed(mGetAppInfoRunnable, RESTART_REQUEST_TIME);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (count4 == IntegerConstant.RESTART_REQUEST_COUNT) {
                    DialogUtils.showDialog(LoadActivity.this, "账户信息验证失败，请联系恬宝科技公司！");
                } else {
                    mHandler.postDelayed(mGetAppInfoRunnable, RESTART_REQUEST_TIME);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
        imageBackground = null;
        textYear = null;
        textTitle = null;
        imageLeft = null;
        imageRight = null;
        mContext = null;
        playType = null;
    }
}
