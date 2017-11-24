package com.tianbao.mi.utils;

import android.content.Context;
import android.content.Intent;

import com.tianbao.mi.bean.FitUser;
import com.tianbao.mi.constant.IntentConstant;
import com.tianbao.mi.constant.ReceiverConstant;

import java.io.Serializable;
import java.util.List;

/**
 * 发送广播
 * Created by edianzu on 2017/11/24.
 */
public class SendBroadUtil {

    /**
     * 给服务发送播放声音广播
     */
    public static void sendPlayToService(Context context, int value) {
        Intent intent = new Intent();
        intent.setAction(ReceiverConstant.SERVICE_PLAY_SOUND);
        intent.putExtra(IntentConstant.SOUND_ID, value);
        context.sendBroadcast(intent);
    }

    /**
     * 给服务发送停止播放声音广播
     */
    public static void sendStopToService(Context context) {
        Intent intent = new Intent();
        intent.setAction(ReceiverConstant.SERVICE_STOP_SOUND);
        context.sendBroadcast(intent);
    }

    /**
     * 获取全部绑定关系
     */
    public static void sendAllBindingToService(Context context) {
        Intent intent = new Intent();
        intent.setAction(ReceiverConstant.BROAD_ALL_BINDING);
        context.sendBroadcast(intent);
    }

    /**
     * 数据更新
     */
    public static void sendUpdate(Context context, List<FitUser> list) {
        Intent intent = new Intent();
        intent.setAction(ReceiverConstant.BROAD_UPDATE_DATA);
        intent.putExtra(IntentConstant.USER_DATA, (Serializable) list);
        context.sendBroadcast(intent);
    }
}
