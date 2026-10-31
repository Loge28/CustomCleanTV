package com.gathin.tvhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * Cinematic 900-degree Sharingan spin on launcher boot.
 */
public final class SharinganBootView extends View {

    private final Paint bgPaint = new Paint();
    private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path tomoePath = new Path();
    private final float density;

    private long startTime;
    private static final long DURATION = 2200L;
    private boolean finished = false;

    public SharinganBootView(Context context) {
        this(context, null);
    }

    public SharinganBootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SharinganBootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        bgPaint.setColor(0xFF000000);
        redPaint.setStyle(Paint.Style.FILL);
        blackPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(0xFFDC2626);
        textPaint.setTextSize(14f * density);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setLetterSpacing(0.25f);

        buildTomoe();
        startTime = SystemClock.uptimeMillis();
    }

    private void buildTomoe() {
        tomoePath.reset();
        float r = 10f * density;
        tomoePath.addCircle(0, 0, r, Path.Direction.CW);
        tomoePath.moveTo(-r, 0);
        tomoePath.quadTo(-r * 2.5f, r * 2f, 0, r * 3.2f);
        tomoePath.quadTo(-r * 0.8f, r * 1.5f, r, 0);
        tomoePath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        long elapsed = SystemClock.uptimeMillis() - startTime;
        float progress = Math.min(1f, (float) elapsed / DURATION);

        // Fill background
        canvas.drawRect(0, 0, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.45f;

        // Rotation: 0 to 900 degrees with smooth ease-out
        float t = progress;
        float ease = 1f - (1f - t) * (1f - t) * (1f - t);
        float angle = ease * 900f;
        float scale = ease < 0.7f ? (0.4f + ease * 0.9f) : (1.03f - (ease - 0.7f) * 0.1f);

        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(scale, scale);
        canvas.rotate(angle);

        float irisRadius = 70f * density;

        // Crimson Sharingan background with outer glow
        RadialGradient grad = new RadialGradient(
                0, 0, irisRadius,
                new int[]{0xFFFF3B30, 0xFFDC2626, 0xFF7F1D1D},
                new float[]{0f, 0.7f, 1f},
                Shader.TileMode.CLAMP);
        redPaint.setShader(grad);
        canvas.drawCircle(0, 0, irisRadius, redPaint);

        // Concentric Rings
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeWidth(3.5f * density);
        blackPaint.setColor(0xFF1A0508);
        canvas.drawCircle(0, 0, irisRadius * 0.95f, blackPaint);
        blackPaint.setStrokeWidth(2f * density);
        canvas.drawCircle(0, 0, irisRadius * 0.58f, blackPaint);

        // Center Pupil
        blackPaint.setStyle(Paint.Style.FILL);
        blackPaint.setColor(0xFF000000);
        canvas.drawCircle(0, 0, irisRadius * 0.22f, blackPaint);

        // 3 Tomoe
        for (int i = 0; i < 3; i++) {
            canvas.save();
            canvas.rotate(i * 120f);
            canvas.translate(0, -irisRadius * 0.58f);
            canvas.drawPath(tomoePath, blackPaint);
            canvas.restore();
        }

        canvas.restore();

        // Subtitle
        canvas.drawText("SHARINGAN SYSTEM INITIALIZED", cx, cy + irisRadius + 45f * density, textPaint);

        if (progress < 1f) {
            postInvalidateOnAnimation();
        } else if (!finished) {
            finished = true;
            animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewGroup parent = (ViewGroup) getParent();
                    if (parent != null) parent.removeView(SharinganBootView.this);
                }
            }).start();
        }
    }
}
