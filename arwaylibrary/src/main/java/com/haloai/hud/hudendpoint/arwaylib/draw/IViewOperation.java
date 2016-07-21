package com.haloai.hud.hudendpoint.arwaylib.draw;

import android.content.Context;
import android.view.View;

/**
 * Created by wangshengxing on 16/7/15.
 */
public interface IViewOperation {

    /**
     * 动态增加相关的view
     * @param context
     * @return
     */
    public View getViewInstance(Context context);

    /**
     * 传入父布局，可根据父布局获取子view
     * @param view
     */
    public void setView(Context context, View view);

}
