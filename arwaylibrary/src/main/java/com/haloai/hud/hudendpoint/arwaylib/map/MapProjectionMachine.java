package com.haloai.hud.hudendpoint.arwaylib.map;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/9/3.
 */
public class MapProjectionMachine {
    public interface UpdateMapViewCall{
        public void updateMapView();
    }

    public interface ProjectionOkCall{
        public void projectionOk();
    }

    public enum Operation{
        MAP_LOADED,
        UPDATE_PATH,
        MAP_SCALED
    }

    //sensive switch
    public boolean mNeedUpdatePath           = false; //判断需要更新到render中去,更新path后自动关闭，开始导航、偏航时开启
    public boolean mForceUpdateNaviView4Path = false; //更新地图样式比例可转换oepngl点和屏幕点，需要更新path时打开开关，更新后则关闭
    public boolean mIsMapLoaded              = false; //地图未加载成功,成功后值不再改变

    private UpdateMapViewCall mUpdateMapViewCall = null;
    private ProjectionOkCall mProjectionOkCall = null;

    public void init(UpdateMapViewCall updateMapViewCall,ProjectionOkCall projectionOkCall){
        mUpdateMapViewCall = updateMapViewCall;
        mProjectionOkCall = projectionOkCall;
    }

    public void work(Operation operation) {
        if (operation == null) {
            return;
        }
        switch (operation){
            case MAP_LOADED:
                work(mMaploadState);
                break;
            case UPDATE_PATH:
                work(mUpdatePathState);
                break;
            case MAP_SCALED:
                work(mMapScaledState);
                break;
            default:
                break;
        }
    }

    private void work(MapProjectionState state) {
        state.handle(this);
    }

    public void updateContext(){
        if(mNeedUpdatePath){//是最新的路径，需要更新到render中去
            if(!mIsMapLoaded){ //地图未加载成功，压根等地图加载成功后，重新调用
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updatePath 地图未加载成功，正在等待...");
            }else {//更新地图样式比例可转换oepngl点和屏幕点
                mForceUpdateNaviView4Path = true;
                mUpdateMapViewCall.updateMapView();
            }

        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updatePath 路径不需要更新");
        }
    }

    public boolean isNeedUpdatePath() {
        return mNeedUpdatePath;
    }

    public void setNeedUpdatePath(boolean needUpdatePath) {
        mNeedUpdatePath = needUpdatePath;
    }

    public UpdateMapViewCall getUpdateMapViewCall() {
        return mUpdateMapViewCall;
    }

    public ProjectionOkCall getProjectionOkCall() {
        return mProjectionOkCall;
    }

    public boolean isForceUpdateNaviView4Path() {
        return mForceUpdateNaviView4Path;
    }

    public void setForceUpdateNaviView4Path(boolean forceUpdateNaviView4Path) {
        mForceUpdateNaviView4Path = forceUpdateNaviView4Path;
    }

    public boolean isMapLoaded() {
        return mIsMapLoaded;
    }

    public void setMapLoaded(boolean mapLoaded) {
        mIsMapLoaded = mapLoaded;
    }

    private MapProjectionState mMaploadState    = new MapProjectionState() {
        @Override
        public void handle(MapProjectionMachine machine) {
            if(!machine.mIsMapLoaded){
                machine.mIsMapLoaded =true;
            }
            if(machine.mNeedUpdatePath ) {
                machine.updateContext();
            }
            machine.getUpdateMapViewCall().updateMapView();
        }
    };
    private MapProjectionState mUpdatePathState = new MapProjectionState() {
        @Override
        public void handle(MapProjectionMachine machine) {
            machine.updateContext();
        }
    };

    private MapProjectionState mMapScaledState = new MapProjectionState() {
        @Override
        public void handle(MapProjectionMachine machine) {
            if(machine.mNeedUpdatePath && machine.mForceUpdateNaviView4Path){
                machine.mForceUpdateNaviView4Path = false;
                machine.getProjectionOkCall().projectionOk();
            }
            machine.getUpdateMapViewCall().updateMapView();
        }
    };
}
