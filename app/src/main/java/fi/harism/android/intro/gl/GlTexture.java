package fi.harism.android.intro.gl;

import android.opengl.GLES30;

import java.nio.Buffer;

public class GlTexture {

    private final int[] texture = {0};

    public GlTexture() {
        GLES30.glGenTextures(1, texture, 0);
        GlUtils.checkGLErrors();
    }

    public int name() {
        return texture[0];
    }

    public GlTexture bind(int target) {
        GLES30.glBindTexture(target, texture[0]);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlTexture unbind(int target) {
        GLES30.glBindTexture(target, 0);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlTexture texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data) {
        GLES30.glTexImage2D(target, level, internalFormat, width, height, border, format, type, data);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlTexture parameter(int target, int pname, float pvalue) {
        GLES30.glTexParameterf(target, pname, pvalue);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlTexture parameter(int target, int pname, int pvalue) {
        GLES30.glTexParameteri(target, pname, pvalue);
        GlUtils.checkGLErrors();
        return this;
    }

}
