package com.tianbao.mi.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tianbao.mi.R;

/**
 * SortView
 * Created by edianzu on 2017/11/6.
 */

public class SortView extends LinearLayout {

    private Context mContext;

    public SortView(Context context) {
        super(context);
        mContext = context;
    }

    public SortView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        inflate(mContext, R.layout.item_sort, this);
    }
}
