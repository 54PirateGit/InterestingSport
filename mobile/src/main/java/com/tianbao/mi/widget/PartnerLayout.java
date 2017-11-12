package com.tianbao.mi.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动排版 View
 * Created by edianzu on 2017/11/2.
 */
public class PartnerLayout extends LinearLayout {

    private Context mContext;
    private Handler mHandler = new Handler();
    private List<PartnerView> vList;
    private List<PartnerView> tList;
    private boolean isLoop;

    private int count = 6;

    public PartnerLayout(Context context) {
        super(context);
        mContext = context;
    }

    public PartnerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public PartnerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    // 循环展示
    private Runnable mLoopListRunnable = new Runnable() {
        @Override
        public void run() {
            if (vList != null && vList.size() > 0) {
                if (vList.size() > 6) {
                    if (tList != null && tList.size() > 0) {
                        if (getChildCount() == tList.size()) {
                            for (int i=0; i< tList.size(); i++) {
                                PartnerView child = (PartnerView) getChildAt(i);
                                animFront(child);
                            }
                        }
                    }

                    new Handler().postDelayed(() -> {
                        removeAllViews();
                        if (tList == null) tList = new ArrayList<>();
                        else tList.clear();
                        for (; position<vList.size(); position++) {
                            if (tList.size() == 6) break;
                            tList.add(vList.get(position));
                        }
                        if (tList.size() < 6) position = 0;
                        for (int i=0; i<tList.size(); i++) {
                            addView(tList.get(i));
                        }
                        for (int i=0; i<getChildCount(); i++) {
                            animBack(getChildAt(i));
                        }
                        invalidate();
                    }, 1200L);
                } else {
                    removeAllViews();
                    if (tList == null) tList = new ArrayList<>();
                    else tList.clear();
                    for (int i=0; i<vList.size(); i++) {
                        tList.add(vList.get(i));
                    }
                    for (int i=0; i<tList.size(); i++) {
                        View view = tList.get(i);
                        view.setAlpha(0.5f);
                        addView(view);
                    }
                    invalidate();
                }
            }
            mHandler.postDelayed(this, 5000L);
        }
    };

    // 有心的瘾伙伴加入
    public void setPartnerView(PartnerView view) {
        if (vList == null) vList = new ArrayList<>();
        vList.add(view);
    }

    // 将瘾伙伴加到列表中
    public void setList(List<PartnerView> list) {
        vList = list;
        if (!isLoop) {
            mHandler.post(mLoopListRunnable);
            isLoop = true;
        }
    }

    // 处于前面的 View 执行的动画
    private void animFront(View view) {
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "rotationX", 0, 360f);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "alpha", 0.5f, 0);
        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(objectAnimator1, objectAnimator2);
        set1.setDuration(1200L);
        set1.start();
    }

    // 处于前面的 View 执行的动画
    private void animBack(View view) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 0.5f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(objectAnimator);
        set.setDuration(1200L);
        set.start();
    }

    private int position = 0;// 当前所在位置

    // 循环展示
//    private Runnable mLoopListRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (vList != null) {
//                if (vList.size() > 0 && vList.size() <= count) {
//                    removeAllViews();
//                    position = 0;
//                    for (int i = 0; i < vList.size(); i++) {
//                        if (getChildCount() <= count) {
//                            addView(vList.get(i));
//                        }
//                        position++;
//                    }
//                } else if (vList.size() > count) {
//                    if (getChildCount() < count) {
//                        for (int i=0; i<count; i++) {
//                            addView(vList.get(i));
//                            position++;
//                        }
//                    } else {
//                        removeViewAt(0);
//                        addView(vList.get(position));
//                        position++;
//                        if (position == vList.size()) {
//                            position = 0;
//                        }
//                    }
//                }
//
//                for (int i = 0; i < getChildCount(); i++) {
//                    PartnerView child = (PartnerView) getChildAt(i);
//                    child.setAlpha(0.5f);
//                }
//                invalidate();
//            }
//            mHandler.postDelayed(this, 5000L);
//        }
//    };
}
