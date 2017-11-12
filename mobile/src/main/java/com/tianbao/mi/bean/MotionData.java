package com.tianbao.mi.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 保存用户运动数据
 * Created by edianzu on 2017/11/10.
 */
@Data
public class MotionData implements Serializable {

    /**
     * 用户id
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
}
