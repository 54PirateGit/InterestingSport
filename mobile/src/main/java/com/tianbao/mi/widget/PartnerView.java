package com.tianbao.mi.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.bean.PartnerBean;

/**
 * View
 * Created by edianzu on 2017/11/2.
 */
public class PartnerView extends LinearLayout {

    private Context mContext;

    private ImageView imageHead;
    private TextView textName;

    public PartnerView(Context context) {
        this(context, null);
        mContext = context;
    }

    public PartnerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.item_partner, this);
        imageHead = findViewById(R.id.image_head);
        textName = findViewById(R.id.text_name);
    }

    public void updateView(PartnerBean bean) {
        // 用户头像
        String avatar = bean.getHead();
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(mContext).load(avatar).into(imageHead);
        } else {
            imageHead.setImageDrawable(mContext.getDrawable(R.mipmap.image_head_default));// 展示默认头像
        }

        // 用户名
        String name = bean.getNick();
        if (TextUtils.isEmpty(name)) {
            name = "用户";
        }
        textName.setText(name);

        invalidate();
    }
}
