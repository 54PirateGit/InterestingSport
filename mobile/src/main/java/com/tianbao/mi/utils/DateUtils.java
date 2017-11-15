package com.tianbao.mi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间、日期
 * Created by edianzu on 2017/10/30.
 */
public class DateUtils {

    private DateUtils() {

    }

    /**
     * 按指定格式返回时间
     * @param time 时间
     * @return 格式之后的时间
     */
    public static String timeFormat(String time) {
        String format = time;
        if (format.contains("-")) {
            format = format.replace("-", "  至  ");
        }

        if (format.contains(".")) {
            format = format.replace(".", ":");
        }

        return format;
    }

    /**
     * date 类型转换为 String 类型
     *
     * @param formatType formatType 格式为 yyyy-MM-dd HH:mm:ss // yyyy年MM月dd日 HH时mm分ss秒
     * @param data       data Date 类型的时间
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType, Locale.getDefault()).format(data);
    }

    /**
     * long 类型转换为 String 类型
     *
     * @param currentTime 要转换的 long 类型的时间
     * @param formatType  要转换的 string 类型的时间格式
     * @throws ParseException 异常
     */
    public static String longToString(long currentTime, String formatType) throws ParseException {
        Date date = longToDate(currentTime, formatType); // long 类型转成 Date 类型
        return dateToString(date, formatType);// date 类型转成 String
    }

    /**
     * string 类型转换为 date 类型
     *
     * @param strTime    要转换的 string 类型的时间
     * @param formatType 要转换的格式 yyyy-MM-dd HH:mm:ss // yyyy年MM月dd日
     *                   strTime 的时间格式必须要与 formatType 的时间格式相同
     */
    public static Date stringToDate(String strTime, String formatType) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType, Locale.getDefault());
        return formatter.parse(strTime);
    }

    /**
     * long 转换为 Date 类型
     *
     * @param currentTime 要转换的 long 类型的时间
     * @param formatType  要转换的时间格式 yyyy-MM-dd HH:mm:ss // yyyy年MM月dd日 HH时mm分ss秒
     */
    public static Date longToDate(long currentTime, String formatType) throws ParseException {
        Date dateOld = new Date(currentTime); // 根据 long 类型的毫秒数生命一个 date 类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把 date 类型的时间转换为 string
        return stringToDate(sDateTime, formatType);// 把 String 类型转换为 Date 类型
    }

    /**
     * String 类型转换为 long 类型
     *
     * @param strTime    要转换的 String 类型的时间
     * @param formatType 时间格式
     *                   strTime 的时间格式和 formatType 的时间格式必须相同
     */
    public static long stringToLong(String strTime, String formatType) throws ParseException {
        Date date = stringToDate(strTime, formatType); // String 类型转成 date 类型
        if (date == null) {
            return 0;
        } else {
            return dateToLong(date);// date 类型转成 long 类型
        }
    }

    /**
     * date 类型转换为 long 类型
     *
     * @param date 要转换的 date 类型的时间
     */
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static long dateToLong() throws ParseException {
        Calendar c = Calendar.getInstance();//
        int hour = c.get(Calendar.HOUR_OF_DAY);//时
        int minute = c.get(Calendar.MINUTE);//分
        String time = hour + "." + minute;
        return stringToLong(time, "HH.mm");
    }
}
