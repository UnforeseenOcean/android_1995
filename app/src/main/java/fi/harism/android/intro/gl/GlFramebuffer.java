package fi.harism.android.intro.gl;

import android.opengl.GLES30;

public class GlFramebuffer {

    private final int[] framebuffer = {0};

    public GlFramebuffer() {
        GLES30.glGenFramebuffers(1, framebuffer, 0);
        GlUtils.checkGLErrors();
    }

    public GlFramebuffer bind(int target) {
        GLES30.glBindFramebuffer(target, framebuffer[0]);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlFramebuffer unbind(int target) {
        GLES30.glBindFramebuffer(target, 0);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlFramebuffer renderbuffer(int target, int attachment, int renderbuffer) {
        GLES30.glFramebufferRenderbuffer(target, attachment, GLES30.GL_RENDERBUFFER, renderbuffer);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlFramebuffer texture2D(int target, int attachment, int textarget, int texture, int level) {
        GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlFramebuffer drawBuffers(int... buffers) {
        GLES30.glDrawBuffers(buffers.length, buffers, 0);
        GlUtils.checkGLErrors();
        return this;
    }

    public GlFramebuffer readBuffer(int buffer) {
        GLES30.glReadBuffer(buffer);
        GlUtils.checkGLErrors();
        return this;
    }

    public int checkStatus(int target) {
        GlUtils.checkGLErrors();
        return GLES30.glCheckFramebufferStatus(target);
    }

    public int name() {
        return framebuffer[0];
    }

}
