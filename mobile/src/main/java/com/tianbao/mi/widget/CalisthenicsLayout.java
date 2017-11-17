package com.tianbao.mi.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tianbao.mi.utils.SystemUtils;

/**
 * 自动排版 View
 * Created by edianzu on 2017/11/2.
 */
public class CalisthenicsLayout extends LinearLayout {

    private Context mContext;

    public CalisthenicsLayout(Context context) {
        super(context);
        mContext = context;
    }

    public CalisthenicsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public CalisthenicsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void updateView() {
        int[] arr = SystemUtils.getAndroidScreenProperty(mContext);
        int width = (arr[0] - SystemUtils.dip2px(mContext, 40)) / 7;
        int height = (arr[1] - SystemUtils.dip2px(mContext, 40)) / 7;

        for (int i=0; i<getChildCount(); i++) {
            MemberView child = (MemberView) getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            child.setLayoutParams(layoutParams);

            child.sort();
            child.updateView();
        }
        invalidate();
    }
}
