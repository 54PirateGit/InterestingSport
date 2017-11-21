package com.tianbao.mi.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 保存用户运动数据
 * Created by edianzu on 2017/11/10.
 */
@Data
public class GymData implements Serializable {

    /**
     * 用户 id
     */
    private int userId;

    /**
     * 课程 ID
     */
    private int courseId;

    /**
     * 状态
     */
    private int status;

    /**
     * 卡路里
     */
    private float calorie;

    /**
     * 运动时长
     */
    private int exerciseDuration;

    /**
     * 平均速度
     */
    private float averageVelocity;

    /**
     * 最高速度
     */
    private float topSpeed;

    /**
     * 里程
     */
    private float mileage;

    /**
     * 最大心率
     */
    private int maximumHeartRate;

    /**
     * 平均心率
     */
    private int averageHeartRate;

    private int maximum;

    private int accumulation;

    private int consume;

    private int burning;

    private int relax;

    private int maximumPct;

    private int accumulationPct;

    private int consumePct;

    private int burningPct;

    private int relaxPct;

    /**
     * 课程类型。
     1：单车
     2：跑步机
     3：团操
     */
    private int type;
}
