package com.tianbao.mi.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 点播列表信息
 * Created by edianzu on 2017/11/15.
 */
@Data
public class OnDemandCourseBean implements Serializable {

    private int code;
    private String message;
    private List<DataBean> data;

    @Data
    public static class DataBean {
        private CoachBean coach;
        private String crowd;
        private String description;
        private int id;
        private String liveUrl;
        private String shortTime;
        private int stock;
        private String time;
        private String title;

        @Data
        public static class CoachBean {
            private String avatar;
            private String desc;
            private int id;
            private String nick;
        }
    }
}
