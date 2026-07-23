package com.example.smartfinancialmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ConfettiView — Full-screen confetti burst overlay.
 *
 * Mirrors the Compose ConfettiOverlay + ConfettiParticle implementation.
 *
 * A single ValueAnimator drives a master [progress] from 0f → 1f over DURATION_MS.
 * Every particle reads that one progress value and computes its own position/alpha,
 * so we never run 70 separate animators.
 *
 * Particle shapes: mix of rectangles and circles.
 * Fade out: last 20% of each particle's effective progress.
 * Touch: always returns false so touches fall through to the content below.
 */
public class ConfettiView extends View {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final int   PARTICLE_COUNT = 72;
    private static final long  DURATION_MS    = 2600L;

    /** GoalColors palette + extra festive colors */
    private static final int[] COLORS = {
            Color.parseColor("#2FE0AC"), // mint
            Color.parseColor("#FFC857"), // gold
            Color.parseColor("#FF6B7A"), // coral
            Color.parseColor("#4ade80"), // blue
            Color.parseColor("#FFD700"), // yellow
            Color.parseColor("#FF69B4"), // hot-pink
            Color.parseColor("#7FFF00"), // chartreuse
            Color.parseColor("#FF8C00"), // dark-orange
            Color.parseColor("#DA70D6"), // orchid
            Color.parseColor("#00CED1"), // dark-turquoise
    };

    // ── Particle data ────────────────────────────────────────────────────────
    /** All particle state stored in parallel float arrays for cache efficiency */
    private float[] pStartX;   // relative [0,1] of screen width
    private float[] pStartY;   // negative [-0.5, -0.05] of screen height (above top)
    private float[] pDrift;    // horizontal drift per full progress unit
    private float[] pRotSpeed; // degrees per full progress unit (signed)
    private float[] pSize;     // half-size in px
    private float[] pDelay;    // stagger delay [0, 0.25]
    private int[]   pColor;    // base color (fully opaque)
    private boolean[] pIsRect; // true → rectangle, false → circle

    private boolean particlesReady = false;

    // ── Animation ────────────────────────────────────────────────────────────
    private ValueAnimator animator;
    private float progress = 0f;
    private Runnable onFinished;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Constructors ─────────────────────────────────────────────────────────
    public ConfettiView(Context context) {
        super(context);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConfettiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // Hardware layer for smooth compositing of alpha changes
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Build particle data. Call once before {@link #burst(Runnable)}.
     * Can be called before the view is laid out — sizes are in screen-relative units.
     */
    public void buildParticles() {
        Random rnd = new Random();
        float density = getResources().getDisplayMetrics().density;

        pStartX   = new float[PARTICLE_COUNT];
        pStartY   = new float[PARTICLE_COUNT];
        pDrift    = new float[PARTICLE_COUNT];
        pRotSpeed = new float[PARTICLE_COUNT];
        pSize     = new float[PARTICLE_COUNT];
        pDelay    = new float[PARTICLE_COUNT];
        pColor    = new int[PARTICLE_COUNT];
        pIsRect   = new boolean[PARTICLE_COUNT];

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pStartX[i]   = rnd.nextFloat();
            pStartY[i]   = -(0.05f + rnd.nextFloat() * 0.35f);
            pDrift[i]    = (rnd.nextFloat() - 0.5f) * 0.35f;
            pRotSpeed[i] = (rnd.nextFloat() - 0.5f) * 900f;
            pSize[i]     = (5 + rnd.nextInt(9)) * density;
            pDelay[i]    = rnd.nextFloat() * 0.22f;
            pColor[i]    = COLORS[rnd.nextInt(COLORS.length)];
            pIsRect[i]   = rnd.nextBoolean();
        }
        particlesReady = true;
    }

    /**
     * Start the confetti burst. Calls {@code onDone} when animation finishes.
     */
    public void burst(Runnable onDone) {
        if (!particlesReady) buildParticles();
        this.onFinished = onDone;

        if (animator != null) animator.cancel();
        progress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_MS);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(anim -> {
            progress = (float) anim.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onFinished != null) onFinished.run();
            }
        });
        animator.start();
    }

    /** Cancel animation and hide. */
    public void stop() {
        if (animator != null) animator.cancel();
        setVisibility(GONE);
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!particlesReady) return;

        int W = getWidth();
        int H = getHeight();
        if (W == 0 || H == 0) return;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // Effective per-particle progress (delayed start)
            float delay = pDelay[i];
            float eff   = (progress - delay) / (1f - delay);
            if (eff <= 0f) continue;
            if (eff > 1f)  eff = 1f;

            // Position
            float px = (pStartX[i] + pDrift[i] * eff) * W;
            float py = (pStartY[i] + eff * 1.35f)      * H;

            // Fade out in last 20% of effective progress
            float alpha = eff > 0.80f ? (1f - (eff - 0.80f) / 0.20f) : 1f;
            if (alpha <= 0f) continue;

            int baseColor = pColor[i];
            int a = (int) (alpha * 255f);
            paint.setColor(Color.argb(a,
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)));

            canvas.save();
            canvas.translate(px, py);
            canvas.rotate(pRotSpeed[i] * eff);

            float s = pSize[i];
            if (pIsRect[i]) {
                // Flat ribbon shape: 2:1 aspect
                canvas.drawRect(-s, -s * 0.5f, s, s * 0.5f, paint);
            } else {
                canvas.drawCircle(0, 0, s * 0.6f, paint);
            }
            canvas.restore();
        }
    }

    // ── Touch passthrough ────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never consume touches — let them fall through to content below
        return false;
    }
}
