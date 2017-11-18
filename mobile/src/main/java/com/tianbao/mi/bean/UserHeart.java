package com.tianbao.mi.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户
 * Created by edianzu on 2017/11/18.
 */
@Data
public class UserHeart implements Serializable {

    private int heart;

    private int userId;
}
