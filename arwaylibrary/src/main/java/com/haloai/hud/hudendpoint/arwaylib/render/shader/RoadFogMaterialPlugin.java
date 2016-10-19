package com.haloai.hud.hudendpoint.arwaylib.render.shader;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.vector.Vector3;

/**
 * Created by wangshengxing on 16/10/15.
 */
public class RoadFogMaterialPlugin implements IMaterialPlugin {
    private RoadFogVertexShaderFragment mVertexShader;
    private RoadFogFragmentShaderFragment mFragmentShader;

    public RoadFogMaterialPlugin() {
        mVertexShader = new RoadFogVertexShaderFragment();
        mFragmentShader = new RoadFogFragmentShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_LIGHTING;
    }

    @Override
    public IShaderFragment getVertexShaderFragment() {
        return mVertexShader;
    }

    @Override
    public IShaderFragment getFragmentShaderFragment() {
        return mFragmentShader;
    }

    @Override
    public void bindTextures(int nextIndex) {
       /* mVertexShader.applyParams();
        mFragmentShader.applyParams();*/
    }
    @Override
    public void unbindTextures() {}

    public void setFogEndPosition(Vector3 position) {
        mVertexShader.setFogEndPosition(position);
    }

    public void setFogStartPosition(Vector3 position) {
        mVertexShader.setFogStartPosition(position);
    }

    private class RoadFogVertexShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "ROAD_FOG_VERTEX";

        private final String U_START_POS = "uStartPosition";
        private final String U_END_POS   = "uEndPosition";
        private final String V_DIST_FOG  = "vDistFog";

        private RVec3  muStartPosition;
        private RVec3  muEndPosition;
        private RFloat mvDistFog;

        private RVec3 mgPosition;



        private int muStartPositionHandle, muEndPositionHandle, mvDistFogHandle;

        private float[] mStartPosition;
        private float[] mEndPosition;

        public RoadFogVertexShaderFragment() {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            initialize();
            mStartPosition = new float[]{0,0,0,0};
            mEndPosition = new float[]{0,1,0,0};
        }

        @Override
        public void initialize()
        {
            super.initialize();
            muStartPosition = (RVec3) addUniform(U_START_POS, DataType.VEC3);
            muEndPosition = (RVec3) addUniform(U_END_POS, DataType.VEC3);
            mvDistFog = (RFloat) addVarying(V_DIST_FOG, DataType.FLOAT);

        }
        @Override
        public void main() {

//            float fogFactor = distance(aPosition,uEndPosition)/distance(uStartPosition,uEndPosition);
//            vDistFog = clamp(fogFactor,0,1);
            mgPosition = (RVec4) addGlobal(DefaultShaderVar.G_POSITION);
            RFloat fogFactor = new RFloat("fogFactor");
//            fogFactor.assign(length(mgPosition.subtract(muEndPosition)).divide(length(muEndPosition.subtract(muStartPosition))));
            fogFactor.assign(distance(mgPosition.xyz(),muEndPosition).divide(distance(muEndPosition,muStartPosition)));
            mvDistFog.assign(clamp(fogFactor,0,1));
//            fogFactor.assign(fogFactor.multiply(fogFactor).multiply(fogFactor));
//            mvDistFog.assign(0.5f);
        }

        @Override
        public void setLocations(int programHandle) {
            muEndPositionHandle = getUniformLocation(programHandle, U_END_POS);
            muStartPositionHandle = getUniformLocation(programHandle, U_START_POS);
        }

        @Override
        public void applyParams() {
            super.applyParams();

//            GLES20.glUniform3fv(muCameraPositionHandle, 1, mCameraPosition, 0);
            GLES20.glUniform3fv(muStartPositionHandle, 1, mStartPosition, 0);
            GLES20.glUniform3fv(muEndPositionHandle, 1, mEndPosition, 0);
        }



        public void setFogStartPosition(Vector3 pos)
        {
            float[] p = mStartPosition;
            p[0] = (float)pos.x;
            p[1] = (float)pos.y;
            p[2] = (float)pos.z;
        }

        public void setFogEndPosition(Vector3 pos)
        {
            float[] p = mEndPosition;
            p[0] = (float)pos.x;
            p[1] = (float)pos.y;
            p[2] = (float)pos.z;
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.IGNORE;
        }

        @Override
        public void bindTextures(int nextIndex) {}

        @Override
        public void unbindTextures() {}
    }
    private class RoadFogFragmentShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID  = "ROAD_FOG_VERTEX";
        private final       String V_DIST_FOG = "vDistFog";
        private RFloat mvDistFog;

        public RoadFogFragmentShaderFragment()
        {
            super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
            initialize();
        }

        @Override
        public String getShaderId() {
            return SHADER_ID;
        }

        @Override
        public void initialize()
        {
            super.initialize();
            mvDistFog = (RFloat) addVarying(V_DIST_FOG, DataType.FLOAT);
        }

        @Override
        public void main() {
            RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            gColor.assignMultiply(mvDistFog.multiply(mvDistFog).multiply(mvDistFog));
        }

        @Override
        public Material.PluginInsertLocation getInsertLocation() {
            return Material.PluginInsertLocation.IGNORE;
        }

        @Override
        public void bindTextures(int nextIndex) {}

        @Override
        public void unbindTextures() {}
    }
}
