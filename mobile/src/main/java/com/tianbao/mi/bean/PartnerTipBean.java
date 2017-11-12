package com.tianbao.mi.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * InformationBean
 * Created by edianzu on 2017/11/6.
 */
@Data
public class PartnerTipBean implements Serializable {

    private int type;

    private String name;

    private String tip;
}
