package com.tianbao.mi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tianbao.mi.R;
import com.tianbao.mi.bean.PartnerTipBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 瘾伙伴加入
 * Created by edianzu on 2017/11/9.
 */
public class PartnerTipAdapter extends BaseAdapter {

    private Context mContext;
    private List<PartnerTipBean> mList;

    public PartnerTipAdapter(Context context, List<PartnerTipBean> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_partner_tip, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PartnerTipBean bean = mList.get(position);

        String name = bean.getName();
        if (TextUtils.isEmpty(name)) name = "NULL";
        holder.textName.setText(name);

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.text_name)
        TextView textName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
