package com.tianbao.mi.broad;

import android.content.Context;
import android.content.Intent;

import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.utils.L;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

/**
 * 接收推送消息
 * 10/30
 */
public class MiMessageReceiver extends PushMessageReceiver {

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);

        String title = miPushMessage.getTitle();// 标题
        switch (title) {
            case StringConstant.BUILD_UPDATE:// 有新的用户绑定关系
                String content = miPushMessage.getContent();
                L.v("MiMessageReceiver", "content -> " + content);

                Intent intentBuild = new Intent(StringConstant.BROAD_BUILD_UPDATE);
                intentBuild.putExtra(StringConstant.BUILD_UPDATE_KEY, content);
                MyApp.getContext().sendBroadcast(intentBuild);
                break;
            case StringConstant.START_COURSE:// 课程开始了
                L.v("MiMessageReceiver", "title -> " + title);

                Intent intentStart = new Intent(StringConstant.BROAD_START_COURSE);
                MyApp.getContext().sendBroadcast(intentStart);
                break;
            case StringConstant.END_COURSE:// 课程结束了
                L.v("MiMessageReceiver", "title -> " + title);

                Intent intentEnd = new Intent(StringConstant.BROAD_END_COURSE);
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
                    StringConstant.REG_ID = regId.get(0);
                }

                L.i("register -> " + msg);

            } else {
                L.w("register fail");
            }
        }
    }
}
