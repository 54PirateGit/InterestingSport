package com.tianbao.mi.bean;

import java.util.List;

import lombok.Data;

/**
 * LiveCourseBean
 * Created by edianzu on 2017/11/4.
 */
@Data
public class LiveCourseBean {
    /**
     * code : 200
     * data : [{"care":"事项1事项2","coach":{"avatar":"http://img.hitianbao.com/wx/course/0.jpeg","desc":"效果\n效果2\n效果3","id":1,"nick":"棋子"},"crowd":"人群1人群2","description":"描述3描述4","faq":"问答","id":98,"mainPics":["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic"],"price":"¥29.9","shortTime":"11.30-20.20","stock":30,"subPics":["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic","http://www.hitianbao.com/b.pic3","http://www.hitianbao.com/b.pic4","http://www.hitianbao.com/b.pic5"],"tags":["a","b"],"time":"2017-11-04 11.30-20.20","title":"课程1","trainingEffect":"效果1效果2"},{"care":"事项1事项2","coach":{"avatar":"http://img.hitianbao.com/wx/course/0.jpeg","desc":"效果\n效果2\n效果3","id":1,"nick":"棋子"},"crowd":"人群1人群2","description":"描述3\r\n描述4","faq":"问答","id":99,"mainPics":["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic"],"price":"¥29.9","shortTime":"10.00-22.20","stock":30,"subPics":["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic","http://www.hitianbao.com/b.pic3","http://www.hitianbao.com/b.pic4","http://www.hitianbao.com/b.pic5"],"tags":["a","b"],"time":"2017-11-04 10.00-22.20","title":"动感单车（花式）","trainingEffect":"效果1效果2"}]
     * message : SUCCESS
     */
    private int code;
    private String message;
    private List<DataBean> data;

    @Data
    public static class DataBean {
        /**
         * care : 事项1事项2
         * coach : {"avatar":"http://img.hitianbao.com/wx/course/0.jpeg","desc":"效果\n效果2\n效果3","id":1,"nick":"棋子"}
         * crowd : 人群1人群2
         * description : 描述3描述4
         * faq : 问答
         * id : 98
         * mainPics : ["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic"]
         * price : ¥29.9
         * shortTime : 11.30-20.20
         * stock : 30
         * subPics : ["http://www.hitianbao.com/a.pic","http://www.hitianbao.com/b.pic","http://www.hitianbao.com/b.pic3","http://www.hitianbao.com/b.pic4","http://www.hitianbao.com/b.pic5"]
         * tags : ["a","b"]
         * time : 2017-11-04 11.30-20.20
         * title : 课程1
         * trainingEffect : 效果1效果2
         */
        private String care;
        private CoachBean coach;
        private String crowd;
        private String description;
        private String faq;
        private int id;
        private String liveStatus;
        private String liveUrl;
        private String price;
        private String shortTime;
        private int stock;
        private String time;
        private String title;
        private String trainingEffect;
        private List<String> mainPics;
        private List<String> subPics;
        private List<String> tags;
        private boolean select;

        @Data
        public static class CoachBean {
            /**
             * avatar : http://img.hitianbao.com/wx/course/0.jpeg
             * desc : 效果
             效果2
             效果3
             * id : 1
             * nick : 棋子
             */
            private String avatar;
            private String desc;
            private int id;
            private String nick;
        }
    }
}
