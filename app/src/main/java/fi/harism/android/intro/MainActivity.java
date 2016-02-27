package fi.harism.android.intro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import fi.harism.android.intro.egl.EglCore;
import fi.harism.android.intro.view.GlTextureView;

public class MainActivity extends Activity {

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            glTextureView.renderFrame(frameTimeNanos);
            choreographer.postFrameCallback(this);
            onRender(mediaPlayer.getCurrentPosition());
        }
    };

    private Choreographer choreographer;
    private MainGlRenderer glRenderer;
    private GlTextureView glTextureView;
    private Button buttonBegin;
    private MediaPlayer mediaPlayer;
    private View layoutCredits;
    private View imagePointer;
    private View buttonLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        glRenderer = new MainGlRenderer(this);

        glTextureView = (GlTextureView) findViewById(R.id.gltextureview_main);
        glTextureView.setEglContext(EglCore.VERSION_GLES3, 0);
        glTextureView.setGlRenderer(glRenderer);
        glTextureView.setAlpha(0f);

        buttonBegin = (Button) findViewById(R.id.button_begin);
        buttonBegin.setAlpha(0f);
        buttonBegin.animate().alpha(1f).setStartDelay(500).setDuration(2000).start();
        buttonBegin.animate().rotation(360f).setDuration(2000).start();
        buttonBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBeginIntro();
            }
        });

        layoutCredits = findViewById(R.id.layout_credits);
        imagePointer = findViewById(R.id.image_pointer);
        buttonLink = findViewById(R.id.button_link);

        ObjectAnimator pointerAnimatorScaleX = ObjectAnimator.ofFloat(imagePointer, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator pointerAnimatorScaleY = ObjectAnimator.ofFloat(imagePointer, "scaleY", 1f, 1.5f, 1f);
        ObjectAnimator pointerAnimatorTranslationX = ObjectAnimator.ofFloat(imagePointer, "translationX", -30f, 30f, -30f);

        pointerAnimatorScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        pointerAnimatorScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        pointerAnimatorTranslationX.setRepeatCount(ObjectAnimator.INFINITE);

        pointerAnimatorTranslationX.setDuration(1234).start();
        AnimatorSet pointerScaleAnimatorSet = new AnimatorSet().setDuration(1000);
        pointerScaleAnimatorSet.playTogether(pointerAnimatorScaleX, pointerAnimatorScaleY);
        pointerScaleAnimatorSet.start();

        buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://youtu.be/vlktldEcKo8";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        choreographer = Choreographer.getInstance();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
    }

    @Override
    protected void onResume() {
        super.onResume();
        choreographer.postFrameCallback(frameCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        choreographer.removeFrameCallback(frameCallback);
        mediaPlayer.release();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        glTextureView.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void onBeginIntro() {
        mediaPlayer.start();
        buttonBegin.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                buttonBegin.setVisibility(View.GONE);
            }
        }).start();
        glTextureView.animate().alpha(1f).setDuration(6000).start();
    }

    private void onRender(long timeMillis) {
        if (timeMillis > 5000 && timeMillis < 15000) {
            float t = (timeMillis - 5000) / 10000f;
            layoutCredits.setVisibility(View.VISIBLE);
            layoutCredits.setAlpha((float) Math.random() * t);
            if (timeMillis > 14000) {
                float scale = (15000 - timeMillis) / 1000f;
                scale = scale * scale * (3 - scale * 2);
                layoutCredits.setScaleX(scale);
                layoutCredits.setScaleY(scale);
            }
        } else {
            layoutCredits.setVisibility(View.GONE);
        }
        if (timeMillis < 15000) {
            glRenderer.setSnowAlpha(0f);
        } else {
            glRenderer.setSnowAlpha((float) Math.random());
        }
    }

}
