package com.haloai.hud.hudendpoint.arwaylib.render.shader;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

/**
 * Created by wangshengxing on 16/10/15.
 */
public class TextureAlphaMaterialPlugin implements IMaterialPlugin {
    private RoadFogVertexShaderFragment mVertexShader;
    private RoadFogFragmentShaderFragment mFragmentShader;

    public TextureAlphaMaterialPlugin() {
        mVertexShader = new RoadFogVertexShaderFragment();
        mFragmentShader = new RoadFogFragmentShaderFragment();
    }

    @Override
    public Material.PluginInsertLocation getInsertLocation() {
        return Material.PluginInsertLocation.PRE_ALPHA;
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
    }
    @Override
    public void unbindTextures() {}


    private class RoadFogVertexShaderFragment extends AShader implements IShaderFragment {
        public final static String SHADER_ID = "TEXTURE_ALPHA_VERTEX";


        public RoadFogVertexShaderFragment() {
            super(ShaderType.VERTEX_SHADER_FRAGMENT);
            initialize();

        }

        @Override
        public void initialize()
        {
            super.initialize();

        }
        @Override
        public void main() {
        }

        @Override
        public void setLocations(int programHandle) {

        }

        @Override
        public void applyParams() {
            super.applyParams();

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
        public final static String SHADER_ID  = "TEXTURE_ALPHA_VERTEX";



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

        }

        @Override
        public void main() {
            RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
            gColor.a().assign(new RVec4("texColor").a());
            gColor.rgb().assign(new RVec4("vColor").rgb());
            gColor.rgb().multiply(new RFloat("uColorInfluence"));
        }

        @Override
        public void setLocations(int programHandle) {
            super.setLocations(programHandle);

        }

        @Override
        public void applyParams() {
            super.applyParams();

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
