package com.haloai.hud.hudendpoint.arwaylib.draw;

/**
 * Created by wangshengxing on 16/7/18.
 */
public interface IDriveStateLister {
    public enum DriveState{
        DRIVING,
        PAUSE
    }
    public void changeDriveState(DriveState state);
}
