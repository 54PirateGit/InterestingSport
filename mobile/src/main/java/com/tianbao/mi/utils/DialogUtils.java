package com.tianbao.mi.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;


/**
 * Dialog
 * Created by edianzu on 2017/9/19.
 */

public class DialogUtils {

    /**
     * 加载数据
     */
    public static Dialog dialogLoading(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }

    /**
     * 提示
     */
    public static void showDialog(Activity context, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setNegativeButton("确定", (dialog, which) -> {
            context.finish();
            MyApp.appExit();
        });
        builder.show();
    }

    /**
     * 提示
     */
    public static void showDialogFinish(Activity context, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setNegativeButton("确定", (dialog, which) -> context.finish());
        builder.show();
    }
}
