package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Natural Amegakure rainfall overlay matching Claude's exact algorithm:
 * 90 delicate 1px streaks, cold translucent tint rgba(170,200,255,.35), slanted with wind drift.
 */
public final class HiddenRainView extends View {

    private static final int COUNT = 90;
    private final float[] x = new float[COUNT];
    private final float[] y = new float[COUNT];
    private final float[] len = new float[COUNT];
    private final float[] vel = new float[COUNT];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rnd = new Random();
    private boolean initialized = false;

    public HiddenRainView(Context context) {
        this(context, null);
    }

    public HiddenRainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HiddenRainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Claude exact: rgba(170,200,255,.35), 1px width
        paint.setColor(0x59AAC8FF);
        paint.setStrokeWidth(1.0f * getResources().getDisplayMetrics().density);
        paint.setStrokeCap(Paint.Cap.BUTT);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private void initRain(int w, int h) {
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < COUNT; i++) {
            x[i] = rnd.nextFloat() * (w + 100);
            y[i] = rnd.nextFloat() * h;
            len[i] = (14f + rnd.nextFloat() * 20f) * density;
            vel[i] = (14f + rnd.nextFloat() * 14f) * density;
        }
        initialized = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) initRain(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (!initialized) initRain(w, h);

        for (int i = 0; i < COUNT; i++) {
            // Claude exact: lineTo(x - l*0.2, y + l)
            float lx = x[i] - len[i] * 0.2f;
            float ly = y[i] + len[i];
            canvas.drawLine(x[i], y[i], lx, ly, paint);

            // Wind drift and fall
            y[i] += vel[i];
            x[i] -= vel[i] * 0.2f;

            if (y[i] > h) {
                y[i] = -20f;
                x[i] = rnd.nextFloat() * w + 30f;
            }
        }
        postInvalidateOnAnimation();
    }
}
