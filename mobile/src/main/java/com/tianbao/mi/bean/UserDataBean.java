package com.tianbao.mi.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.Data;

/**
 * UserDataBean
 * Created by edianzu on 2017/10/26.
 */
@Data
public class UserDataBean implements Serializable, Comparable<UserDataBean> {

    private String key;

    private String avatar;

    private String nick;

    private String sex;

    private String openId;

    private String heartRate;

    private String rate;

    private String userId;

    private int calorie;

    private int sort;// 排名  只排123

    private boolean isNotOnline;// 是否不在线

    private long lastTime;// 最后获取到数据时的时间

    @Override
    public int compareTo(@NonNull UserDataBean o) {
        int i = o.getCalorie() - this.getCalorie();// 按卡路里排序
        return i;
    }
}
