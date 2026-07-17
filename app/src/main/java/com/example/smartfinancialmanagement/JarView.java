package com.example.smartfinancialmanagement;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * JarView — Custom View that draws the animated liquid-fill savings jar.
 * Mirrors the JarVisual Composable from GoalProgressScreen.kt.
 */
public class JarView extends View {

    private final Paint jarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float animatedPercent = 0f;
    private ValueAnimator animator;

    // GoalColors matching the Compose design
    private static final int COLOR_BG        = Color.parseColor("#090D1A");
    private static final int COLOR_BORDER    = Color.parseColor("#232D4D");
    private static final int COLOR_MINT      = Color.parseColor("#2FE0AC");
    private static final int COLOR_MINT_DIM  = Color.parseColor("#1A8F6F");
    private static final int COLOR_TEXT      = Color.parseColor("#EEF1FB");

    public JarView(Context context) { super(context); init(); }
    public JarView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public JarView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        jarPaint.setColor(COLOR_BG);
        jarPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(COLOR_BORDER);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        lidPaint.setColor(COLOR_BORDER);
        lidPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(42f);
        textPaint.setFakeBoldText(true);
    }

    /**
     * Set the fill percentage (0–100) with an animated transition.
     */
    public void setPercent(float targetPercent) {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(animatedPercent, targetPercent);
        animator.setDuration(700);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            animatedPercent = (float) anim.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        // Build jar path (matches the Compose bezier outline)
        Path jarPath = new Path();
        jarPath.moveTo(w * 0.19f, h * 0.07f);
        jarPath.lineTo(w * 0.81f, h * 0.07f);
        jarPath.lineTo(w * 0.81f, h * 0.15f);
        jarPath.cubicTo(w * 0.81f, h * 0.18f, w * 0.77f, h * 0.20f, w * 0.77f, h * 0.23f);
        jarPath.lineTo(w * 0.77f, h * 0.83f);
        jarPath.cubicTo(w * 0.77f, h * 0.92f, w * 0.69f, h * 0.97f, w * 0.5f, h * 0.97f);
        jarPath.cubicTo(w * 0.31f, h * 0.97f, w * 0.23f, h * 0.92f, w * 0.23f, h * 0.83f);
        jarPath.lineTo(w * 0.23f, h * 0.23f);
        jarPath.cubicTo(w * 0.23f, h * 0.20f, w * 0.19f, h * 0.18f, w * 0.19f, h * 0.15f);
        jarPath.close();

        // Draw jar background
        canvas.drawPath(jarPath, jarPaint);

        // Draw liquid fill clipped to jar shape
        canvas.save();
        canvas.clipPath(jarPath);
        float fillTop = h - (animatedPercent / 100f) * (h * 0.90f);
        LinearGradient gradient = new LinearGradient(0, fillTop, 0, h,
                COLOR_MINT, COLOR_MINT_DIM, Shader.TileMode.CLAMP);
        liquidPaint.setShader(gradient);
        canvas.drawRect(0, fillTop, w, h, liquidPaint);
        canvas.restore();

        // Draw jar border on top
        canvas.drawPath(jarPath, borderPaint);

        // Draw lid
        RectF lidRect = new RectF(w * 0.17f, h * 0.03f, w * 0.83f, h * 0.10f);
        canvas.drawRoundRect(lidRect, 12f, 12f, lidPaint);

        // Draw percent text centered
        float textY = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(((int) animatedPercent) + "%", w / 2f, textY, textPaint);
    }
}
