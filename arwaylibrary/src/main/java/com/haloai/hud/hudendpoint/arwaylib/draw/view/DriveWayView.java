//package com.haloai.hud.hudendpoint.arwaylib.draw.view;
//
///**
// * Created by wangshengxing on 13/11/2016.
// */
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//
//import com.amap.api.navi.AMapNaviView;
//
//
//public class DriveWayView extends ImageView {
//    private int          driveWayWidth        = 0;
//    private int          driveWayHeight       = 0;
//    private int          driveWaySize         = 0;
//    private int[]        driveWayBackgroundId = new int[]{2130837538, 2130837539, 2130837540, 2130837541, 2130837542, 2130837543, 2130837544, 2130837545, 2130837546, 2130837547, 2130837548, 2130837549, 2130837550, 2130837551, 2130837552};
//    private int[]        driveWayForegroundId = new int[]{2130837553, 2130837554, 2130837540, 2130837557, 2130837542, 2130837560, 2130837544, 2130837545, 2130837566, 2130837547, 2130837548, 2130837549, 2130837550, 2130837575, 2130837552};
//    private AMapNaviView mAMapNaviView        = null;
//    private Bitmap[]     driveWayBitMaps      = null;
//    private Bitmap[]     driveWayBitMapBgs    = null;
//    private int height;
//    private int width;
//
//    public DriveWayView(Context var1) {
//        super(var1);
//    }
//
//    public DriveWayView(Context var1, AttributeSet var2) {
//        super(var1, var2);
//    }
//
//    public DriveWayView(Context var1, AttributeSet var2, int var3) {
//        super(var1, var2, var3);
//    }
//
//    public int getDriveWayWidth() {
//        return this.driveWayWidth;
//    }
//
//    public void setAMapNaviView(AMapNaviView var1) {
//        this.mAMapNaviView = var1;
//    }
//
//    public void loadDriveWayBitmap(byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
//        this.driveWaySize = this.parseDriveWaySize(laneBackgroundInfo);
//        if(this.driveWaySize != 0) {
//            this.driveWayBitMapBgs = new Bitmap[this.driveWaySize];
//            this.driveWayBitMaps = new Bitmap[this.driveWaySize];
//
//            int i;
//            for(i = 0; i < this.driveWaySize; ++i) {
//                this.driveWayBitMapBgs[i] = BitmapFactory.decodeResource(ee.a(), this.driveWayBackgroundId[laneBackgroundInfo[i]]);
//                if(this.isComplexLane(laneBackgroundInfo[i])) {
//                    this.driveWayBitMaps[i] = this.complexBitmap(laneBackgroundInfo[i], laneRecommendedInfo[i]);
//                } else if(this.isThisLaneRecommended(laneRecommendedInfo[i])) {
//                    this.driveWayBitMaps[i] = BitmapFactory.decodeResource(ee.a(), this.driveWayForegroundId[laneRecommendedInfo[i]]);
//                } else {
//                    this.driveWayBitMaps[i] = this.driveWayBitMapBgs[i];
//                }
//            }
//
//            if(this.driveWayBitMapBgs[i - 1] != null) {
//                this.driveWayWidth = this.driveWayBitMapBgs[i - 1].getWidth();
//                this.driveWayHeight = this.driveWayBitMapBgs[i - 1].getHeight();
//            }
//
//            this.height = this.driveWayHeight;
//            this.width = this.driveWayWidth * this.driveWaySize;
//            this.setImageBitmap(this.produceFinalBitmap());
//        }
//    }
//
//    protected void onSizeChanged(int var1, int var2, int var3, int var4) {
//        super.onSizeChanged(var1, var2, var3, var4);
//    }
//
//    private int parseDriveWaySize(byte[] laneInfos) {
//        int var2 = laneInfos.length;
//        int var3 = 0;
//
//        for(int i = 0; i < var2; ++i) {
//            if(laneInfos[i] == 15) {
//                var3 = i;
//                break;
//            }
//        }
//
//        return var3;
//    }
//
//    private boolean isThisLaneRecommended(byte var1) {
//        return var1 != 15;
//    }
//
//    private boolean isComplexLane(int var1) {
//        return var1 == 14 || var1 == 2 || var1 == 4 || var1 == 9 || var1 == 10 || var1 == 11 || var1 == 12 || var1 == 6 || var1 == 7;
//    }
//
//    private Bitmap complexBitmap(int laneInfo, int laneBackgroundInfo) {
//        Bitmap bitmap = null;
//        if(laneInfo == 10) {
//            if(laneBackgroundInfo == 0) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837569);
//            } else if(laneBackgroundInfo == 8) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837570);
//            }
//        } else if(laneInfo == 9) {
//            if(laneBackgroundInfo == 0) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837567);
//            } else if(laneBackgroundInfo == 5) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837568);
//            }
//        } else if(laneInfo == 2) {
//            if(laneBackgroundInfo == 0) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837555);
//            } else if(laneBackgroundInfo == 1) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837556);
//            }
//        } else if(laneInfo == 4) {
//            if(laneBackgroundInfo == 0) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837558);
//            } else if(laneBackgroundInfo == 3) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837559);
//            }
//        } else if(laneInfo == 6) {
//            if(laneBackgroundInfo == 1) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837561);
//            } else if(laneBackgroundInfo == 3) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837562);
//            }
//        } else if(laneInfo == 7) {
//            if(laneBackgroundInfo == 0) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837563);
//            } else if(laneBackgroundInfo == 1) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837564);
//            } else if(laneBackgroundInfo == 3) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837565);
//            }
//        } else if(laneInfo == 11) {
//            if(laneBackgroundInfo == 5) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837572);
//            } else if(laneBackgroundInfo == 1) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837571);
//            }
//        } else if(laneInfo == 12) {
//            if(laneBackgroundInfo == 8) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837574);
//            } else if(laneBackgroundInfo == 3) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837573);
//            }
//        } else if(laneInfo == 14) {
//            if(laneBackgroundInfo == 1) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837576);
//            } else if(laneBackgroundInfo == 5) {
//                bitmap = BitmapFactory.decodeResource(ee.a(), 2130837577);
//            }
//        }
//
//        if(bitmap == null) {
//            bitmap = BitmapFactory.decodeResource(ee.a(), this.driveWayBackgroundId[laneInfo]);
//        }
//
//        return bitmap;
//    }
//
//    public void setDefaultTopMargin(int var1) {
//        if(this.mAMapNaviView != null) {
//            boolean var2 = false;
//            byte var3 = 10;
//            int var5;
//            if(this.mAMapNaviView.isOrientationLandscape()) {
//                var5 = var1 + var3;
//            } else if(this.mAMapNaviView.isShowRoadEnlarge()) {
//                var5 = var1 * 3 / 8 - (this.driveWayHeight >> 1);
//            } else {
//                var5 = var1 + var3;
//            }
//
//            RelativeLayout.LayoutParams var4 = (RelativeLayout.LayoutParams)this.getLayoutParams();
//            var4.setMargins(0, var5, 0, 0);
//            this.setLayoutParams(var4);
//        }
//    }
//
//    public void recycleResource() {
//        for(int var1 = 0; var1 < this.driveWaySize; ++var1) {
//            if(this.driveWayBitMaps[var1] != null) {
//                this.driveWayBitMaps[var1].recycle();
//                this.driveWayBitMaps[var1] = null;
//            }
//
//            if(this.driveWayBitMapBgs[var1] != null) {
//                this.driveWayBitMapBgs[var1].recycle();
//                this.driveWayBitMapBgs[var1] = null;
//            }
//        }
//
//        this.driveWaySize = 0;
//    }
//
//    Bitmap produceFinalBitmap() {
//        Bitmap bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//
//        for(int i = 0; i < this.driveWaySize; ++i) {
//            if(this.driveWayBitMaps[i] != null) {
//                canvas.drawBitmap(this.driveWayBitMaps[i], (float)(i * this.driveWayWidth), 0.0F, (Paint)null);
//            }
//        }
//
//        return bitmap;
//    }
//
//    public int getDriveWaySize() {
//        return this.driveWaySize;
//    }
//
//    public int getDriveWayBgHeight() {
//        return this.driveWayHeight;
//    }
//}
