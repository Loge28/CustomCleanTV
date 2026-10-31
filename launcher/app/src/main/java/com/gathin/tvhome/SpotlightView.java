package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Localized soft spotlight aura behind the focused icon.
 * Matches Claude reference: radial-gradient(ellipse 14% 22% at tx ty, rgba(150,190,255,.55), rgba(80,130,220,.2) 55%, transparent 100%).
 * Strictly localized to the focused tile, dynamically anchored to the view to eliminate any scroll/navigation drift.
 */
public final class SpotlightView extends View {

    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float currentX = -1f, targetX = -1f;
    private float currentY = -1f, targetY = -1f;
    private WeakReference<View> targetViewRef;
    private final Rect tempRect = new Rect();

    private RadialGradient baseShader;
    private final Matrix shaderMatrix = new Matrix();
    private float rx = 0f, ry = 0f;

    public SpotlightView(Context context) {
        this(context, null);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        glowPaint.setStyle(Paint.Style.FILL);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void setTargetView(View view) {
        this.targetViewRef = (view != null) ? new WeakReference<>(view) : null;
        if (view != null) {
            view.getGlobalVisibleRect(tempRect);
            targetX = tempRect.exactCenterX();
            targetY = tempRect.exactCenterY();
            if (currentX < 0) {
                currentX = targetX;
                currentY = targetY;
            }
        }
        postInvalidateOnAnimation();
    }

    public void setTarget(float x, float y) {
        this.targetViewRef = null;
        targetX = x;
        targetY = y;
        if (currentX < 0) {
            currentX = x;
            currentY = y;
        }
        postInvalidateOnAnimation();
    }

    public void adjustOffset(float dx, float dy) {
        if (targetX >= 0) {
            targetX += dx;
            targetY += dy;
            currentX += dx;
            currentY += dy;
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            rx = w * 0.14f;
            ry = h * 0.22f;
            baseShader = new RadialGradient(
                    0, 0, rx,
                    new int[]{0x8C96BEFF, 0x335082DC, 0x00000000},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP);
            glowPaint.setShader(baseShader);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (targetX < 0) return;

        View tv = targetViewRef != null ? targetViewRef.get() : null;
        if (tv != null && tv.isAttachedToWindow() && tv.isShown()) {
            tv.getGlobalVisibleRect(tempRect);
            targetX = tempRect.exactCenterX();
            targetY = tempRect.exactCenterY();
        }

        // Smooth ease towards target
        float dx = targetX - currentX;
        float dy = targetY - currentY;
        currentX += dx * 0.25f;
        currentY += dy * 0.25f;

        if (baseShader == null && getWidth() > 0 && getHeight() > 0) {
            onSizeChanged(getWidth(), getHeight(), 0, 0);
        }
        if (baseShader == null) return;

        shaderMatrix.reset();
        shaderMatrix.setScale(1f, ry / rx);
        shaderMatrix.postTranslate(currentX, currentY);
        baseShader.setLocalMatrix(shaderMatrix);

        canvas.drawRect(currentX - rx, currentY - ry, currentX + rx, currentY + ry, glowPaint);

        if (Math.abs(dx) > 0.5f || Math.abs(dy) > 0.5f) {
            postInvalidateOnAnimation();
        }
    }
}
