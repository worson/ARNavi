package demo.opengl.haloai.com.rajawalidemo;

import android.os.Bundle;
import android.view.MotionEvent;

import rajawali.RajawaliActivity;

public class DemoActivity extends RajawaliActivity {
    private DemoRenderer7 mDemoRenderer = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //创建一个渲染器
        //为渲染器关联一个GLSurfaceView(RajawaliActivity中定义的)
        mDemoRenderer = new DemoRenderer7(this);
        mDemoRenderer.setSurfaceView(mSurfaceView);
        //设置渲染器
        setRenderer(mDemoRenderer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDemoRenderer.onSurfaceDestroyed();
    }

    int count = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(count++%2==0){
                mDemoRenderer.pause();
            }else{
                mDemoRenderer.continue_();
            }
        }
        return true;
    }
}
