package com.tianbao.mi.bean;

import lombok.Data;

/**
 * 登录信息
 * Created by edianzu on 2017/10/28.
 */
@Data
public class LoginBean {

    private int code;

    private DataBean data;

    private String message;

    @Data
    public class DataBean {
        private String account;// 账号

        private String description;// 描述

        private String deviceId;// 设备ID

        private String id;

        private String password;// 密码

        private String mipushId;// 小米推送id

        private int status;// 状态

        private int storeId;// 店ID

        private int type;// 器械类型  1:动感单车；2:跑步机

        private Long refreshDataFrequency;// 刷新数据频率

        private Long refreshRelationFrequency;// 刷新用户关系频率

        private Long sortFrequency;// 排序频率

        private String splashAdUrl;

        private String standbyUpAdUrl;

        private String standbyDownAdUrl;

        private String loadLeftAdUrl;

        private String loadRightAdUrl;
    }
}
