package fi.harism.android.intro.gl;

import android.opengl.GLES30;

public class GlSampler {

    private final int sampler[] = {0};

    public GlSampler() {
        GLES30.glGenSamplers(1, sampler, 0);
        GlUtils.checkGLErrors();
    }

    public GlSampler parameter(int pname, float pvalue) {
        GLES30.glSamplerParameterf(sampler[0], pname, pvalue);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlSampler parameter(int pname, int pvalue) {
        GLES30.glSamplerParameteri(sampler[0], pname, pvalue);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlSampler bind(int textureUnit) {
        GLES30.glBindSampler(textureUnit, sampler[0]);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlSampler unbind(int textureUnit) {
        GLES30.glBindSampler(textureUnit, 0);
        GlUtils.checkGLErrors();
        return this;
    }

}
