package com.tianbao.mi.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.bean.UserDataBean;

import java.util.Map;
import java.util.Random;

/**
 * 显示成员
 * Created by Administrator on 2017/10/26.
 */
public class MemberView extends LinearLayout {

    private ImageView imageHead;
    private TextView textSpeed;
    private TextView textKey;
    private TextView textHeartRate;
    private TextView textCalorie;
    private ImageView imageSort;// 排名
    private View viewBack;// 背景

    private Context mContext;

    public MemberView(Context context) {
        this(context, null);
        mContext = context;
    }

    public MemberView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.item_member, this);

        imageHead = findViewById(R.id.image_head);
        textSpeed = findViewById(R.id.text_speed);
        textKey = findViewById(R.id.text_key);
        textHeartRate = findViewById(R.id.text_heart_rate);
        textCalorie = findViewById(R.id.text_calorie);
        imageSort = findViewById(R.id.image_sort);
        viewBack = findViewById(R.id.view_background);
    }

    public void setUserData(Map<String, String> map) {
        if (map == null || map.size() <= 0) return ;// 没有数据

        // 数据
        String speed = map.get("rate");
        if (TextUtils.isEmpty(speed)) speed = "--";

        String heart = map.get("heartRate");
        if (TextUtils.isEmpty(heart)) heart = "--";

        int c = new Random().nextInt(100);
        String calorie = String.valueOf(c);
        if (TextUtils.isEmpty(calorie)) calorie = "--";

        // 速度
        textSpeed.setText(speed);

        // 心率
        textHeartRate.setText(heart);

        // 卡路里
        textCalorie.setText(calorie);

        invalidate();
    }

    public void setRelaData(Map<String, String> map) {
        if (map == null || map.size() <= 0) return ;// 没有数据

        // 用户头像
        String avatar = map.get("avatar");
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(mContext).load(avatar).transform(new CircleTransform()).into(imageHead);
        } else {
            imageHead.setImageDrawable(mContext.getDrawable(R.mipmap.image_head_default));// 展示默认头像
        }

        invalidate();
    }

    public void setData(UserDataBean data) {
        String key1 = data.getKey();
        if (!TextUtils.isEmpty(key1)) {
            textKey.setText(key1);
        }

        // 用户头像
        String avatar = data.getAvatar();
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(mContext).load(avatar).transform(new CircleTransform()).into(imageHead);
        } else {
            imageHead.setImageDrawable(mContext.getDrawable(R.mipmap.image_head_default));// 展示默认头像
        }

        // 数据
        String speed = data.getRate();
        if (TextUtils.isEmpty(speed)) speed = "--";

        String heart = data.getHeartRate();
        if (TextUtils.isEmpty(heart)) heart = "--";

        int c = new Random().nextInt(100);
        String calorie = String.valueOf(c);
        if (TextUtils.isEmpty(calorie)) calorie = "--";

        // 速度
        textSpeed.setText(speed);

        // 心率
        textHeartRate.setText(heart);

        // 卡路里
        textCalorie.setText(calorie);

        invalidate();
    }

    // 排名
    public void dataSort(int num) {
        if (num == 1) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no1));
            imageSort.setVisibility(VISIBLE);
            viewBack.setBackground(getResources().getDrawable(R.drawable.card_white_background));
        } else if (num == 2) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no2));
            imageSort.setVisibility(VISIBLE);
            viewBack.setBackground(getResources().getDrawable(R.drawable.card_white_background));
        } else if (num == 3) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no3));
            imageSort.setVisibility(VISIBLE);
            viewBack.setBackground(getResources().getDrawable(R.drawable.card_white_background));
        } else {
            imageSort.setVisibility(GONE);
            viewBack.setBackground(getResources().getDrawable(R.drawable.card_background));
        }
    }

    public String getKey() {
        return textKey.getText().toString();
    }
}
