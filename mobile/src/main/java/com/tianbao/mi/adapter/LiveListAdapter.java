package com.tianbao.mi.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.LiveCourseBean;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.DateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 直播课程列表信息
 * Created by edianzu on 2017/11/4.
 */
public class LiveListAdapter extends BaseAdapter {

    private Context mContext;
    private List<LiveCourseBean.DataBean> mList;
    private int mIndex;// 焦点位置
    private int selectPosition = -1;// 选中位置

    public LiveListAdapter(Context context, List<LiveCourseBean.DataBean> list) {
        this.mContext = context;
        this.mList = list;
    }

    public int setList(List<LiveCourseBean.DataBean> list, int courseId) {
        this.mList = list;

        int index = 0;
        for (int i=0; i<mList.size(); i++) {
            if (mList.get(i).getId() == courseId) {
                index = i;
                break;
            }
        }
        notifyDataSetChanged();
        return index;
    }

    public void setList(List<LiveCourseBean.DataBean> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    // 上
    public void up(int index) {
        mIndex = index;
        notifyDataSetChanged();
    }

    // 下
    public void down(int index) {
        mIndex = index;
        notifyDataSetChanged();
    }

    // 确认
    public int ok(int index) {
        selectPosition = index;
        notifyDataSetChanged();
        return mList.get(selectPosition).getId();
    }

    // 获取课程 ID
    public int getCourseId(int index) {
        if (index >= 0 && index < mList.size()) return mList.get(index).getId();
        else return 0;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_live_list, parent, false);
            holder = new ViewHolder(convertView);
            holder.viewBackground.setAlpha(0.5f);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mIndex == position) {
            holder.viewBackground.setBackground(mContext.getDrawable(R.drawable.card_white_background));
        } else {
            holder.viewBackground.setBackground(mContext.getDrawable(R.drawable.card_background));
        }

//        if (selectPosition == position) {
//            holder.imageSelect.setVisibility(View.VISIBLE);
//        } else {
//            holder.imageSelect.setVisibility(View.GONE);
//        }

        LiveCourseBean.DataBean data = mList.get(position);

        // 选中状态
        if (data.isSelect()) {
            holder.imageSelect.setVisibility(View.VISIBLE);
            MyApp.setCourseId(data.getId());// 获取课程 ID

            if (StringConstant.LIVE_URL.equals("-1")) {// 获取直播地址
                StringConstant.LIVE_URL = data.getLiveUrl();
            }



            Toast.makeText(mContext, "id -> " + MyApp.getCourseId(), Toast.LENGTH_LONG).show();
        } else {
            holder.imageSelect.setVisibility(View.GONE);
        }

        // 教练头像
        String avatar = data.getCoach().getAvatar();
        if (TextUtils.isEmpty(avatar)) {
            holder.imageHead.setImageDrawable(mContext.getDrawable(R.mipmap.image_head_default));// 展示默认头像
        } else {
            Picasso.with(mContext).load(avatar).into(holder.imageHead);
        }

        // 教练名
        String name = data.getCoach().getNick();
        if (TextUtils.isEmpty(name)) name = "课程教练";
        holder.textName.setText(name);

        // 课程标题
        String title = data.getTitle();
        if (TextUtils.isEmpty(title)) title = "健身课程";
        holder.textTitle.setText(title);

        // 课程介绍
        String desc = data.getDescription();
        if (TextUtils.isEmpty(desc)) desc = "课程介绍";
        holder.textDescribe.setText(desc);

        // 课程时间
        String time = data.getShortTime();
        if (TextUtils.isEmpty(time)) {
            time = "课程时间正在安排";
        } else {
            time = DateUtils.timeFormat(time);
        }
        holder.textTime.setText(time);

        // 选中状态
//        int type = data.getSelectType();
//        if (type == IntegerConstant.LIVE_COURSE_SELECT_TYPE) {
//            holder.imageSelect.setVisibility(View.VISIBLE);
//        } else {
//            holder.imageSelect.setVisibility(View.GONE);
//        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.view_background)
        View viewBackground;
        @BindView(R.id.image_head)
        ImageView imageHead;// 教练头像
        @BindView(R.id.text_name)
        TextView textName;// 教练名
        @BindView(R.id.text_title)
        TextView textTitle;// 课程标题
        @BindView(R.id.text_describe)
        TextView textDescribe;// 课程描述
        @BindView(R.id.text_time)
        TextView textTime;// 课程时间
        @BindView(R.id.image_select)
        ImageView imageSelect;// 选中状态

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
