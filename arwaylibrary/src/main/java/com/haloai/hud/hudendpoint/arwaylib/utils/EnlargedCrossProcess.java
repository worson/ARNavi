//package com.haloai.hud.hudendpoint.arwaylib.utils;
//
//import android.graphics.Bitmap;
//import android.graphics.Point;
//import android.util.Log;
//
//import com.haloai.hud.utils.HaloLogger;
//
//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Created by Mo Bing(mobing@haloai.com) on 15/6/2016.
// *         <p/>
// *         cd ./build/intermediates/classes/debug
// *         javah com.haloai.hud.hudendpoint.arwaylib.utils.EnlargedCrossProcess
// */
//public class EnlargedCrossProcess {
//    static final String TAG           = "HaloAI_ECP_Lib_Caller";
//    private      Bitmap myCrossImage  = null;
//    private      Mat    matECImage    = new Mat();
//    private      Mat    myMatECImage  = new Mat();
//    private      Mat    bgrMatECImage = new Mat();
//
//    public static class ECBranchLine {
//
//        private List<Point> linePoints = new ArrayList<>();
//
//        /*
//         *
//         * param-branchRoadPointsStr: 岔路格式为: "pt1.x,pt1.y,pt2.x,pt2.y,...ptN.x,ptN.y"
//         */
//        public ECBranchLine(String branchRoadPointsStr) {
//            String[] strPointXYValues = branchRoadPointsStr.split(",");
//            for (int i = 0; i < strPointXYValues.length - 1; i = i + 2) {
//                Point pt = new Point();
//                pt.x = Integer.valueOf(strPointXYValues[i]);
//                pt.y = Integer.valueOf(strPointXYValues[i + 1]);
//
//                linePoints.add(pt);
//            }
//        }
//
//        public List<Point> getLinePoints() {
//            return linePoints;
//        }
//    }
//
//    public List<ECBranchLine> recognizeBranchInECImage(Bitmap amapECImage, int centerPointIndex, String[] mainRoadArray) {
//
//        Utils.bitmapToMat(amapECImage, matECImage, true);
//        Imgproc.cvtColor(matECImage, bgrMatECImage, Imgproc.COLOR_RGBA2BGR);
//        String[] branchLinesStr = nativeGetBranchRoads(bgrMatECImage.getNativeObjAddr(), centerPointIndex, mainRoadArray);
//        if (branchLinesStr == null)
//            return null;
//
//        List<ECBranchLine> listECBranchLines = new ArrayList<>();
//        for (int i = 0; i < branchLinesStr.length; i++) {
//            ECBranchLine branch = new ECBranchLine(branchLinesStr[i]);
//            listECBranchLines.add(branch);
//        }
//        return listECBranchLines;
//    }
//
//    public Point getHopPointInCrossImage(Bitmap crossImage) {
//        if (crossImage == null)
//            return null;
//
//        if (myCrossImage == null) {
//            myCrossImage = Bitmap.createBitmap(crossImage.getWidth(), crossImage.getHeight(), Bitmap.Config.RGB_565);
//        }
//
//        Utils.bitmapToMat(crossImage, matECImage, true);
//        Imgproc.cvtColor(matECImage, myMatECImage, Imgproc.COLOR_RGBA2RGB);
//        double[] color = myMatECImage.get(200, 200);
//        PointA resPt = new PointA();
//        int res = nativeGetHopPointInCrossImage(myMatECImage.getNativeObjAddr(), resPt);
//        if (res == 0) {
//            Log.e(TAG, "the turn point is (" + resPt.x + "," + resPt.y + ")");
//            return new Point(resPt.x, resPt.y);
//        } else {
//            Log.e(TAG, "Happen error in nativeGetFirstTurnPointInCrossImage");
//        }
//        return null;
//    }
//
//    static class PointA {
//        public int x;
//        public int y;
//    }
//
//    //Load the Enlarge Cross Image Processing JNI lib.
//    static {
//        System.loadLibrary("HaloECP");
//        System.loadLibrary("opencv_java3");
//    }
//
//    /*
//     * 传入路口放大图,主路屏幕点坐标集合,以及中心点所处下标,得到岔路集合
//     * [in] bmpAMapCrossImage 路口放大图的long型表示方式
//     * [in] centerPointIndex 中心点所在主路中的下标
//     * [in] mainRoadEndpoint 主路的屏幕点坐标数组[x_1,y_1,x_2,y_2....x_n,y_n]
//     * [out]岔路格式为: "pt1.x,pt1.y,pt2.x,pt2.y,...ptN.x,ptN.y"
//     */
//    public native String[] nativeGetBranchRoads(long bmpAMapCrossImage, int centerPointIndex, String[] mainRoadEndpoint);
//
//    /*
//     * 查找高德路口放大图中，从中心点(200,200)沿箭头方向，第一个发生角度跳变的像素位置
//     * @param bmpAMapECImage 高德的路口放大图原图
//     * @return 0 means succeed, non-zero means failed.
//     */
//    public native int nativeGetHopPointInCrossImage(long bmpAMapCrossImage, PointA resHopPoint);
//}
