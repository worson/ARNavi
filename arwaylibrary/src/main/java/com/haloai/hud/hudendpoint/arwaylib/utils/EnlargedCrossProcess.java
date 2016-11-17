package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LatLngOutSide;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LinkInfoOutside;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.Size2iOutside;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 15/6/2016.
 *         <p/>
 *         cd ./build/intermediates/classes/debug
 *         javah com.haloai.hud.hudendpoint.arwaylib.utils.EnlargedCrossProcess
 */
public class EnlargedCrossProcess {
    private static final String TAG                       = "HaloAI_ECP_Lib_Caller";
    private static final String ROAD_NET_SOURCE_FILE_PATH = /*"/sdcard/haloaimapdata_bj_noRDname.hmd"*/"/sdcard";
    private static final int CROSS_ROAD_LEN               = 2000;

    private Bitmap myCrossImage  = null;
    private Mat    matECImage    = new Mat();
    private Mat    myMatECImage  = new Mat();
    private Mat    bgrMatECImage = new Mat();

    public static class ECBranchLine {

        private List<Point> linePoints = new ArrayList<>();

        /*
         *
         * param-branchRoadPointsStr: 岔路格式为: "pt1.x,pt1.y,pt2.x,pt2.y,...ptN.x,ptN.y"
         */
        public ECBranchLine(String branchRoadPointsStr) {
            String[] strPointXYValues = branchRoadPointsStr.split(",");
            for (int i = 0; i < strPointXYValues.length - 1; i = i + 2) {
                Point pt = new Point();
                pt.x = Integer.valueOf(strPointXYValues[i]);
                pt.y = Integer.valueOf(strPointXYValues[i + 1]);

                linePoints.add(pt);
            }
        }

        public List<Point> getLinePoints() {
            return linePoints;
        }
    }

    public List<ECBranchLine> recognizeBranchInECImage(Bitmap amapECImage, int centerPointIndex, String[] mainRoadArray) {

        Utils.bitmapToMat(amapECImage, matECImage, true);
        Imgproc.cvtColor(matECImage, bgrMatECImage, Imgproc.COLOR_RGBA2BGR);
        String[] branchLinesStr = nativeGetBranchRoads(bgrMatECImage.getNativeObjAddr(), centerPointIndex, mainRoadArray);
        if (branchLinesStr == null)
            return null;

        List<ECBranchLine> listECBranchLines = new ArrayList<>();
        for (int i = 0; i < branchLinesStr.length; i++) {
            ECBranchLine branch = new ECBranchLine(branchLinesStr[i]);
            listECBranchLines.add(branch);
        }
        return listECBranchLines;
    }

    public Point getHopPointInCrossImage(Bitmap crossImage) {
        if (crossImage == null)
            return null;

        if (myCrossImage == null) {
            myCrossImage = Bitmap.createBitmap(crossImage.getWidth(), crossImage.getHeight(), Bitmap.Config.RGB_565);
        }

        Utils.bitmapToMat(crossImage, matECImage, true);
        Imgproc.cvtColor(matECImage, myMatECImage, Imgproc.COLOR_RGBA2RGB);
        double[] color = myMatECImage.get(200, 200);
        PointA resPt = new PointA();
        int res = nativeGetHopPointInCrossImage(myMatECImage.getNativeObjAddr(), resPt);
        if (res == 0) {
            Log.e(TAG, "the turn point is (" + resPt.x + "," + resPt.y + ")");
            return new Point(resPt.x, resPt.y);
        } else {
            Log.e(TAG, "Happen error in nativeGetFirstTurnPointInCrossImage");
        }
        return null;
    }

    /**
     * 调用JNI接口实现路网数据的获取
     *
     * @param links            机动点前后的主路(由N个link表示,对应linkInfo,但是目前该集合只有一个link,该link就是中心点前后的点的集合且保证头尾是边界点)
     * @param linkInfos        目前为null
     * @param centerPoint      机动点的经纬度
     * @param szCover          400*400的矩形
     * @param crossLinks       [out]:路网数据中的岔路部分
     * @param mainRoad         [out]:路网中的主路部分
     * @param crossPointIndexs [out]:主路中与岔路相交部分的角标,最后一个点为路网中导航路的中心点角标
     * @return 0:正常,数据可用 其他:错误
     */
    public int updateCrossLinks(List<List<LatLngOutSide>> links, List<LinkInfoOutside> linkInfos, LatLngOutSide centerPoint,
                                Size2iOutside szCover, List<List<LatLngOutSide>> crossLinks, List<LatLngOutSide> mainRoad,
                                List<Integer> crossPointIndexs) {
        return nativeGetCrossLinks(links, linkInfos, centerPoint, szCover, ROAD_NET_SOURCE_FILE_PATH, CROSS_ROAD_LEN, crossLinks, mainRoad, crossPointIndexs);
    }

    /**
     * 每次导航前调用该接口去清空JNI部分的路网数据缓存
     */
    public void clearJNIStatus(){
        nativeClearRoadNetStatus();
    }

    public static class PointA {
        public int x;
        public int y;
    }

    //Load the Enlarge Cross Image Processing JNI lib.
    static {
        System.loadLibrary("HaloECP");
        System.loadLibrary("opencv_java3");
    }

    /*
     * 传入路口放大图,主路屏幕点坐标集合,以及中心点所处下标,得到岔路集合
     * [in] bmpAMapCrossImage 路口放大图的long型表示方式
     * [in] centerPointIndex 中心点所在主路中的下标
     * [in] mainRoadEndpoint 主路的屏幕点坐标数组[x_1,y_1,x_2,y_2....x_n,y_n]
     * [out]岔路格式为: "pt1.x,pt1.y,pt2.x,pt2.y,...ptN.x,ptN.y"
     */
    public native String[] nativeGetBranchRoads(long bmpAMapCrossImage, int centerPointIndex, String[] mainRoadEndpoint);

    /*
     * 查找高德路口放大图中，从中心点(200,200)沿箭头方向，第一个发生角度跳变的像素位置
     * @param bmpAMapECImage 高德的路口放大图原图
     * @return 0 means succeed, non-zero means failed.
     */
    public native int nativeGetHopPointInCrossImage(long bmpAMapCrossImage, PointA resHopPoint);


    /**
     * 通过传入一段导航路获取该中心点附近的路网数据信息
     *
     * @param links       [in] 导航路切割成N个link(为了对应LinkInfo)
     * @param linkInfos   [in] 对应每个Link有一个对应的LinkInfo
     * @param centerPoint [in] 中心点的经纬度
     * @param szCover     覆盖区域的宽高
     * @param strDictPath 地图数据路径(目前写死在/sdcard/下)
     * @param crossRoadLen
     *@param crossLinks  [out] 中心点附近的路网信息  @return 1表示正常 0表示不正常
     */
    public native int nativeGetCrossLinks(List<List<LatLngOutSide>> links, List<LinkInfoOutside> linkInfos, LatLngOutSide centerPoint,
                                          Size2iOutside szCover, String strDictPath, int crossRoadLen, List<List<LatLngOutSide>> crossLinks, List<LatLngOutSide> mainRoad,
                                          List<Integer> crossPointIndexs);
    public native void nativeClearRoadNetStatus();
}
