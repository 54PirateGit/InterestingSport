package com.tianbao.mi.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.tianbao.mi.adapter.InfoListAdapter;
import com.tianbao.mi.adapter.PartnerTipAdapter;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.bean.PartnerTipBean;

import java.util.ArrayList;
import java.util.List;

/**
 * AutoScrollListView
 * Created by edianzu on 2017/11/5.
 */
public class AutoScrollListView extends ListView {

    // 延时滚动时间
    private static int DELAY_TIME = 5000;

    private Context mContext;

    public AutoScrollListView(Context context) {
        this(context, null);
        mContext = context;
    }

    public AutoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    private List<InformationBean> iList;
    private List<PartnerTipBean> pList;

    public void setInfoList(List<InformationBean> list) {
        this.iList = list;
        setAdapter(iAdapter = new InfoListAdapter(mContext, iList));

        postDelayed(updateRunnable, DELAY_TIME);
    }

    public void setPartnerList(List<PartnerTipBean> list) {
        this.pList = list;
        setAdapter(pAdapter = new PartnerTipAdapter(mContext, pList));

        postDelayed(updatePartnerRunnable, DELAY_TIME);
    }

    // 更新数据
    public void updateData(InformationBean bean) {
        if (iList == null) iList = new ArrayList<>();
        iList.add(bean);
    }

    // 更新数据
    public void updatePartnerData(PartnerTipBean bean) {
        if (pList == null) pList = new ArrayList<>();
        pList.add(bean);
    }

    // 更新课程中的提示信息
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (iList.size() > 3) {
                iList.remove(0);
            }

            setAdapter(new InfoListAdapter(mContext, iList));

//            if (iAdapter == null) {
//                iAdapter = new InfoListAdapter(mContext, iList);
//                setAdapter(iAdapter);
//            } else {
//                iAdapter.setList(iList);
//            }

            postDelayed(this, DELAY_TIME);
        }
    };

    private InfoListAdapter iAdapter;
    private PartnerTipAdapter pAdapter;

    // 更新新加入的瘾伙伴
    private Runnable updatePartnerRunnable = new Runnable() {
        @Override
        public void run() {
            if (pList.size() > 3) {
                pList.remove(0);
            }

            if (pAdapter == null) {
                pAdapter = new PartnerTipAdapter(mContext, pList);
                setAdapter(pAdapter);
            } else {
                pAdapter.notifyDataSetChanged();
            }

            postDelayed(this, DELAY_TIME);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (updateRunnable != null) {
            removeCallbacks(updateRunnable);
            updateRunnable = null;
        }

        if (updatePartnerRunnable != null) {
            removeCallbacks(updatePartnerRunnable);
            updatePartnerRunnable = null;
        }

        if (iList != null) {
            iList.clear();
            iList = null;
        }

        if(pList != null) {
            pList.clear();
            pList = null;
        }
    }
}