package com.tianbao.mi.bean;

import java.util.Map;

import lombok.Data;

/**
 * RecordBean
 * Created by edianzu on 2017/10/26.
 */
@Data
public class RecordBean {

    private int code;

    private Map<String, Map<String, String>> data;

    private String message;

    // map key
//    exerciseTime;
//    heartRate;
//    rate;
}
