package com.tianbao.mi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tianbao.mi.R;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.utils.SendBroadUtil;

import java.util.List;

import static com.tianbao.mi.constant.IntegerConstant.SOUND_WARM;
import static com.tianbao.mi.constant.IntegerConstant.SOUND_YES;
import static com.tianbao.mi.constant.IntegerConstant.VIEW_TYPE_SORT;
import static com.tianbao.mi.constant.IntegerConstant.VIEW_TYPE_TIP;

/**
 * PartnerListAdapter
 * Created by edianzu on 2017/11/5.
 */

public class InfoListAdapter extends BaseAdapter {

    private Context mContext;
    private List<InformationBean> mList;

    public InfoListAdapter(Context context, List<InformationBean> list) {
        this.mContext = context;
        this.mList = list;

        if (mList.get(mList.size() - 1).getType() == VIEW_TYPE_SORT) {// 排名
            SendBroadUtil.sendPlayToService(mContext, SOUND_YES);
        } else if (mList.get(mList.size() - 1).getType() == VIEW_TYPE_TIP) {// 警告
            SendBroadUtil.sendPlayToService(mContext, SOUND_WARM);
        }
    }

    public void setList(List<InformationBean> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    // 布局管理器
    private LayoutInflater mLayoutInflater;

    @Override
    public int getCount() {
        return mList.size();  // 返回的最多的数据
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getType() == VIEW_TYPE_SORT) return VIEW_TYPE_SORT;
        else return IntegerConstant.VIEW_TYPE_TIP;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int type = getItemViewType(position);
        if (convertView == null) {
            if (mLayoutInflater == null) mLayoutInflater = LayoutInflater.from(mContext);
            if (type == VIEW_TYPE_SORT) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_sort, parent, false);
                viewHolder.textSortName1 = convertView.findViewById(R.id.text_sort_1);
                viewHolder.textSortName2 = convertView.findViewById(R.id.text_sort_2);
                viewHolder.textSortName3 = convertView.findViewById(R.id.text_sort_3);
                convertView.setTag(viewHolder);
            } else {
                convertView = mLayoutInflater.inflate(R.layout.item_tip, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textTipName = convertView.findViewById(R.id.text_name);
                viewHolder.textTip = convertView.findViewById(R.id.text_tip);
                convertView.setTag(viewHolder);
            }
        } else {
            if (type == VIEW_TYPE_SORT) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
        }

        InformationBean bean = mList.get(position);

        if (type == VIEW_TYPE_SORT) {
            List<InformationBean.SortBean> sortBeen = bean.getSortList();
            for (int i=0; i<sortBeen.size(); i++) {
                if (sortBeen.get(i).getSort() == 1) {
                    viewHolder.textSortName1.setText(sortBeen.get(i).getName());
                } else if (sortBeen.get(i).getSort() == 2) {
                    viewHolder.textSortName2.setText(sortBeen.get(i).getName());
                } else {
                    viewHolder.textSortName3.setText(sortBeen.get(i).getName());
                }
            }
        } else {
            String name = bean.getName();
            viewHolder.textTipName.setText(name);

            String tip = bean.getTip();
            viewHolder.textTip.setText(tip);
        }

        return convertView;
    }

    class ViewHolder {
        TextView textSortName1;
        TextView textSortName2;
        TextView textSortName3;

        TextView textTipName;
        TextView textTip;
    }
}
