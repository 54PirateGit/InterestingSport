package com.tianbao.mi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.widget.banner.BannerPagerAdapter;

import java.util.List;

/**
 * BannerAdapter
 * Created by edianzu on 2017/9/11.
 */
public class BannerAdapter extends BannerPagerAdapter {
    private int code;

    private Context mContext;
    private List<String> data;

    private List<Integer> testData;

    public BannerAdapter(Context context, int code) {
        mContext = context;
        this.code = code;
    }

    @Override
    public void setData(List data) {
        super.setData(data);
        if (code == 200) {
            this.data = data;
        } else {
            testData = data;
        }
    }

    /**
     * 只需要重写构造和这个方法即可
     * 在这里可以设置自己的 View,使用自己的图片加载库
     */
    @Override
    public View setView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_banner, null);
        ImageView image = view.findViewById(R.id.image);

        if (code == IntegerConstant.RESULT_OK) {
            // 加载网络图片
            Picasso.with(mContext).load(data.get(position)).into(image);
        } else {
            image.setImageBitmap(BitmapUtils.readBitMap(mContext, testData.get(position)));
        }
        return view;
    }
}
