package fi.harism.android.intro.util;

import android.os.Looper;

public class GlRenderThread extends Thread {

    private static final String TAG = "GlRenderThread";
    private static int INSTANCE_COUNTER = 0;

    private final Object mLock = new Object();
    private boolean mLockValue;
    private GlRenderHandler mGlRenderHandler;

    public GlRenderThread() {
        super(TAG + " #" + ++INSTANCE_COUNTER);
    }

    public void startAndWaitUntilReady() {
        synchronized (mLock) {
            start();
            boolean lockValue = mLockValue;
            while (lockValue == mLockValue) {
                try {
                    mLock.wait();
                } catch (InterruptedException ex) {
                }
            }

        }
    }

    public void stopAndWaitUntilReady() {
        synchronized (mLock) {
            mGlRenderHandler.postRelease();
            boolean lockValue = mLockValue;
            while (lockValue == mLockValue) {
                try {
                    mLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public GlRenderHandler getGlRenderHandler() {
        return mGlRenderHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mGlRenderHandler = new GlRenderHandler();
        synchronized (mLock) {
            mLockValue = !mLockValue;
            mLock.notifyAll();
        }
        Looper.loop();
        synchronized (mLock) {
            mLockValue = !mLockValue;
            mLock.notifyAll();
        }
    }

}
