package com.tianbao.mi.bean;

import java.util.List;

import lombok.Data;

/**
 * 课程、教练信息
 * Created by edianzu on 2017/10/28.
 */
@Data
public class CourseInfoBean {

    private int code;
    private DataBean data;
    private String message;

    @Data
    public static class DataBean {
        private CourseBean course;
        private StoreBean store;

        @Data
        public static class CourseBean {
            private String care;
            private String crowd;
            private String description;
            private String faq;
            private int id;
            private String liveStatus;
            private String price;
            private String shortTime;
            private int stock;
            private String time;
            private String title;
            private String trainingEffect;
            private List<String> mainPics;
            private List<String> subPics;
            private List<String> tags;
            private CoachBean coach;

            @Data
            public static class CoachBean {
                private String avatar;
                private String desc;
                private int id;
                private String nick;
            }
        }

        @Data
        public static class StoreBean {
            private String address;
            private String describe;
            private int id;
            private String name;
            private String poi;
        }
    }
}
