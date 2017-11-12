package com.tianbao.mi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.bean.PartnerBean;

import java.util.List;

/**
 * PartnerListAdapter
 * Created by edianzu on 2017/11/5.
 */

public class PartnerListAdapter extends BaseAdapter {

    private List<PartnerBean> mList;
    private Context mContext;

    public PartnerListAdapter(Context context, List<PartnerBean> list) {
        this.mContext = context;
        this.mList = list;
    }

    // 布局管理器
    private LayoutInflater mLayoutInflater;

    @Override
    public int getCount() {
        return mList.size();  // 返回的最多的数据
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (mLayoutInflater == null) mLayoutInflater = LayoutInflater.from(parent.getContext());
            convertView = mLayoutInflater.inflate(R.layout.item_partner, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textName = convertView.findViewById(R.id.text_name);
            viewHolder.imageHead = convertView.findViewById(R.id.image_head);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PartnerBean bean = mList.get(position);

        viewHolder.textName.setText(bean.getNick());
        Picasso.with(mContext).load(bean.getHead()).into(viewHolder.imageHead);

        return convertView;
    }

    class ViewHolder {
        ImageView imageHead;
        TextView textName;
    }
}
