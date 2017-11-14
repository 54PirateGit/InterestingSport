package com.tianbao.mi.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.constant.StringConstant;

import butterknife.BindView;
import butterknife.ButterKnife;

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
                    // 跳转到主界面将数据展示
                    Intent intentToMain = new Intent(mContext, MainActivity.class);
                    startActivity(intentToMain);
                    finish();
                    isRun = false;// 停止线程
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
