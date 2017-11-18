package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.utils.SoundPlayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private Context mContext;
    private String playType;// 标识 点播 or 直播
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
                    SoundPlayUtils.play(IntegerConstant.SOUND_START_COURSE);

                    // 跳转到主界面将数据展示
                    Intent intentToMain = new Intent(mContext, MainActivity.class);
                    intentToMain.putExtra(StringConstant.PLAY_TYPE, playType);
                    startActivity(intentToMain);
                    isRun = false;// 停止线程
                    finish();
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
