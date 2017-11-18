package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.T;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 课程结束跳转至此
 * 11/06
 */
public class CourseEndActivity extends Activity {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_year)
    TextView textYear;
    @BindView(R.id.text_time)
    TextView textTime;

    private Context mContext;
    private Handler mHandler = new Handler();

    private MediaPlayer mp;

    private int timer = 180;

    private void setFront() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "font/FZKTJT.ttf");
        textTitle.setTypeface(tf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_end);
        mContext = this;
        ButterKnife.bind(this);
        setFront();

        initView();
        initData();
    }

    private void playSound() {
        try {
            mp = MediaPlayer.create(CourseEndActivity.this, R.raw.course_end1);// 重新设置要播放的音频
            mp.start();// 开始播放
            mp.setOnCompletionListener(m -> {
                mp = MediaPlayer.create(mContext, R.raw.course_end2);
                mp.start();
            });
        } catch (Exception e) {
            e.printStackTrace();// 输出异常信息
        }
    }

    // 初始化视图
    private void initView() {
        Bitmap bitmapBackground = BitmapUtils.readBitMap(mContext, R.drawable.end_background);
        imageBackground.setImageBitmap(bitmapBackground);

        String string = (String) SPUtils.get(mContext, StringConstant.TIME_YEAR, "2017");
        textYear.setText(string);

        mHandler.postDelayed(mCountDownRunnable, 1000L);// 倒计时 三分钟后结束
    }

    // 初始化数据
    private void initData() {
        playSound();
    }

    // 倒计时
    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            timer--;
            textTime.setText(String.valueOf(timer));
            if (timer > 0) {
                mHandler.postDelayed(this, 1000L);
            } else {
                startActivity(new Intent(mContext, StandbyActivity.class));
                finish();
            }
        }
    };

    private long time;// 保存点击返回键的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            long currentTime = System.currentTimeMillis();
            if (currentTime - time > IntegerConstant.APP_EXIT_TIME) {
                T.alwaysLong(mContext, "再按一次返回键退出程序");
                time = currentTime;
            } else {
                finish();
                MyApp.appExit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownRunnable != null) {
            mHandler.removeCallbacks(mCountDownRunnable);
            mCountDownRunnable = null;
        }
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }

        imageBackground = null;
        textTitle = null;
        textYear = null;
        textTime = null;

        mHandler = null;
        mContext = null;
    }
}
