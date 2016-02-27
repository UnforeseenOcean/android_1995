package fi.harism.android.intro.gl;

import android.opengl.GLES30;

import java.nio.Buffer;

public class GlBuffer {

    private final int[] buffer = {0};

    public GlBuffer() {
        GLES30.glGenBuffers(1, buffer, 0);
        GlUtils.checkGLErrors();
    }

    public GlBuffer bind(int target) {
        GLES30.glBindBuffer(target, buffer[0]);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlBuffer unbind(int target) {
        GLES30.glBindBuffer(target, 0);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlBuffer data(int target, int sizeInBytes, Buffer data, int usage) {
        GLES30.glBufferData(target, sizeInBytes, data, usage);
        GlUtils.checkGLErrors();
        return this;
    }

}
