package com.tianbao.mi.bean;

import java.util.Map;

import lombok.Data;

/**
 * Build
 * Created by edianzu on 2017/10/26.
 */
@Data
public class BindingBean {

    private int code;

    private Map<String, Map<String, Object>> data;

    private String message;
}
