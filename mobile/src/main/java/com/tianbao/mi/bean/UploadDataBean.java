package com.tianbao.mi.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 上传用户数据
 * Created by edianzu on 2017/11/12.
 */
@Data
public class UploadDataBean implements Serializable {

    private int code;

    private String message;
}
