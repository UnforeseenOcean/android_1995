package fi.harism.android.intro.egl;

import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.util.Log;

public class EglSurface {

    private static final String TAG = "EglSurface";

    private EglCore eglCore;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;

    public EglSurface(EglCore eglCore, Object surface) {
        this.eglCore = eglCore;
        eglSurface = eglCore.createWindowSurface(surface);
    }

    public void release() {
        eglCore.releaseSurface(eglSurface);
        eglSurface = EGL14.EGL_NO_SURFACE;
    }

    @Override
    protected void finalize() throws Throwable {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            Log.e(TAG, "finalize called without release");
        }
        super.finalize();
    }

    public void makeCurrent() {
        eglCore.makeCurrent(eglSurface);
    }

    public boolean swapBuffers() {
        boolean result = eglCore.swapBuffers(eglSurface);
        if (!result) {
            Log.d(TAG, "swapBuffers failed");
        }
        return result;
    }

    public void setPresentationTime(long frameTimeNanos) {
        eglCore.setPresentationTime(eglSurface, frameTimeNanos);
    }

    public int getWidth() {
        return eglCore.querySurface(eglSurface, EGL14.EGL_WIDTH);
    }

    public int getHeight() {
        return eglCore.querySurface(eglSurface, EGL14.EGL_HEIGHT);
    }

}
