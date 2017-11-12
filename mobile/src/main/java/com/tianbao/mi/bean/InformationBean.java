package com.tianbao.mi.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * InformationBean
 * Created by edianzu on 2017/11/6.
 */
@Data
public class InformationBean implements Serializable {

    private int type;

    private String name;

    private List<SortBean> sortList;

    private String tip;

    @Data
    public static class SortBean implements Serializable {

        private String name;

        private int sort;
    }
}
