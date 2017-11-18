package com.tianbao.mi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 图片工具
 * 11/18
 */
public class BitmapUtils {

    /**
     * 以最省内存的方式读取本地资源的图片
     * Android中图片有四种属性，分别是：
     * ALPHA_8：每个像素占用1byte内存
     * ARGB_4444：每个像素占用2byte内存
     * ARGB_8888：每个像素占用4byte内存 （默认）
     * RGB_565：每个像素占用2byte内存
     * Android默认的颜色模式为ARGB_8888，这个颜色模式色彩最细腻，
     * 显示质量最高。但同样的，占用的内存也最大。
     * 所以在对图片效果不是特别高的情况下使用RGB_565（565没有透明度属性* @param context
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
        return compressImage(bitmap);
    }

    /**
     * 质量压缩
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里 100 表示不压缩，把压缩后的数据存放到 baos 中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于 100kb,大于继续压缩
            baos.reset();// 重置 baos 即清空 baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到 baos 中
            options -= 10;// 每次都减少 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据 baos 存放到 ByteArrayInputStream 中
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 图片按比例大小压缩
     */
    public static Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把 options.inJustDecodeBounds 设回 true 了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap;//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是 1920 * 1080 分辨率，所以高和宽我们设置为
        float hh = 1080f;// 这里设置高度为 1080f
        float ww = 1920f;// 这里设置宽度为 1920f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把 options.inJustDecodeBounds 设回 false 了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片按比例大小压缩方法（根据Bitmap图片压缩）
     */
    public static Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于 1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置 baos 即清空 baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩 50%，把压缩后的数据存放到 baos 中
        }
        ByteArrayInputStream isBm;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把 options.inJustDecodeBounds 设回 true 了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap;
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是 1920 * 1080 分辨率，所以高和宽我们设置为
        float hh = 1080f;// 这里设置高度为 1080f
        float ww = 1920f;// 这里设置宽度为 1920f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        // 重新读入图片，注意此时已经把 options.inJustDecodeBounds 设回 false 了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }
}
