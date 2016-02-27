package fi.harism.android.intro.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import fi.harism.android.intro.util.GlRenderThread;
import fi.harism.android.intro.util.GlRenderer;

public class GlTextureView extends TextureView {

    private static final String TAG = GlTextureView.class.getSimpleName();
    private final GlRenderThread mGlRenderThread;

    public GlTextureView(Context context) {
        this(context, null, 0, 0);
    }

    public GlTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public GlTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GlTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGlRenderThread = new GlRenderThread();
        mGlRenderThread.startAndWaitUntilReady();
        setSurfaceTextureListener(new GlSurfaceTextureListener());
    }

    public void setEglContext(int eglVersion, int eglFlags) {
        mGlRenderThread.getGlRenderHandler().postCreateEglContext(eglVersion, eglFlags);
    }

    public void setGlRenderer(GlRenderer glRenderer) {
        mGlRenderThread.getGlRenderHandler().postSetRenderer(glRenderer);
    }

    public void renderFrame() {
        if (isAvailable()) {
            mGlRenderThread.getGlRenderHandler().postRenderFrame();
        }
    }

    public void renderFrame(long frameTimeNanos) {
        if (isAvailable()) {
            mGlRenderThread.getGlRenderHandler().postRenderFrame(frameTimeNanos);
        }
    }

    public void onDestroy() {
        mGlRenderThread.stopAndWaitUntilReady();
    }

    private class GlSurfaceTextureListener implements SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture, int width, int height) {
            mGlRenderThread.getGlRenderHandler().postCreateEglSurface(surfaceTexture);
            mGlRenderThread.getGlRenderHandler().postResizeEglSurface(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            mGlRenderThread.getGlRenderHandler().postResizeEglSurface(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            mGlRenderThread.getGlRenderHandler().postReleaseEglSurfaceAndWait();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Do nothing
        }
    }

}
