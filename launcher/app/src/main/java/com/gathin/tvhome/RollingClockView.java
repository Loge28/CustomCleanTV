package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

/**
 * Clean TV Clock with live mechanical rolling odometer digits for seconds.
 */
public final class RollingClockView extends View {

    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final float density;

    private int lastHour = -1, lastMin = -1;
    private int curSec10 = 0, curSec1 = 0;
    private int prevSec10 = 0, prevSec1 = 0;
    private long secTransitionStart = 0;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTime();
            invalidate();
            handler.postDelayed(this, 40);
        }
    };

    public RollingClockView(Context context) {
        this(context, null);
    }

    public RollingClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollingClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;

        timePaint.setColor(0xFFFFFFFF);
        timePaint.setTextSize(48f * density);
        timePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        timePaint.setLetterSpacing(-0.03f);
        timePaint.setShadowLayer(4f * density, 0, 1.5f * density, 0xB0000000);

        // Claude reference: white with 0.65 opacity (.clock .s { opacity: .65; color: #fff; })
        secPaint.setColor(0xA6FFFFFF);
        secPaint.setTextSize(26f * density);
        secPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        secPaint.setShadowLayer(3f * density, 0, 1.5f * density, 0xB0000000);

        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private boolean darkTheme = false;

    public void setDarkTheme(boolean dark) {
        this.darkTheme = dark;
        if (dark) {
            timePaint.setColor(0xFF1E2022);
            timePaint.setShadowLayer(4f * density, 0, 1.5f * density, 0x4DFFFFFF);
            secPaint.setColor(0xCC303336);
            secPaint.setShadowLayer(3f * density, 0, 1f * density, 0x4DFFFFFF);
        } else {
            timePaint.setColor(0xFFFFFFFF);
            timePaint.setShadowLayer(4f * density, 0, 1.5f * density, 0xB0000000);
            secPaint.setColor(0xA6FFFFFF);
            secPaint.setShadowLayer(3f * density, 0, 1.5f * density, 0xB0000000);
        }
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTime();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
    }

    private void updateTime() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        int s = c.get(Calendar.SECOND);

        int s10 = s / 10;
        int s1 = s % 10;

        if (s1 != curSec1) {
            prevSec1 = curSec1;
            curSec1 = s1;
            prevSec10 = curSec10;
            curSec10 = s10;
            secTransitionStart = SystemClock.uptimeMillis();
        }

        lastHour = h;
        lastMin = m;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (220 * density), (int) (64 * density));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float baseline = 46f * density;

        // 1. Draw HH:mm
        String hm = String.format(Locale.ROOT, "%02d:%02d", lastHour >= 0 ? lastHour : 12, lastMin >= 0 ? lastMin : 0);
        canvas.drawText(hm, 0, baseline, timePaint);

        float hmWidth = timePaint.measureText(hm);

        // 2. Draw rolling seconds
        float secX = hmWidth + 8f * density;
        float secBaseline = baseline - 2f * density;
        float rollHeight = 28f * density;

        long now = SystemClock.uptimeMillis();
        float t = Math.min(1f, (now - secTransitionStart) / 180f); // Snappy 180ms roll

        // Clip seconds area for rolling drum effect
        canvas.save();
        canvas.clipRect(secX - 2, 0, secX + 50 * density, getHeight());

        // Draw rolling tens
        drawRollingDigit(canvas, secX, secBaseline, prevSec10, curSec10, t, rollHeight);
        secX += secPaint.measureText("0") + 2f * density;

        // Draw rolling units
        drawRollingDigit(canvas, secX, secBaseline, prevSec1, curSec1, t, rollHeight);

        canvas.restore();

        if (t < 1f) {
            postInvalidateOnAnimation();
        }
    }

    private void drawRollingDigit(Canvas canvas, float x, float y, int prev, int cur, float t, float h) {
        if (t >= 1f || prev == cur) {
            canvas.drawText(String.valueOf(cur), x, y, secPaint);
        } else {
            // Cubic ease
            float ease = t * t * (3 - 2 * t);
            float offset = ease * h;

            // Old digit scrolls up
            canvas.drawText(String.valueOf(prev), x, y - offset, secPaint);

            // New digit rolls in from below
            canvas.drawText(String.valueOf(cur), x, y + h - offset, secPaint);
        }
    }
}
