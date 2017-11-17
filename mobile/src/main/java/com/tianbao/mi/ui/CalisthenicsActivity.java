package com.tianbao.mi.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tianbao.mi.R.id.image_background;

/**
 * 团操数据展示界面
 * 11/17
 */
public class CalisthenicsActivity extends AppCompatActivity {

    @BindView(image_background)
    ImageView imageBackground;// 背景图
    @BindView(R.id.view_info)
    View viewInfo;// 信息板

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calisthenics);

        ButterKnife.bind(this);
        mContext = this;

        initView();
        initData();
    }

    // 初始化视图
    private void initView() {
        viewInfo.setAlpha(0.5f);
        Picasso.with(mContext).load(R.drawable.kec).into(imageBackground);// 设置背景图片
    }

    // 初始化数据
    private void initData() {

    }
}
