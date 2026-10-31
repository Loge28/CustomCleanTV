package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Itachi's Sharingan eyes tracking the focused app and blinking.
 * Built with 100% mathematical fidelity to Claude's golden reference:
 * Exact quadratic bezier tomoe curves, authentic crimson radial gradient,
 * specular reflection highlight, aperture masking, and wet hair occlusion.
 */
public final class ItachiEyesView extends View {

    private Bitmap maskL, occL;
    private Bitmap maskR, occR;
    private Bitmap compL, compR;
    private Canvas canvasL, canvasR;

    private final Paint clearPaint = new Paint();
    private final Paint irisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lidEdgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Path tomoeTail = new Path();
    private final RectF dstRect = new RectF();
    private final RectF specRect = new RectF();

    private float targetNormX = 0.5f;
    private float currentLookXL = 0f;
    private float currentLookXR = 0f;
    private float lookY = 0f;

    // Blinking state
    private boolean isBlinking = false;
    private float blinkProgress = 0f;
    private long blinkStart = 0;
    private long nextBlinkTime = 0;
    private final Random rnd = new Random();

    public ItachiEyesView(Context context) {
        this(context, null);
    }

    public ItachiEyesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItachiEyesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Load masks and occlusion hair bitmaps
        maskL = BitmapFactory.decodeResource(getResources(), R.drawable.eye_left_mask);
        occL = BitmapFactory.decodeResource(getResources(), R.drawable.eye_left_occ);
        maskR = BitmapFactory.decodeResource(getResources(), R.drawable.eye_right_mask);
        occR = BitmapFactory.decodeResource(getResources(), R.drawable.eye_right_occ);

        // Create compositing buffers matching reference boxes (110x60 and 150x80)
        compL = Bitmap.createBitmap(110, 60, Bitmap.Config.ARGB_8888);
        canvasL = new Canvas(compL);

        compR = Bitmap.createBitmap(150, 80, Bitmap.Config.ARGB_8888);
        canvasR = new Canvas(compR);

        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        irisPaint.setStyle(Paint.Style.FILL);

        ringPaint.setStyle(Paint.Style.STROKE);

        pupilPaint.setStyle(Paint.Style.FILL);
        pupilPaint.setColor(0xFF0B0306);

        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(0xBFFFEBE6); // rgba(255,235,230,.75)

        glowPaint.setStyle(Paint.Style.FILL);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        skinPaint.setStyle(Paint.Style.FILL);
        lidEdgePaint.setStyle(Paint.Style.FILL);
        lidEdgePaint.setColor(0xE605080E);

        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        nextBlinkTime = SystemClock.uptimeMillis() + 2500;
    }

    public void trackApp(float cardCenterX, float screenWidth) {
        if (screenWidth <= 0) return;
        targetNormX = cardCenterX / screenWidth;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        long now = SystemClock.uptimeMillis();

        // 1. Blinking state machine (Claude reference: 240ms duration)
        if (!isBlinking && now > nextBlinkTime) {
            isBlinking = true;
            blinkStart = now;
            nextBlinkTime = now + 2500 + rnd.nextInt(3500);
            if (rnd.nextFloat() < 0.2f) nextBlinkTime = now + 450;
        }
        if (isBlinking) {
            float p = (now - blinkStart) / 240f;
            if (p >= 1f) {
                isBlinking = false;
                blinkProgress = 0f;
            } else if (p < 0.29f) {
                blinkProgress = p / 0.29f;
            } else if (p < 0.46f) {
                blinkProgress = 1f;
            } else {
                blinkProgress = 1f - (p - 0.46f) / 0.54f;
            }
        }

        // Screen scale factors from 1400x781
        float sx = (float) w / 1400f;
        float sy = (float) h / 781f;

        // 2. Physics tracking for Left Eye
        // cx=739, cy=310, r=17.5, ax0=720, ax1=779, bx=690, by=285
        float exL = (739f * sx) / w;
        float uL = Math.max(-1f, Math.min(1f, (targetNormX - exL) / 0.3f));
        float rlL = 739f - (720f + 17.5f * 0.7f);
        float rrL = (779f - 17.5f * 0.7f) - 739f;
        float targetXL = uL * (uL < 0 ? rlL : rrL);
        currentLookXL += (targetXL - currentLookXL) * 0.22f;

        // 3. Physics tracking for Right Eye
        // cx=938, cy=281, r=26, ax0=887, ax1=978, bx=850, by=240
        float exR = (938f * sx) / w;
        float uR = Math.max(-1f, Math.min(1f, (targetNormX - exR) / 0.3f));
        float rlR = 938f - (887f + 26f * 0.7f);
        float rrR = (978f - 26f * 0.7f) - 938f;
        float targetXR = uR * (uR < 0 ? rlR : rrR);
        currentLookXR += (targetXR - currentLookXR) * 0.22f;

        lookY = 3.5f; // downward dock focus

        // Draw Left Eye
        drawEye(canvasL, maskL, occL, canvas,
                690f, 285f, 110, 60,
                739f, 310f, 17.5f,
                720f, 779f, 295f, 331f,
                currentLookXL, lookY,
                20, 30, 46,
                sx, sy);

        // Draw Right Eye
        drawEye(canvasR, maskR, occR, canvas,
                850f, 240f, 150, 80,
                938f, 281f, 26f,
                887f, 978f, 258f, 305f,
                currentLookXR, lookY,
                8, 13, 27,
                sx, sy);

        if (Math.abs(targetXL - currentLookXL) > 0.3f || Math.abs(targetXR - currentLookXR) > 0.3f || isBlinking) {
            postInvalidateOnAnimation();
        }
    }

    private void drawEye(Canvas eyeCanvas, Bitmap mask, Bitmap occ, Canvas screenCanvas,
                         float bx, float by, int bw, int bh,
                         float cx, float cy, float r,
                         float ax0, float ax1, float ay0, float ay1,
                         float lookX, float lookY,
                         int skinR, int skinG, int skinB,
                         float sx, float sy) {

        // 1. Clear offscreen bitmap
        eyeCanvas.drawRect(0, 0, bw, bh, clearPaint);

        float gx = (cx - bx) + lookX;
        float gy = (cy - by) + lookY;

        // 2. Ambient Red Glow (Claude: r*0.6 to r*2)
        glowPaint.setShader(new RadialGradient(gx, gy, r * 2.0f,
                new int[]{0x59FF3228, 0x00FF3228},
                new float[]{0.3f, 1.0f},
                Shader.TileMode.CLAMP));
        eyeCanvas.drawCircle(gx, gy, r * 2.0f, glowPaint);

        // 3. Sharingan Iris (Claude exact: -r*.2,-r*.25,r*.1,0,0,r -> #ff4a35, #e5211c, #a50d12)
        irisPaint.setShader(new RadialGradient(gx - r * 0.2f, gy - r * 0.25f, r,
                new int[]{0xFFFF4A35, 0xFFE5211C, 0xFFA50D12},
                new float[]{0.1f, 0.6f, 1.0f},
                Shader.TileMode.CLAMP));
        eyeCanvas.drawCircle(gx, gy, r, irisPaint);

        // Outer dark ring (Claude: #4a0a10, strokeWidth r*.09, arc r*.96)
        ringPaint.setColor(0xFF4A0A10);
        ringPaint.setStrokeWidth(r * 0.09f);
        eyeCanvas.drawCircle(gx, gy, r * 0.96f, ringPaint);

        // Middle dark ring (Claude: rgba(30,0,5,.85), strokeWidth r*.06, arc r*.58)
        ringPaint.setColor(0xD91E0005);
        ringPaint.setStrokeWidth(r * 0.06f);
        eyeCanvas.drawCircle(gx, gy, r * 0.58f, ringPaint);

        // Center pupil (Claude: #0b0306, arc r*.2)
        eyeCanvas.drawCircle(gx, gy, r * 0.20f, pupilPaint);

        // 4. Three Tomoe Pinwheels (Claude exact quadratic curves)
        // for(let i=0;i<3;i++) { c.rotate(i*2.0944-.5); ... }
        for (int i = 0; i < 3; i++) {
            eyeCanvas.save();
            eyeCanvas.rotate(i * 120f - 28.65f, gx, gy);
            eyeCanvas.translate(gx, gy);

            // Circular Tomoe Head at (0, -r*.58), radius r*.15
            eyeCanvas.drawCircle(0, -r * 0.58f, r * 0.15f, pupilPaint);

            // Curved Tomoe Tail:
            // moveTo(-r*.15, -r*.58)
            // quadraticCurveTo(-r*.42, -r*.52, -r*.34, -r*.3)
            // quadraticCurveTo(-r*.3, -r*.5, -r*.06, -r*.5)
            tomoeTail.reset();
            tomoeTail.moveTo(-r * 0.15f, -r * 0.58f);
            tomoeTail.quadTo(-r * 0.42f, -r * 0.52f, -r * 0.34f, -r * 0.30f);
            tomoeTail.quadTo(-r * 0.30f, -r * 0.50f, -r * 0.06f, -r * 0.50f);
            tomoeTail.close();
            eyeCanvas.drawPath(tomoeTail, pupilPaint);

            eyeCanvas.restore();
        }

        // 5. Specular Reflection Highlight (Claude exact: ellipse -r*.45,-r*.45, r*.14, r*.07, -.7)
        eyeCanvas.save();
        eyeCanvas.translate(gx - r * 0.45f, gy - r * 0.45f);
        eyeCanvas.rotate(-40f);
        specRect.set(-r * 0.14f, -r * 0.07f, r * 0.14f, r * 0.07f);
        eyeCanvas.drawOval(specRect, highlightPaint);
        eyeCanvas.restore();

        // 6. Shading under upper eyelid (Claude: rgba(10,0,10,.55) atop)
        float t0 = ay0 - by;
        float hh = ay1 - ay0;
        shadowPaint.setShader(new LinearGradient(0, t0, 0, t0 + hh * 0.55f,
                0x8C0A000A, 0x000A000A, Shader.TileMode.CLAMP));
        eyeCanvas.drawRect(0, 0, bw, bh, shadowPaint);

        // 7. Eyelid if blinking (Claude exact drop and crease)
        if (blinkProgress > 0.01f) {
            skinPaint.setColor(Color.rgb((int) (skinR * 0.9f), (int) (skinG * 0.9f), (int) (skinB * 0.95f)));
            eyeCanvas.drawRect(0, t0 - 1, bw, t0 + hh * blinkProgress * 0.85f + 1, skinPaint);
            eyeCanvas.drawRect(0, t0 + hh - hh * blinkProgress * 0.15f, bw, t0 + hh + 2, skinPaint);
            eyeCanvas.drawRect(0, t0 + hh * blinkProgress * 0.85f - 1f, bw, t0 + hh * blinkProgress * 0.85f + 1f, lidEdgePaint);
        }

        // 8. CLIP TO EYE APERTURE MASK (Claude: destination-in e.m)
        eyeCanvas.drawBitmap(mask, 0, 0, maskPaint);

        // 9. DRAW COMPOSITED EYE ONTO SCREEN
        dstRect.set(bx * sx, by * sy, (bx + bw) * sx, (by + bh) * sy);
        Bitmap comp = (bw == 110) ? compL : compR;
        screenCanvas.drawBitmap(comp, null, dstRect, drawPaint);

        // 10. DRAW OCCLUSION (HAIR STRANDS + RAIN DROPLETS) ON TOP! (Claude: drawImage e.o)
        screenCanvas.drawBitmap(occ, null, dstRect, drawPaint);
    }
}
