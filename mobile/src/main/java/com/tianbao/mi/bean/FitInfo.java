package com.tianbao.mi.bean;

import com.tianbao.mi.constant.IntegerConstant;

import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import java.io.Serializable;

/**
 * FitInfo
 * Created by edianzu on 2017/11/20.
 */
public class FitInfo implements Serializable {

    /**
     * 踏频
     **/
    private float cadence;

    /**
     * 踏频间隔时间，单位秒
     **/
    private float interval4Cadence;

    /**
     * 踏频上报最近时间
     **/
    private LocalDateTime lastReport4Cadence = new LocalDateTime();

    /**
     * 当前心率
     **/
    private int heartRate;

    /**
     * 心率上报最近时间
     **/
    private LocalDateTime lastReport4HeartRate = new LocalDateTime();

    /**
     * 当前速度
     **/
    private float speed;

    /**
     * 动感单车大卡计算：
     * 1. 体重 X 时速 X 0.45 X 时长（小时）
     * 2. 如果心率在有氧区域，加一点系数
     * <p>
     * 此处算法基准是 60 公斤人，骑行一小时，速度 22 公里消耗 600 大卡
     **/
    private void getKcal4Cycle(FitUser user, float perimeter, LocalDateTime now) {
        // 获取时速
        float speed = calSpeed(perimeter);
        this.speed = speed;
//        user.setSpeed(this.speed);

        if (this.speed <= 1) {// 没有数据
            if (user.getLastTime() == 0) {// 第一次没有数据
                user.setLastTime(System.currentTimeMillis());
            } else {
                if (!user.isNotOnline()) {// 用户还在线
                    long lastTime = user.getLastTime();
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTime >= IntegerConstant.DATA_NEUTRAL_GEAR_TIME) {// 用户数据空挡已经超过限定时间
                        user.setIsNotOnline(true);// 设置用户已经掉线
                    }
                } else {
                    // 用户已经掉线
                }
            }
        } else {// 有数据 记录数据的同时记录修改时间
            user.setSpeed(this.speed);
            user.setLastTime(System.currentTimeMillis());
            user.setIsNotOnline(false);
        }

        // 获取上次上报和当前上报的时间差（秒）
        float seconds = Seconds.secondsBetween(lastReport4Cadence, now).getSeconds();

        if (seconds < 1) seconds = interval4Cadence;
        if (seconds > 120) seconds = 120; // 上报时间差了两分钟，即按两分钟算

        float kcal = user.getWeight() * 0.45f * speed * seconds / 3600f;

        // 根据心率进行调整
        int level = getHearRateLevel(user);

        if (level >= 4) kcal = kcal * 1.2f;
        this.lastReport4Cadence = now;

        user.setKcal(user.getKcal() + kcal);
        user.setDistance(user.getDistance() + perimeter * cadence * seconds / interval4Cadence);
        user.setHrLevelDuration(seconds, level);
    }

    /**
     * 团操大卡计算：
     * 1. 体重 X 时速 X 0.45 X 时长（小时）
     * 2. 如果心率在有氧区域，加一点系数
     * <p>
     * 此处算法基准是 50 公斤，练一小时，心率达到三档，消耗 315 大卡
     **/
    private void getKcal4Group(FitUser user, LocalDateTime now) {
        int level = getHearRateLevel(user);
        if (level <= 0) return;

        float radio = 1f;
        if (level == 2) radio = 1.5f;
        if (level > 2) radio = radio + level - 2;

        // 获取上次上报和当前上报的时间差（秒）
        float seconds = Seconds.secondsBetween(lastReport4HeartRate, now).getSeconds();
//        if (seconds < 1) seconds = lastReport4HeartRate;  TODO
        if (seconds > 120) seconds = 120; // 上报时间差了两分钟，即按两分钟算

        if (seconds > 0) this.lastReport4HeartRate = now;
        float kcal = user.getWeight() * 2.1f * seconds / 3600f * radio;

        user.setKcal(user.getKcal() + kcal);
        user.setHrLevelDuration(seconds, level);
    }

    /**
     * 返回心率五档位置
     **/
    private int getHearRateLevel(FitUser user) {
        if (heartRate <= 10) return -1;
        if (heartRate <= 30) return 3;

        int max = user.getMaxHeartRate();
        int min = user.getHRrest();
        if (heartRate <= user.getHRrest()) return 1;
        if (heartRate >= max) return 5;

        float level = (max - min) / 5;
        return (int) Math.ceil((heartRate - min) / level);
    }

    /**
     * 根据单车轮子周长，踏频和时间，获得速度
     **/
    private float calSpeed(float perimeter) {
        return perimeter * cadence / interval4Cadence * 3.6f;
    }

    public void cal4Cycle(float perimeter, FitUser user, LocalDateTime now) {
        this.cadence = user.getCadence();
        this.interval4Cadence = user.getInterval4Cadence();
        this.heartRate = user.getHeartRate();

        getKcal4Cycle(user, perimeter, now);
    }

    public void cal4Group(int heartRate, FitUser user, LocalDateTime now) {
        this.heartRate = heartRate;

//        user.setHeartRate(heartRate);
        this.getKcal4Group(user, now);
    }
}
