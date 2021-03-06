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
import com.tianbao.mi.bean.FitUser;
import com.tianbao.mi.constant.IntegerConstant;

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
    private TextView textSort;// 排名
    private View viewBack;// 背景
    private TextView textLevel;// 级别
    private ImageView imageXl;// 心率

    private TextView textHeartRateUnit;// 心率单位

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
        textSort = findViewById(R.id.text_sort);
        viewBack = findViewById(R.id.view_background);
        textLevel = findViewById(R.id.text_level);
        imageXl = findViewById(R.id.image_xinlv);

        textHeartRateUnit = findViewById(R.id.text_heart_rate_unit);
    }

    public void setData(FitUser data) {
        String key1 = data.getKey();
        if (!TextUtils.isEmpty(key1)) {
            textKey.setText(key1);
        }

        // 用户头像
        String avatar = data.getAvatar();
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(mContext).load(avatar).into(imageHead);
        } else {
            Picasso.with(mContext).load(R.mipmap.image_head_default).into(imageHead);// 展示默认头像
        }

        // 数据
        String speed = data.getSpeed();
        if (TextUtils.isEmpty(speed) || speed.equals("NaN")) speed = "--";

//        int h = new Random().nextInt(220);
        String heart = String.valueOf(data.getHeartRate());
//        String heart = String.valueOf(h);
        if (TextUtils.isEmpty(heart) || heart.equals("NaN")) heart = "--";

//        int c = new Random().nextInt(100);
        String calorie = data.getKcalStr();
//        String calorie = String.valueOf(c);
        if (TextUtils.isEmpty(calorie) || calorie.equals("NaN")) calorie = "--";

        // 速度
        textSpeed.setText(speed);

        // 心率
        if (TextUtils.isEmpty(heart) || heart.equals("--") || heart.equals("0")) {// 没有获取到心率
            textHeartRateUnit.setTextColor(getResources().getColor(R.color.gray));
            textHeartRate.setTextColor(getResources().getColor(R.color.gray));
            imageXl.setImageDrawable(mContext.getDrawable(R.drawable.xinlv_gray));
        } else {
            textHeartRateUnit.setTextColor(getResources().getColor(R.color.white));
            textHeartRate.setTextColor(getResources().getColor(R.color.white));
            imageXl.setImageDrawable(mContext.getDrawable(R.drawable.xinlv));
        }
        textHeartRate.setText(heart);

        // 卡路里
        textCalorie.setText(calorie);

        // 排序
        int sort = data.getSort();
        textSort.setText(String.valueOf(sort));

        // 级别
        int level = data.getHearRateLevel();
        textLevel.setText(String.valueOf(level));
        invalidate();
    }

    /**
     * 排序
     */
    public void sort() {
        String sort = textSort.getText().toString();
        if (!TextUtils.isEmpty(sort) && sort.equals("1")) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no1));
            imageSort.setVisibility(VISIBLE);
        } else if (!TextUtils.isEmpty(sort) && sort.equals("2")) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no2));
            imageSort.setVisibility(VISIBLE);
        } else if (!TextUtils.isEmpty(sort) && sort.equals("3")) {
            imageSort.setBackground(getResources().getDrawable(R.drawable.no3));
            imageSort.setVisibility(VISIBLE);
        } else {
            imageSort.setVisibility(GONE);
        }
        invalidate();
    }

    // 根据心率在不同的范围内给 View 设置不同的背景色  不同的背景色代表着不同的提示信息
    public void updateView() {
        String level = textLevel.getText().toString();
        if (!TextUtils.isEmpty(level) && !level.equals("--")) {
            int l = Integer.valueOf(level);
            if (l == IntegerConstant.RELAX_HEAR_RATE) {// 放松热身
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_relax_background));

            } else if (l == IntegerConstant.BURNING_HEAR_RATE) {// 燃烧脂肪
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_burning_background));

            } else if (l == IntegerConstant.CONSUME_HEAR_RATE) {// 糖原消耗
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_consume_background));

            } else if (l == IntegerConstant.ACCUMULATION_HEAR_RATE) {// 乳酸堆积
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_accumulation_background));

            } else if (l == IntegerConstant.MAX_HEAR_RATE) {// 身体极限
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_limit_background));

            } else {// 意外情况  一般只在刚开始时出现 在有运动数据之后不会出现
                viewBack.setBackground(getResources().getDrawable(R.drawable.card_background));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        imageHead = null;
        textSpeed = null;
        textKey = null;
        textHeartRate = null;
        textCalorie = null;
        imageSort = null;
        textSort = null;
        viewBack = null;
        textHeartRateUnit = null;
        mContext = null;
    }
}
