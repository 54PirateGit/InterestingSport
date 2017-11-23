package com.tianbao.mi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tianbao.mi.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.graphics.Bitmap.createBitmap;

/**
 * 生成二维码图片
 * Created by edianzu on 2017/11/17.
 */
public class QrUtil {

    /**
     * 根据指定内容生成自定义宽高的二维码图片
     * <p>
     * param logoBm
     * logo图标
     * param content
     * 需要生成二维码的内容
     * param width
     * 二维码宽度
     * param height
     * 二维码高度
     * throws WriterException
     * 生成二维码异常
     */
    public static Bitmap makeQRImage(Context context, String content, int QR_WIDTH, int QR_HEIGHT) throws WriterException {
        try {
            // 图像数据转换，使用了矩阵转换
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// 容错率
            hints.put(EncodeHintType.MARGIN, 2); // default is 4
            hints.put(EncodeHintType.MAX_SIZE, 350);
            hints.put(EncodeHintType.MIN_SIZE, 100);
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                // 下面这里按照二维码的算法，逐个生成二维码的图片，//两个for循环是图片横列扫描的结果
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        if (x < QR_WIDTH / 2 && y < QR_HEIGHT / 2) {
//                            pixels[y * QR_WIDTH + x] = 0xff000000;
//                            pixels[y * QR_WIDTH + x] = 0xFF0616F1;// 蓝色
                            pixels[y * QR_WIDTH + x] = 0xFF0000FF;
                        } else if (x < QR_WIDTH / 2 && y > QR_HEIGHT / 2) {
//                            pixels[y * QR_WIDTH + x] = 0xFFF2CF04;// 黄色
                            pixels[y * QR_WIDTH + x] = 0xFF00FFFF;
                        } else if (x > QR_WIDTH / 2 && y > QR_HEIGHT / 2) {
//                            pixels[y * QR_WIDTH + x] = 0xFF5ACF00;// 绿色
                            pixels[y * QR_WIDTH + x] = 0xFFFF00FF;
                        } else {
//                            pixels[y * QR_WIDTH + x] = 0xFFF2CF04;// 黑色
                            pixels[y * QR_WIDTH + x] = 0xFF5ACF00;
                        }
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            // ------------------添加图片部分------------------ //
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            // 设置像素点
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

            // 添加 logo
            Bitmap logoBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.qr_logo);
            // 获取图片宽高
            int logoWidth = logoBmp.getWidth();
            int logoHeight = logoBmp.getHeight();

            if (QR_WIDTH == 0 || QR_HEIGHT == 0) return null;
            if (logoWidth == 0 || logoHeight == 0) return bitmap;
            // 图片绘制在二维码中央，合成二维码图片
            // logo 大小为二维码整体大小的 1/2
            float scaleFactor = QR_WIDTH * 1.0f / 5 / logoWidth;
            try {
                Canvas canvas = new Canvas(bitmap);
                canvas.drawBitmap(bitmap, 0, 0, null);
                canvas.scale(scaleFactor, scaleFactor, QR_WIDTH / 2, QR_HEIGHT / 2);
                canvas.drawBitmap(logoBmp, (QR_WIDTH - logoWidth) / 2, (QR_HEIGHT - logoHeight) / 2, null);
                canvas.save(Canvas.ALL_SAVE_FLAG);
                canvas.restore();
                return bitmap;
            } catch (Exception e) {
                e.getStackTrace();
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 生成二维码
    public static Bitmap generateBitmap(Context context, String content, int width, int height, boolean logo) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 1); //default is 4
            hints.put(EncodeHintType.MAX_SIZE, 350);
            hints.put(EncodeHintType.MIN_SIZE, 100);
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

//            encode = deleteWhite(encode);
//            width = encode.getWidth();
//            height = encode.getHeight();

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (encode.get(x, y)) {
//                        pixels[i * width + j] = 0x00000000;

                        if (x < width / 2 && y < height / 2) {
                            pixels[y * width + x] = 0xFF0094FF;// 蓝色
//                            Integer.toHexString(new Random().nextInt());
                        } else if (x < width / 2 && y > height / 2) {
                            pixels[y * width + x] = 0xFFFED545;// 黄色
                        } else if (x > width / 2 && y > height / 2) {
                            pixels[y * width + x] = 0xFF5ACF00;// 绿色
                        } else {
                            pixels[y * width + x] = 0xFFFED545;// 黑色
                        }
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
            if (logo) {
                Bitmap logoBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.qr_logo);
                bitmap = addLogo(bitmap, logoBm);
            }

            // 添加背景图片
//            Bitmap backBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.qr_background);
//            bitmap = addBackground(bitmap, backBitmap);

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */
    public static boolean createQRImage(String content, int widthPix, int heightPix, Bitmap logoBm, String filePath) {
        try {
            if (content == null || "".equals(content)) {
                return false;
            }

            // 配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个 for 循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = createBitmap(widthPix, heightPix, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }

            // 必须使用 compress 方法将 bitmap 保存到文件中再进行读取。直接返回的 bitmap 是没有任何压缩的，内存消耗巨大！
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 在二维码中间添加 Logo 图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) return null;
        if (logo == null) return src;

        // 获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) return null;
        if (logoWidth == 0 || logoHeight == 0) return src;

        // logo 大小为二维码整体大小的 1/5
        float scaleFactor = srcWidth * 0.8f / 5 / logoWidth;
        Bitmap bitmap = createBitmap(srcWidth, srcHeight, Bitmap.Config.RGB_565);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }

    /**
     * 给二维码图片加背景
     */
    public static Bitmap addBackground(Bitmap foreground, Bitmap background) {
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2, (bgHeight - fgHeight) * 3 / 5 + 70, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newmap;
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 5; i < resWidth - 5; i++) {
            for (int j = 5; j < resHeight - 5; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }
}
