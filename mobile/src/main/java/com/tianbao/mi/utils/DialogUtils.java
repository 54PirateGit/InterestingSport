package com.tianbao.mi.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.tianbao.mi.R;


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
}
