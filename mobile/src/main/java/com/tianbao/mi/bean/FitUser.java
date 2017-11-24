package com.tianbao.mi.bean;

import android.text.TextUtils;

import com.tianbao.mi.constant.StringConstant;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * FitUser
 * Created by edianzu on 2017/11/20.
 */
public class FitUser implements Serializable {

    private FitUser() {

    }

    private static final DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");

    FitInfo fitInfo = new FitInfo();

    /**
     * 用户绑定的设备
     */
    private String key;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户昵称
     */
    private String nick;

    /**
     * 性别，暂没用到
     **/
//    private boolean sex;
    private String sex;

    /**
     * openId
     */
    private String openId;

    /**
     * 用户 Id
     */
    private int userId;

    /**
     * 是否掉线
     */
    private boolean isNotOnline;

    /**
     * 最后获取数据的时间
     */
    private long lastTime;

    /**
     * 排名
     */
    private int sort;

    /**
     * 年龄
     **/
    private float age;

    /**
     * 体重
     **/
    private float weight;

    /**
     * 高度，厘米
     **/
    private int height;

    /**
     * 安静心率
     **/
    private int HRrest;

    /**
     * 踏频
     */
    private float cadence;

    /**
     * 踏频间隔时间
     */
    private float interval4Cadence;

    /**
     * 总消耗卡路里
     **/
    private float kcal;

    /**
     * 锻炼时长（秒）
     **/
    private LocalDateTime start = new LocalDateTime();

    /**
     * 最佳配速 m/KM
     **/
    private float PACE;

    /**
     * 距离，米
     **/
    private float distance;

    /**
     * 心率档位 1 所占时长，单位秒
     **/
    private float hrLevel1duration;

    /**
     * 心率档位 2 所占时长，单位秒
     **/
    private float hrLevel2duration;

    /**
     * 心率档位 3 所占时长，单位秒
     **/
    private float hrLevel3duration;

    /**
     * 心率档位 4 所占时长，单位秒
     **/
    private float hrLevel4duration;

    /**
     * 心率档位 5 所占时长，单位秒
     **/
    private float hrLevel5duration;

    /**
     * 速度
     **/
    private float speed;

    /**
     * 当前心率
     **/
    private int heartRate;

    /**
     * 提示次数
     */
    private int count = 0;

    /**
     * 单车 uuid
     */
    private String bUUID;

    /**
     * 心率设备 uuid
     */
    private String hUUID;

    public String getbUUID() {
        return bUUID;
    }

    public void setbUUID(String bUUID) {
        this.bUUID = bUUID;
    }

    public String gethUUID() {
        return hUUID;
    }

    public void sethUUID(String hUUID) {
        this.hUUID = hUUID;
    }

    public String getbSupplierId() {
        return bSupplierId;
    }

    public void setbSupplierId(String bSupplierId) {
        this.bSupplierId = bSupplierId;
    }

    public String gethSupplierId() {
        return hSupplierId;
    }

    public void sethSupplierId(String hSupplierId) {
        this.hSupplierId = hSupplierId;
    }

    /**
     * 单车厂商 id
     */
    private String bSupplierId;

    /**
     * 心率设备厂商 id
     */
    private String hSupplierId;

    public FitInfo getFitInfo() {
        return fitInfo;
    }

    public FitUser setSex(String sex) {
        this.sex = sex;

        return this;
    }

    /**
     * 设置年纪
     **/
    public FitUser setAge(String birth, LocalDateTime now) {
        // 设置年龄
        if (birth != null && !"".equals(birth) && now != null) {
            String[] arr = birth.split(" ");
            birth = arr[0];

            // 时间解析
            LocalDateTime birthday = DateTime.parse(birth, format).toLocalDateTime();

            int days = Days.daysBetween(birthday, now).getDays();

            this.age = days / 365;
        } else {
            this.age = 24f;
        }
        return this;
    }

    /**
     * 用户排名
     */
    public FitUser setSort(int sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 获取用户排名
     */
    public int getSort() {
        return this.sort;
    }

    /**
     * 设置 key
     */
    public FitUser setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * 设置用户头像
     */
    public FitUser setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    /**
     * 获取用户头像
     */
    public String getAvatar() {
        return this.avatar;
    }

    /**
     * 设置用户昵称
     */
    public FitUser setNick(String nick) {
        this.nick = nick;
        return this;
    }

    /**
     * 获取用户昵称
     */
    public String getNick(boolean isTip) {
        if (isTip && count < 3) {
            count++;
        }
        if (isTip && count >= 3) return StringConstant.NO_LONGER_PROMPTED;
        else return this.nick;
    }

    /**
     * openId
     */
    public FitUser setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    /**
     * 获取 openId
     */
    public String getOpenId() {
        return this.openId;
    }

    /**
     * userId
     */
    public FitUser setUserId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            userId = "0";
        }
        this.userId = Integer.valueOf(userId);
        return this;
    }

    /**
     * userId
     */
    public FitUser setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    /**
     * 获取 userId
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * lastTime  最后修改时间
     */
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * 获取最后修改时间
     */
    public long getLastTime() {
        return this.lastTime;
    }

    /**
     * isNotOnline 是否不在线
     */
    public void setIsNotOnline(boolean isNotOnline) {
        this.isNotOnline = isNotOnline;
    }

    /**
     * 获取用户是否已经掉线
     */
    public boolean isNotOnline() {
        return this.isNotOnline;
    }

    /**
     * 踏频
     */
    public FitUser setCadence(String cadence) {
        if (TextUtils.isEmpty(cadence)) {
            cadence = "0";
        }
        this.cadence = Float.valueOf(cadence) / 100;
        return this;
    }

    /**
     * 获取踏频
     */
    public float getCadence() {
        return this.cadence;
    }

    /**
     * 踏频间隔时间
     */
    public FitUser setInterval4Cadence(String interval4Cadence) {
        if (TextUtils.isEmpty(interval4Cadence)) {
            interval4Cadence = "0";
        }
        this.interval4Cadence = Float.valueOf(interval4Cadence);
        return this;
    }

    /**
     * 获取踏频间隔时间
     */
    public float getInterval4Cadence() {
        return this.interval4Cadence;
    }

    public float getWeight() {
        return weight;
    }

    public FitUser setWeight(String weight) {
        float w;
        if (TextUtils.isEmpty(weight)) {
            weight = "0";
        }
        w = Float.valueOf(weight);
        // 设置体重
        if (w < 20) {
            this.weight = 50;
        } else {
            this.weight = w;
        }

        return this;
    }

    public FitUser setHeight(String height) {
        if (TextUtils.isEmpty(height)) {
            height = "0";
        }
        this.height = Integer.valueOf(height);
        return this;
    }

    public int getHRrest() {
        return HRrest;
    }

    /**
     * 设备安静心率
     **/
    public FitUser setHRrest(String HRrest) {
        if (TextUtils.isEmpty(HRrest)) {
            HRrest = "75";
        }

        this.HRrest = Integer.valueOf(HRrest);

        // 怀疑是设备坏了，专业运动员可能会很低，但太低就不管了
        if (this.HRrest <= 40) {
            this.HRrest = 40;
        }
        return this;
    }

    public int getMaxHeartRate() {
        // 最大心率 = 220 - 实际年龄
        if (age < 18f) return 210;
        if (age > 50f) return 160;

        return 220 - (int) age;
    }

    public float getKcal() {
        return kcal;
    }

    public String getKcalStr() {
        return format(1, kcal);
    }

    public void setKcal(float kcal) {
        this.kcal = kcal;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public String getPACEStr() {
        return format(1, PACE);
    }

    public float getPACE() {
        return this.PACE;
    }

    private void setPACE() {
        float pace = 1f / speed * 60f;

        if (pace > this.PACE) this.PACE = pace;
    }

    public float getDistance() {
        return distance;
    }

    public String getDistanceStr() {
        return format(1, distance / 1000);
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getHrLevelRate(int level, LocalDateTime now) {
        float duration = Seconds.secondsBetween(start, now).getSeconds();

        int l1 = (int) (this.hrLevel1duration / duration * 100);
        int l2 = (int) (this.hrLevel2duration / duration * 100);
        int l3 = (int) (this.hrLevel3duration / duration * 100);
        int l4 = (int) (this.hrLevel4duration / duration * 100);
        int l5 = (int) (this.hrLevel5duration / duration * 100);

        int total = l1 + l2 + l3 + l4 + l5;
        int[] arr = new int[]{l1, l2, l3, l4, l5};

        // 这里简单的把百分比弄成 100，要不会少了数字
        if (total != 100) {
            for (int j = 4; j >= 0; j--) {
                if (arr[j] > 0) {
                    arr[j] = arr[j] + 100 - total;
                    break;
                }
            }
        }

        switch (level) {
            case 1:
                return arr[0];
            case 2:
                return arr[1];
            case 3:
                return arr[2];
            case 4:
                return arr[3];
            case 5:
                return arr[4];
            default:
                return -1;
        }
    }

    public void setHrLevelDuration(float duration, int level) {
        switch (level) {
            case 1:
                this.hrLevel1duration = this.hrLevel1duration + duration;
                break;
            case 2:
                this.hrLevel2duration = this.hrLevel2duration + duration;
                break;
            case 3:
                this.hrLevel3duration = this.hrLevel3duration + duration;
                break;
            case 4:
                this.hrLevel4duration = this.hrLevel4duration + duration;
                break;
            case 5:
                this.hrLevel5duration = this.hrLevel5duration + duration;
                break;
        }
    }

    public float getHrLevel1duration() {
        return hrLevel1duration;
    }

    public float getHrLevel2duration() {
        return hrLevel2duration;
    }

    public float getHrLevel3duration() {
        return hrLevel3duration;
    }

    public float getHrLevel4duration() {
        return hrLevel4duration;
    }

    public float getHrLevel5duration() {
        return hrLevel5duration;
    }

    public String getSpeed() {
        return format(1, speed);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        setPACE();
    }

    public int getHeartRate() {
        return heartRate;
    }

    public String getHeartRateStr() {
        return format(1, heartRate);
    }

    public FitUser setHeartRate(String heartRate) {
        if (TextUtils.isEmpty(heartRate)) {

        } else {
            this.heartRate = Integer.valueOf(heartRate);
        }
        return this;
    }

    /**
     * 返回心率五档位置
     **/
    public int getHearRateLevel() {
        if (heartRate == 0) return -1;

        if (heartRate <= 30) return 3;
        if (heartRate <= HRrest) return 1;

        int maxHeartRate = getMaxHeartRate();
        if (heartRate >= maxHeartRate) return 5;

        int level = (maxHeartRate - HRrest) / 5;

        return (heartRate - HRrest) / level + 1;
    }

    public int getDuration(LocalDateTime now) {
        int seconds = Seconds.secondsBetween(this.start, now).getSeconds();
        return seconds / 60;
    }

    public int getAverageHeartRate(LocalDateTime now) {
        int maxHeartRate = getMaxHeartRate();
        int level = (maxHeartRate - HRrest) / 5;
        int total = 0;

        for (int i = 1; i < 6; i++) {
            int hrLevelRate = this.getHrLevelRate(i, now);

            total = total + hrLevelRate * ((i - 1) * level + level / 2);
        }

        return total / 100 + HRrest;
    }

    public static FitUser build() {
        return new FitUser();
    }

    public static String format(int maximumFraction, float data) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();

        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(maximumFraction);

        return numberFormat.format(data);
    }

//    public static void main(String[] args) throws InterruptedException {
//        FitUser user = FitUser.build().setAge("1980-2-7", new LocalDateTime()).setHeight(170).setSex(true).setWeight(60.1f).setHRrest(61);
//
//        user.testGroup(user);
//        user.testCycle(user);
//    }

    private void testGroup(FitUser user) throws InterruptedException {
        /** 注意 FitUser 不要被虚拟机给回收了，一堂课下来要握住 **/
        LocalDateTime start = new LocalDateTime();

        int heartRate = 61;
//        int heartRate = 0;
        for (int i = 0; i < 120; i++) {
            System.out.println("index:" + (i + 1));
            LocalDateTime now = new LocalDateTime();
            user.getFitInfo().cal4Group(heartRate, user, now);
            user.printLive(user, now);

            heartRate++;
            if (heartRate > 180) heartRate = 63;
            Thread.sleep(2000);
        }

        user.printShart(user, new LocalDateTime());

        LocalDateTime end = new LocalDateTime();

        System.out.println("耗时：" + Seconds.secondsBetween(start, end).getSeconds());
    }

    private void testCycle(FitUser user) throws InterruptedException {
        /** 注意 FitUser 不要被虚拟机给回收了，一堂课下来要握住 **/
        LocalDateTime start = new LocalDateTime();

        int heartRate = 61;
        for (int i = 0; i < 120; i++) {
            System.out.println("index:" + (i + 1));
            LocalDateTime now = new LocalDateTime();
            user.getFitInfo().cal4Cycle(2.1f, user, now);
            user.printLive(user, now);

            heartRate++;
            if (heartRate > 180) heartRate = 63;
            Thread.sleep(2000);
        }

        user.printShart(user, new LocalDateTime());

        LocalDateTime end = new LocalDateTime();

        System.out.println("耗时：" + Seconds.secondsBetween(start, end).getSeconds());
    }

    private void printShart(FitUser user, LocalDateTime now) {
        System.out.println("----------share----------");
        System.out.println("卡路里" + user.getKcalStr());
        System.out.println("时长" + user.getDuration(now));
        System.out.println("配速" + user.getPACEStr());
        System.out.println("距离" + user.getDistanceStr());
        System.out.println("第1档心率比例" + user.getHrLevelRate(1, now));
        System.out.println("第2档心率比例" + user.getHrLevelRate(2, now));
        System.out.println("第3档心率比例" + user.getHrLevelRate(3, now));
        System.out.println("第4档心率比例" + user.getHrLevelRate(4, now));
        System.out.println("第5档心率比例" + user.getHrLevelRate(5, now));
        System.out.println("平均心率" + user.getAverageHeartRate(now));
    }

    private void printLive(FitUser user, LocalDateTime now) {
        System.out.println("----------live----------");
        System.out.println("心率：" + user.getHeartRate());
        System.out.println("心率档位：" + user.getHearRateLevel());
        System.out.println("速度：" + user.getSpeed());
        System.out.println("卡路里" + user.getKcalStr());
        System.out.println("第1档心率比例" + user.getHrLevelRate(1, now));
        System.out.println("第2档心率比例" + user.getHrLevelRate(2, now));
        System.out.println("第3档心率比例" + user.getHrLevelRate(3, now));
        System.out.println("第4档心率比例" + user.getHrLevelRate(4, now));
        System.out.println("第5档心率比例" + user.getHrLevelRate(5, now));

        System.out.println("----------end----------");
    }
}
