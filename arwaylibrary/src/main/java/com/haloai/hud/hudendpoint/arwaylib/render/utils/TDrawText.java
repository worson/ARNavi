package com.haloai.hud.hudendpoint.arwaylib.render.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;


/**
 * 文字纹理绘制
 * Created by joycejie on 2015/4/8.
 */
public class TDrawText {
    private final static boolean DEBUG = false;
    public final static Paint pt = new Paint();
    public static int[] buffer = null;

    public synchronized static int[] drawText(String mstr, int iFontSize, int iFontStyle, int[] iParam, int txtrgb,
                                              int srgb, int colorbg, int iHaloWidth) {
        if(DEBUG) {
            TNLogUtil.d("drawText mstr:" + mstr + " iFontSize:" + iFontSize + " iFontStyle:" + iFontStyle + " txtrgb:" + txtrgb + " srgb:" + srgb + " colorbg:" + colorbg + " iHaloWidth:" + iHaloWidth);
        }
        int iWordHeight = 0;
        int iWordWidth = 0;
        pt.reset();
        pt.setSubpixelText(true);
        pt.setAntiAlias(true);
        pt.setTextSize(iFontSize);
        // 设置为无衬线体
        pt.setTypeface(Typeface.SANS_SERIF);

        // 单行
        FontMetrics fm = pt.getFontMetrics();
        iWordWidth = (int) pt.measureText(mstr);
        iWordHeight = (int) Math.ceil(fm.descent - fm.ascent);
        iParam[0] = iWordWidth;
        iParam[1] = iWordHeight;
        iParam[2] = iWordWidth;
        iParam[3] = iWordHeight;

        Canvas canvas = GLBitmapUtil.lockCanvas(iWordWidth, iWordHeight);
        if (iHaloWidth != 0) {
            pt.setStrokeWidth(iHaloWidth);
            // 尽量减少描边交接处像素
            pt.setStrokeCap(Paint.Cap.BUTT);
            pt.setStrokeJoin(Paint.Join.BEVEL);
            pt.setStyle(Paint.Style.STROKE);
            pt.setColor(srgb);
            // 设置透明度为90
            pt.setAlpha(230);
            canvas.drawText(mstr, 0, 0 - fm.ascent, pt); //注意Drawtext的第二个参数为基准线(baseline),
        }
        pt.setStrokeCap(Paint.Cap.ROUND);
        pt.setStrokeJoin(Paint.Join.ROUND);
        pt.setStyle(Paint.Style.FILL);
        pt.setColor(txtrgb);
        pt.setAlpha(255);
        canvas.drawText(mstr, 0, 0 - fm.ascent, pt);
        buffer = new int[iWordWidth * iWordHeight];
        Bitmap bmp = GLBitmapUtil.getLockedBitmap();
        bmp.getPixels(buffer, 0, iWordWidth, 0, 0, iWordWidth, iWordHeight);
        GLBitmapUtil.unlockCanvas();
        colorInvertPixels(buffer);
//        TDrawIcon.savePixels(mstr, buffer, iWordWidth, iWordHeight);
        return buffer;
    }

    public static int[] calcTextSize(String mstr, int iFontSize) {
        if(DEBUG) {
            TNLogUtil.d("calcTextSize mstr:" + mstr + " iFontSize:" + iFontSize);
        }
        int iLen = mstr.length();
        if (iLen == 0)
            return null;
        Paint pt = new Paint();
        pt.setSubpixelText(true);
        pt.setAntiAlias(true);
        pt.setTextSize(iFontSize);
        pt.setTypeface(Typeface.SANS_SERIF);
        // 单行
        FontMetrics fm = pt.getFontMetrics();
        int iWordWidth = (int) pt.measureText(mstr);
        int iWordHight = (int) Math.ceil(fm.descent - fm.ascent);
//        int level = (int) Math.ceil(Math.log(iWordWidth) / Math.log(2));
//        iWordWidth = (int) Math.pow(2, level);
//        level = (int) Math.ceil(Math.log(iWordHight) / Math.log(2));
//        iWordHight = (int) Math.pow(2, level);
        int[] iParam = new int[2];
        iParam[0] = iWordWidth;
        iParam[1] = iWordHight;
        return iParam;
    }

    private static void colorInvertPixels(int[] pixels) {
        for(int index=0;index<pixels.length;index++) {
            int A = (pixels[index] >> 24) & 0xff;
            double a = (A * 1.0) /255;
            int R = (int)Math.ceil(((pixels[index] >> 16) & 0xff) * a);
            int G = (int)Math.ceil(((pixels[index] >> 8) & 0xff) * a );
            int B = (int)Math.ceil((pixels[index] & 0xff) * a );
            pixels[index] = (A << 24) | (R << 16) | (G << 8) | B;
        }
    }

    private static int colorInvertPixel(int pixel) {
//        int A = (pixel >> 24) & 0xff;
//        int R = (pixel >> 16) & 0xff;
//        int G = (pixel >> 8) & 0xff;
//        int B = pixel & 0xff;
//        pixel = (A << 24) | (B << 16) | (G << 8) | R;
        return pixel;
    }
}

