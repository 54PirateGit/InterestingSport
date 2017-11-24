package com.tianbao.mi.broad;

import android.content.Context;
import android.content.Intent;

import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.constant.ConfigConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.L;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

import static com.tianbao.mi.constant.ConfigConstant.BUILD_UPDATE;
import static com.tianbao.mi.constant.ConfigConstant.END_COURSE;
import static com.tianbao.mi.constant.ConfigConstant.START_COURSE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_BINDING_UPDATE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_END_COURSE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_START_COURSE;

/**
 * 接收推送消息
 * 10/30
 */
public class MiMessageReceiver extends PushMessageReceiver {

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);

        String title = miPushMessage.getTitle();// 标题

        L.v("MiMessageReceiver", "title -> " + title);

        switch (title) {
            case BUILD_UPDATE:// 有新的用户绑定关系
                String content = miPushMessage.getContent();
                L.v("MiMessageReceiver", "content -> " + content);

                Intent intentBuild = new Intent(BROAD_BINDING_UPDATE);
                intentBuild.putExtra(StringConstant.BUILD_UPDATE_KEY, content);
                MyApp.getContext().sendBroadcast(intentBuild);
                break;
            case START_COURSE:// 课程开始了
                Intent intentStart = new Intent(BROAD_START_COURSE);
                MyApp.getContext().sendBroadcast(intentStart);
                break;
            case END_COURSE:// 课程结束了
                Intent intentEnd = new Intent(BROAD_END_COURSE);
                MyApp.getContext().sendBroadcast(intentEnd);
                break;
        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                L.i("register success");

                String msg = message.toString();

                List<String> regId = message.getCommandArguments();
                if (regId != null && regId.size() > 0) {
                    ConfigConstant.REG_ID = regId.get(0);
                }

                L.i("register -> " + msg);

            } else {
                L.w("register fail");
            }
        }
    }
}
