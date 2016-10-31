package com.haloai.hud.hudendpoint.arwaylib.render.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Utility providing shared bitmap and canvas objects among different textures.
 */
public class GLBitmapUtil {

    /**
     * permanent bitmap size of small
     */
    private static final int SMALL_SIZE = 64;

    /**
     * permanent bitmap size of middle
     */
    private static final int MIDDLE_SIZE = 128;

    /**
     * permanent bitmap size of big
     */
    private static final int BIG_SIZE = 256;

    /**
     * shared canvas
     */
    private static Canvas drawTextureCanvas;

    /**
     * current using bitmap
     */
    private static int sUsingWhichBmp;

    /**
     * fixed sized bitmap shared mostly, 64 is default
     */
    private static Bitmap bitmap64, bitmap128, bitmap256;
    static {
        bitmap64 = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        bitmap128 = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        bitmap256 = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        drawTextureCanvas = new Canvas(bitmap64);
        sUsingWhichBmp = SMALL_SIZE;
    }

    /**
     * short life cycle bitmap with bigger size than common ones, created on
     * demand and recycled in-time
     */
    private static Bitmap tempBitmap = null;

    /**
     * Take the using privilege of the shared canvas. Call this to get the
     * shared canvas every time needed.
     * 
     * @param width
     *            the width of bitmap needed
     * @param height
     *            the height of bitmap needed
     * @return the shared canvas
     */
    public static Canvas lockCanvas(float width, float height) {
        if (width <= SMALL_SIZE && height <= SMALL_SIZE) {
            sUsingWhichBmp = SMALL_SIZE;
            drawTextureCanvas.setBitmap(bitmap64);
            bitmap64.eraseColor(0x00000000);
            return drawTextureCanvas;
        }

        if (width <= MIDDLE_SIZE && height <= MIDDLE_SIZE) {
            sUsingWhichBmp = MIDDLE_SIZE;
            drawTextureCanvas.setBitmap(bitmap128);
            bitmap128.eraseColor(0x00000000);
            return drawTextureCanvas;
        }

        if (width <= BIG_SIZE && height <= BIG_SIZE) {
            sUsingWhichBmp = BIG_SIZE;
            drawTextureCanvas.setBitmap(bitmap256);
            bitmap256.eraseColor(0x00000000);
            return drawTextureCanvas;
        }

        sUsingWhichBmp = 0;
        int targetWid = 1;
        int targetHei = 1;
        while (targetWid < width) {
            targetWid <<= 1;
        }
        while (targetHei < height) {
            targetHei <<= 1;
        }
        tempBitmap = Bitmap.createBitmap(targetWid, targetHei, Bitmap.Config.ARGB_8888);
        drawTextureCanvas.setBitmap(tempBitmap);
        tempBitmap.eraseColor(0x00000000);
        return drawTextureCanvas;
    }

    public static void measureBitmapSize(float width, float height, Point out) {
        if (width <= SMALL_SIZE && height <= SMALL_SIZE) {
            out.set(SMALL_SIZE, SMALL_SIZE);
            return;
        }
        if (width <= MIDDLE_SIZE && height <= MIDDLE_SIZE) {
            out.set(MIDDLE_SIZE, MIDDLE_SIZE);
            return;
        }
        if (width <= BIG_SIZE && height <= BIG_SIZE) {
            out.set(BIG_SIZE, BIG_SIZE);
            return;
        }
        int targetWid = 1;
        int targetHei = 1;
        while (targetWid < width) {
            targetWid <<= 1;
        }
        while (targetHei < height) {
            targetHei <<= 1;
        }
        out.set(targetWid, targetHei);
    }

    /**
     * Give up the using privilege of the shared canvas. Call this as soon as
     * drawing and texture loading is finished.
     */
    public static void unlockCanvas() {
        // if (sUsingWhichBmp == 64) {
        // return;
        // } else {
        // drawTextureCanvas.setBitmap(bitmap64);
        // }
        // 只对非标准常驻尺寸的位图进行回收
        if (tempBitmap != null) {
            tempBitmap.recycle();
            tempBitmap = null;
        }
    }

    /**
     * Get the bitmap under the locked canvas.
     * 
     * @return
     */
    public static Bitmap getLockedBitmap() {
        switch (sUsingWhichBmp) {
            case SMALL_SIZE:
                return bitmap64;
            case MIDDLE_SIZE:
                return bitmap128;
            case BIG_SIZE:
                return bitmap256;
            default:
                return tempBitmap;
        }
    }
}
