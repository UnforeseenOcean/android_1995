package fi.harism.android.intro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.util.Log;
import android.util.SizeF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import fi.harism.android.intro.gl.GlFramebuffer;
import fi.harism.android.intro.gl.GlProgram;
import fi.harism.android.intro.gl.GlSampler;
import fi.harism.android.intro.gl.GlTexture;
import fi.harism.android.intro.gl.GlUtils;
import fi.harism.android.intro.util.GlRenderer;

public class MainGlRenderer implements GlRenderer {

    private static final String TAG = MainGlRenderer.class.getSimpleName();

    private static final int PARTICLE_COUNT = 64;

    private Context context;
    private ByteBuffer verticesQuad;
    private SizeF screenSize;
    private GlSampler glSamplerLinear;
    private GlSampler glSamplerNearest;
    private boolean useSwapBuffer;
    private float snowAlpha;

    private GlProgram glProgramBackground;
    private GlTexture glTextureBackground;
    private int vBackgroundInPosition;
    private int uBackgroundMatrix;
    private int sBackgroundSampler;
    private SizeF backgroundSize;
    private final Matrix backgroundMatrix = new Matrix();
    private final float backgroundMatrixValues[] = new float[9];
    private SizeF backgroundTransSource = new SizeF(0, 0);
    private SizeF backgroundTransTarget = new SizeF(0, 0);
    private long backgroundTransTime = -1;

    private GlFramebuffer glFramebufferSnowPosition;
    private GlFramebuffer glFramebufferSnowPositionSwap;
    private GlFramebuffer glFramebufferSnowVelocity;
    private GlFramebuffer glFramebufferSnowVelocitySwap;
    private GlTexture glTextureNoise;

    private GlProgram glProgramSnowEvaluate;
    private int vSnowEvaluateInPosition;
    private int sSnowEvaluatePositionSampler;
    private int sSnowEvaluateVelocitySampler;
    private int sSnowEvaluateNoiseSampler;

    private GlProgram glProgramSnowAdvance;
    private int vSnowAdvanceInPosition;
    private int sSnowAdvancePositionSampler;
    private int sSnowAdvanceVelocitySampler;

    private GlProgram glProgramSnowParticle;
    private GlTexture glTextureSnowPosition;
    private GlTexture glTextureSnowPositionSwap;
    private GlTexture glTextureSnowVelocity;
    private GlTexture glTextureSnowVelocitySwap;
    private int vSnowParticleInPosition;
    private int uSnowParticlePositionY;
    private int sSnowParticlePositionSampler;
    private int sSnowParticleVelocitySampler;
    private int uSnowParticlePositionScaleY;
    private int uSnowParticleAlpha;

    private GlProgram glProgramForeground;
    private int vForegroundInPosition;
    private int uForegroundBrightness;

    public MainGlRenderer(Context context) {
        this.context = context;
        final byte[] VERTICES = {-1, 1, -1, -1, 1, 1, 1, -1};
        verticesQuad = ByteBuffer.allocateDirect(VERTICES.length).order(ByteOrder.nativeOrder());
        verticesQuad.put(VERTICES).position(0);
    }

    public void setSnowAlpha(float snowAlpha) {
        this.snowAlpha = snowAlpha;
    }

    @Override
    public void onSurfaceCreated() {
        try {
            final String vertBackground = GlUtils.loadString(context, "background_vs.txt");
            final String fragBackground = GlUtils.loadString(context, "background_fs.txt");
            glProgramBackground = new GlProgram(vertBackground, fragBackground);
            vBackgroundInPosition = glProgramBackground.getAttribLocation("inPosition");
            uBackgroundMatrix = glProgramBackground.getUniformLocation("uBackgroundMatrix");
            sBackgroundSampler = glProgramBackground.getUniformLocation("sTextureBackground");

            final String vertSnowEvaluate = GlUtils.loadString(context, "snowevaluate_vs.txt");
            final String fragSnowEvaluate = GlUtils.loadString(context, "snowevaluate_fs.txt");
            glProgramSnowEvaluate = new GlProgram(vertSnowEvaluate, fragSnowEvaluate);
            vSnowEvaluateInPosition = glProgramSnowEvaluate.getAttribLocation("inPosition");
            sSnowEvaluatePositionSampler = glProgramSnowEvaluate.getUniformLocation("sTexturePosition");
            sSnowEvaluateVelocitySampler = glProgramSnowEvaluate.getUniformLocation("sTextureVelocity");
            sSnowEvaluateNoiseSampler = glProgramSnowEvaluate.getUniformLocation("sTextureNoise");

            final String vertSnowAdvance = GlUtils.loadString(context, "snowadvance_vs.txt");
            final String fragSnowAdvance = GlUtils.loadString(context, "snowadvance_fs.txt");
            glProgramSnowAdvance = new GlProgram(vertSnowAdvance, fragSnowAdvance);
            vSnowAdvanceInPosition = glProgramSnowAdvance.getAttribLocation("inPosition");
            sSnowAdvancePositionSampler = glProgramSnowAdvance.getUniformLocation("sTexturePosition");
            sSnowAdvanceVelocitySampler = glProgramSnowAdvance.getUniformLocation("sTextureVelocity");

            final String vertSnowParticle = GlUtils.loadString(context, "snowparticle_vs.txt");
            final String fragSnowParticle = GlUtils.loadString(context, "snowparticle_fs.txt");
            glProgramSnowParticle = new GlProgram(vertSnowParticle, fragSnowParticle);
            vSnowParticleInPosition = glProgramSnowParticle.getAttribLocation("inPosition");
            uSnowParticlePositionY = glProgramSnowParticle.getUniformLocation("uPositionY");
            sSnowParticlePositionSampler = glProgramSnowParticle.getUniformLocation("sTexturePosition");
            sSnowParticleVelocitySampler = glProgramSnowParticle.getUniformLocation("sTextureVelocity");
            uSnowParticlePositionScaleY = glProgramSnowParticle.getUniformLocation("uScaleY");
            uSnowParticleAlpha = glProgramSnowParticle.getUniformLocation("uParticleAlpha");

            final String vertForeground = GlUtils.loadString(context, "foreground_vs.txt");
            final String fragForeground = GlUtils.loadString(context, "foreground_fs.txt");
            glProgramForeground = new GlProgram(vertForeground, fragForeground);
            vForegroundInPosition = glProgramForeground.getAttribLocation("inPosition");
            uForegroundBrightness = glProgramForeground.getUniformLocation("uBrightness");
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            throw new RuntimeException(ex);
        }

        glSamplerLinear = new GlSampler()
                .parameter(GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
                .parameter(GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
                .parameter(GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
                .parameter(GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        glSamplerNearest = new GlSampler()
                .parameter(GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
                .parameter(GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)
                .parameter(GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
                .parameter(GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        glTextureBackground = new GlTexture();
        glTextureBackground.bind(GLES30.GL_TEXTURE_2D);
        Bitmap backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_main);
        backgroundSize = new SizeF(backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, backgroundBitmap, 0);
        glTextureBackground.unbind(GLES30.GL_TEXTURE_2D);

        FloatBuffer positionBuffer = ByteBuffer.allocateDirect(4 * 3 * PARTICLE_COUNT * PARTICLE_COUNT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int index = 0; index < 3 * PARTICLE_COUNT * PARTICLE_COUNT; ++index) {
            positionBuffer.put((float) Math.random() * 2.0f - 1.0f);
        }
        FloatBuffer velocityBuffer = ByteBuffer.allocateDirect(4 * 3 * PARTICLE_COUNT * PARTICLE_COUNT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int index = 0; index < 3 * PARTICLE_COUNT * PARTICLE_COUNT; ++index) {
            velocityBuffer.put(((float) Math.random() * 2.0f - 1.0f) * 0.01f);
        }
        FloatBuffer noiseBuffer = ByteBuffer.allocateDirect(4 * 2 * 32 * 32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int index = 0; index < 2 * 32 * 32; ++index) {
            noiseBuffer.put((float) Math.random() * 2.0f - 1.0f);
        }

        glTextureSnowPosition = new GlTexture();
        glTextureSnowPosition.bind(GLES30.GL_TEXTURE_2D);
        glTextureSnowPosition.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F, PARTICLE_COUNT, PARTICLE_COUNT, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, positionBuffer.position(0));
        glTextureSnowPosition.unbind(GLES30.GL_TEXTURE_2D);

        glTextureSnowPositionSwap = new GlTexture();
        glTextureSnowPositionSwap.bind(GLES30.GL_TEXTURE_2D);
        glTextureSnowPositionSwap.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F, PARTICLE_COUNT, PARTICLE_COUNT, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, positionBuffer.position(0));
        glTextureSnowPositionSwap.unbind(GLES30.GL_TEXTURE_2D);

        glTextureSnowVelocity = new GlTexture();
        glTextureSnowVelocity.bind(GLES30.GL_TEXTURE_2D);
        glTextureSnowVelocity.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F, PARTICLE_COUNT, PARTICLE_COUNT, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, velocityBuffer.position(0));
        glTextureSnowVelocity.unbind(GLES30.GL_TEXTURE_2D);

        glTextureSnowVelocitySwap = new GlTexture();
        glTextureSnowVelocitySwap.bind(GLES30.GL_TEXTURE_2D);
        glTextureSnowVelocitySwap.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F, PARTICLE_COUNT, PARTICLE_COUNT, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, velocityBuffer.position(0));
        glTextureSnowVelocitySwap.unbind(GLES30.GL_TEXTURE_2D);

        glTextureNoise = new GlTexture();
        glTextureNoise.bind(GLES30.GL_TEXTURE_2D);
        glTextureNoise.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RG16F, 32, 32, 0, GLES30.GL_RG, GLES30.GL_FLOAT, noiseBuffer.position(0));
        glTextureNoise.unbind(GLES30.GL_TEXTURE_2D);

        glFramebufferSnowPosition = new GlFramebuffer();
        glFramebufferSnowPosition.bind(GLES30.GL_DRAW_FRAMEBUFFER)
                .texture2D(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, glTextureSnowPosition.name(), 0)
                .drawBuffers(GLES30.GL_COLOR_ATTACHMENT0).unbind(GLES30.GL_DRAW_FRAMEBUFFER);

        glFramebufferSnowPositionSwap = new GlFramebuffer();
        glFramebufferSnowPositionSwap.bind(GLES30.GL_DRAW_FRAMEBUFFER)
                .texture2D(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, glTextureSnowPositionSwap.name(), 0)
                .drawBuffers(GLES30.GL_COLOR_ATTACHMENT0).unbind(GLES30.GL_DRAW_FRAMEBUFFER);

        glFramebufferSnowVelocity = new GlFramebuffer();
        glFramebufferSnowVelocity.bind(GLES30.GL_DRAW_FRAMEBUFFER)
                .texture2D(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, glTextureSnowVelocity.name(), 0)
                .drawBuffers(GLES30.GL_COLOR_ATTACHMENT0).unbind(GLES30.GL_DRAW_FRAMEBUFFER);

        glFramebufferSnowVelocitySwap = new GlFramebuffer();
        glFramebufferSnowVelocitySwap.bind(GLES30.GL_DRAW_FRAMEBUFFER)
                .texture2D(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, glTextureSnowVelocitySwap.name(), 0)
                .drawBuffers(GLES30.GL_COLOR_ATTACHMENT0).unbind(GLES30.GL_DRAW_FRAMEBUFFER);

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        screenSize = new SizeF(width, height);
    }

    @Override
    public void onRenderFrame() {
        renderBackground();
        renderSnowEvaluate(useSwapBuffer);
        renderSnowAdvance(useSwapBuffer);
        renderSnowParticles(useSwapBuffer);
        renderForeground();
        useSwapBuffer = !useSwapBuffer;
    }

    @Override
    public void onSurfaceReleased() {
    }

    private void renderBackground() {
        long time = SystemClock.uptimeMillis();
        if (backgroundTransTime < 0 || time - backgroundTransTime > 10000) {
            backgroundTransTime = time;
            backgroundTransSource = new SizeF(backgroundTransTarget.getWidth(), backgroundTransTarget.getHeight());
            backgroundTransTarget = new SizeF(((float) Math.random() - .5f) * .25f, ((float) Math.random() - .5f) * .25f);
        }

        float backgroundT = (time - backgroundTransTime) / 10000f;
        backgroundT = backgroundT * backgroundT * (3 - backgroundT * 2);
        float backgroundTransX = backgroundTransSource.getWidth() + (backgroundTransTarget.getWidth() - backgroundTransSource.getWidth()) * backgroundT;
        float backgroundTransY = backgroundTransSource.getHeight() + (backgroundTransTarget.getHeight() - backgroundTransSource.getHeight()) * backgroundT;

        float backgroundRotation = (time % 31415 * 2) / 10000f;
        backgroundRotation = (float) Math.sin(backgroundRotation) * 5f;

        float backgroundScaleY = (1f + ((screenSize.getHeight() - backgroundSize.getHeight()) * .5f) / screenSize.getHeight()) * 1.1f;
        float backgroundScaleX = backgroundScaleY * (backgroundSize.getWidth() / screenSize.getWidth());

        backgroundMatrix.setScale(backgroundScaleX, backgroundScaleY);
        backgroundMatrix.postRotate(backgroundRotation, 0f, 0f);
        backgroundMatrix.postTranslate(backgroundTransX, backgroundTransY);
        backgroundMatrix.getValues(backgroundMatrixValues);

        glProgramBackground.useProgram();
        GLES30.glUniform1i(sBackgroundSampler, 0);
        GLES30.glUniformMatrix3fv(uBackgroundMatrix, 1, false, backgroundMatrixValues, 0);
        GLES30.glVertexAttribPointer(vBackgroundInPosition, 2, GLES30.GL_BYTE, false, 0, verticesQuad);
        GLES30.glEnableVertexAttribArray(vBackgroundInPosition);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        glTextureBackground.bind(GLES30.GL_TEXTURE_2D);
        glSamplerLinear.bind(0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        glTextureBackground.unbind(GLES30.GL_TEXTURE_2D);
        glSamplerLinear.unbind(0);
        GLES30.glDisableVertexAttribArray(vBackgroundInPosition);
    }

    private void renderSnowEvaluate(boolean useSwapBuffer) {
        glProgramSnowEvaluate.useProgram();

        GLES30.glUniform1i(sSnowEvaluatePositionSampler, 0);
        GLES30.glUniform1i(sSnowEvaluateVelocitySampler, 1);
        GLES30.glUniform1i(sSnowEvaluateNoiseSampler, 2);

        GLES30.glViewport(0, 0, PARTICLE_COUNT, PARTICLE_COUNT);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        glTextureSnowPosition.bind(GLES30.GL_TEXTURE_2D);
        glSamplerNearest.bind(0);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        glTextureNoise.bind(GLES30.GL_TEXTURE_2D);
        glSamplerLinear.bind(2);

        if (useSwapBuffer) {
            glFramebufferSnowVelocitySwap.bind(GLES30.GL_DRAW_FRAMEBUFFER);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            glTextureSnowVelocity.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(1);
        } else {
            glFramebufferSnowVelocity.bind(GLES30.GL_DRAW_FRAMEBUFFER);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            glTextureSnowVelocitySwap.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(1);
        }

        GLES30.glVertexAttribPointer(vSnowEvaluateInPosition, 2, GLES30.GL_BYTE, false, 0, verticesQuad);
        GLES30.glEnableVertexAttribArray(vSnowEvaluateInPosition);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        glSamplerNearest.unbind(0);
        glSamplerNearest.unbind(1);
        glSamplerLinear.unbind(2);

        GLES30.glDisableVertexAttribArray(vSnowAdvanceInPosition);

        glTextureSnowPosition.unbind(GLES30.GL_TEXTURE_2D);
        glTextureNoise.unbind(GLES30.GL_TEXTURE_2D);

        if (useSwapBuffer) {
            glFramebufferSnowVelocitySwap.unbind(GLES30.GL_DRAW_FRAMEBUFFER);
            glTextureSnowVelocity.unbind(GLES30.GL_TEXTURE_2D);
        } else {
            glFramebufferSnowVelocity.unbind(GLES30.GL_DRAW_FRAMEBUFFER);
            glTextureSnowVelocitySwap.unbind(GLES30.GL_TEXTURE_2D);
        }

        GLES30.glViewport(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

    private void renderSnowAdvance(boolean useSwapBuffer) {
        glProgramSnowAdvance.useProgram();

        GLES30.glUniform1i(sSnowAdvancePositionSampler, 0);
        GLES30.glUniform1i(sSnowAdvanceVelocitySampler, 1);
        GLES30.glVertexAttribPointer(vSnowAdvanceInPosition, 2, GLES30.GL_BYTE, false, 0, verticesQuad);
        GLES30.glEnableVertexAttribArray(vSnowAdvanceInPosition);

        GLES30.glViewport(0, 0, PARTICLE_COUNT, PARTICLE_COUNT);

        if (useSwapBuffer) {
            glFramebufferSnowPositionSwap.bind(GLES30.GL_DRAW_FRAMEBUFFER);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            glTextureSnowPosition.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(0);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            glTextureSnowVelocity.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(1);
        } else {
            glFramebufferSnowPosition.bind(GLES30.GL_DRAW_FRAMEBUFFER);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            glTextureSnowPositionSwap.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(0);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            glTextureSnowVelocitySwap.bind(GLES30.GL_TEXTURE_2D);
            glSamplerNearest.bind(1);
        }

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        glSamplerNearest.unbind(0);
        glSamplerNearest.unbind(1);

        GLES30.glDisableVertexAttribArray(vSnowAdvanceInPosition);

        if (useSwapBuffer) {
            glTextureSnowPosition.unbind(GLES30.GL_TEXTURE_2D);
            glTextureSnowVelocity.unbind(GLES30.GL_TEXTURE_2D);
            glFramebufferSnowPositionSwap.unbind(GLES30.GL_DRAW_FRAMEBUFFER);
        } else {
            glTextureSnowPositionSwap.unbind(GLES30.GL_TEXTURE_2D);
            glTextureSnowVelocitySwap.unbind(GLES30.GL_TEXTURE_2D);
            glFramebufferSnowPosition.unbind(GLES30.GL_DRAW_FRAMEBUFFER);
        }

        GLES30.glViewport(0, 0, (int) screenSize.getWidth(), (int) screenSize.getHeight());
    }

    private void renderSnowParticles(boolean useSwapBuffer) {
        glProgramSnowParticle.useProgram();

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

        GLES30.glUniform1i(sSnowParticlePositionSampler, 0);
        GLES30.glUniform1i(sSnowParticleVelocitySampler, 1);
        GLES30.glUniform1f(uSnowParticlePositionScaleY, screenSize.getWidth() / screenSize.getHeight());
        GLES30.glUniform1f(uSnowParticleAlpha, snowAlpha);
        GLES30.glVertexAttribPointer(vSnowParticleInPosition, 2, GLES30.GL_BYTE, false, 0, verticesQuad);
        GLES30.glEnableVertexAttribArray(vSnowParticleInPosition);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        if (useSwapBuffer) {
            glTextureSnowPositionSwap.bind(GLES30.GL_TEXTURE_2D);
        } else {
            glTextureSnowPosition.bind(GLES30.GL_TEXTURE_2D);
        }
        glSamplerNearest.bind(0);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        if (useSwapBuffer) {
            glTextureSnowVelocitySwap.bind(GLES30.GL_TEXTURE_2D);
        } else {
            glTextureSnowVelocity.bind(GLES30.GL_TEXTURE_2D);
        }
        glSamplerNearest.bind(1);

        for (int posY = 0; posY < PARTICLE_COUNT; ++posY) {
            GLES30.glUniform1i(uSnowParticlePositionY, posY);
            GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLE_STRIP, 0, 4, PARTICLE_COUNT);
        }

        if (useSwapBuffer) {
            glTextureSnowPositionSwap.unbind(GLES30.GL_TEXTURE_2D);
            glTextureSnowVelocitySwap.unbind(GLES30.GL_TEXTURE_2D);
        } else {
            glTextureSnowPosition.unbind(GLES30.GL_TEXTURE_2D);
            glTextureSnowVelocity.unbind(GLES30.GL_TEXTURE_2D);
        }
        glSamplerNearest.unbind(0);
        glSamplerNearest.unbind(1);
        GLES30.glDisableVertexAttribArray(vSnowParticleInPosition);

        GLES30.glDisable(GLES30.GL_BLEND);
    }

    private void renderForeground() {
        glProgramForeground.useProgram();
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glUniform1f(uForegroundBrightness, (float) Math.random() * 0.2f);
        GLES30.glVertexAttribPointer(vForegroundInPosition, 2, GLES30.GL_BYTE, false, 0, verticesQuad);
        GLES30.glEnableVertexAttribArray(vForegroundInPosition);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(vForegroundInPosition);
        GLES30.glDisable(GLES30.GL_BLEND);
    }

}
