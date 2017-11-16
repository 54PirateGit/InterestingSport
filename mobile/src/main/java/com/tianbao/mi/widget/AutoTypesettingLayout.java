package com.tianbao.mi.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.tianbao.mi.utils.SystemUtils;

/**
 * 自动排版 View
 * Created by edianzu on 2017/11/2.
 */
public class AutoTypesettingLayout extends LinearLayout {

    private Context mContext;

    public AutoTypesettingLayout(Context context) {
        super(context);
        mContext = context;
    }

    public AutoTypesettingLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AutoTypesettingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void updateView() {
        int[] arr = SystemUtils.getAndroidScreenProperty(mContext);
        int width = (arr[0] - SystemUtils.dip2px(mContext, 40)) / 7;
        int height = (arr[1] - SystemUtils.dip2px(mContext, 40)) / 7;

        for (int i=0; i<getChildCount(); i++) {
            MemberView child = (MemberView) getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) child.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            child.setLayoutParams(layoutParams);

            child.sort();
            child.updateView();
        }
        invalidate();
    }

    // 设置透明度
    public void setAlpha(boolean isFront) {
        for (int i=0; i<getChildCount(); i++) {
            View child = getChildAt(i);
            if (isFront) {
                child.setAlpha(0.5f);
            } else {
                child.setAlpha(0);
            }
        }
    }

    // 启动动画
    public void startAnim(boolean isFront) {
        for (int i=0; i<getChildCount(); i++) {
            View child = getChildAt(i);
            if (isFront) {
                animFront(child);
            } else {
                animBack(child);
            }
        }
    }

    // 处于前面的 View 执行的动画
    private void animFront(View view) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "rotationX", 0, 180f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 0);
        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(objectAnimator1, objectAnimator2);
        set1.setDuration(1000L);
        set1.start();

        new Handler().postDelayed(() -> {
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(view, "rotationX", 180f, 0);
            AnimatorSet set2 = new AnimatorSet();
            set2.playTogether(objectAnimator3);
            set2.setDuration(1000L);
            set2.start();
        }, 1200L);
    }

    // 处于后面的 View 执行的动画
    private void animBack(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 0.5f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(objectAnimator);
        set.setDuration(1000L);
        set.start();
    }
}
