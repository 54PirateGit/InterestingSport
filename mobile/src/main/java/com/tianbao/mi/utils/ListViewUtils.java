package com.tianbao.mi.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * List 设置相关
 * Created by edianzu on 2017/9/18.
 */

public class ListViewUtils {

    /**
     * 为 listView 动态设置高度（有多少条目就显示多少条目）
     *
     * @param listView 需要设置的 listView
     */
    public static void setListViewHeight(ListView listView) {
        // 获取 listView 的 adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        // listAdapter.getCount() 返回数据项的数目
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // listView.getDividerHeight() 获取子项间分隔符占用的高度
        // params.height 最后得到整个 ListView 完整显示需要的高度
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 为 listView 设置高度  指定高度
     *
     * @param listView 需要设置的 listView
     */
    public static void setListHeight(ListView listView) {
        // 获取 listView 的 adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int height = 0;
        View listItem = listAdapter.getView(0, null, listView);
        listItem.measure(0, 0);
        height += listItem.getMeasuredHeight();
        height = height * 3;// 一屏幕展示三条数据

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height + (listView.getDividerHeight() * 2);
        listView.setLayoutParams(params);
    }
}
