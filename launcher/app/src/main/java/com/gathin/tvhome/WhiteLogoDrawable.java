package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Renders pure white logo-only vector icons matching Claude's reference preview.
 * Contains dedicated, authentic vector icons for every TV application.
 * Free of any box, container border, or background frame.
 */
public class WhiteLogoDrawable extends Drawable {

    private final String pkg;
    private final String label;
    private final Drawable fallbackIcon;
    private final float density;

    private final Paint whiteFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint whiteStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final int primaryColor;

    public WhiteLogoDrawable(Context context, String pkg, String label, Drawable fallbackIcon) {
        this(context, pkg, label, fallbackIcon, false);
    }

    public WhiteLogoDrawable(Context context, String pkg, String label, Drawable fallbackIcon, boolean darkTheme) {
        this.pkg = pkg != null ? pkg.toLowerCase() : "";
        this.label = label != null ? label : "";
        this.fallbackIcon = fallbackIcon;
        this.density = context.getResources().getDisplayMetrics().density;
        this.primaryColor = darkTheme ? 0xFF1C1D1F : Color.WHITE;

        whiteFill.setColor(primaryColor);
        whiteFill.setStyle(Paint.Style.FILL);

        whiteStroke.setColor(primaryColor);
        whiteStroke.setStyle(Paint.Style.STROKE);
        whiteStroke.setStrokeWidth(2.8f * density);
        whiteStroke.setStrokeCap(Paint.Cap.ROUND);
        whiteStroke.setStrokeJoin(Paint.Join.ROUND);

        redFill.setColor(0xFFE50914); // YouTube / Netflix signature red
        redFill.setStyle(Paint.Style.FILL);

        textPaint.setColor(primaryColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.width() <= 0 || bounds.height() <= 0) return;

        float cx = bounds.exactCenterX();
        float cy = bounds.exactCenterY();
        float size = Math.min(bounds.width(), bounds.height());

        if (pkg.contains("youtube.tvmusic")) {
            // YouTube Music: Double concentric circles with center play triangle
            float r = size * 0.38f;
            canvas.drawCircle(cx, cy, r, whiteStroke);
            canvas.drawCircle(cx, cy, r * 0.65f, whiteStroke);
            path.reset();
            float tri = r * 0.35f;
            path.moveTo(cx - tri * 0.4f, cy - tri);
            path.lineTo(cx + tri * 0.8f, cy);
            path.lineTo(cx - tri * 0.4f, cy + tri);
            path.close();
            canvas.drawPath(path, whiteFill);

        } else if (pkg.contains("youtube")) {
            // YouTube: Claude reference <rect rx="9" fill="#fff"/><path fill="#e50914"/>
            float w = size * 0.82f;
            float h = size * 0.58f;
            rect.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            canvas.drawRoundRect(rect, 10f * density, 10f * density, whiteFill);

            path.reset();
            float triW = size * 0.24f;
            float triH = size * 0.26f;
            path.moveTo(cx - triW * 0.35f, cy - triH / 2f);
            path.lineTo(cx + triW * 0.65f, cy);
            path.lineTo(cx - triW * 0.35f, cy + triH / 2f);
            path.close();
            canvas.drawPath(path, redFill);

        } else if (pkg.contains("graymatrix") || pkg.contains("zee") || label.toLowerCase().contains("zee")) {
            // ZEE5: Claude reference <text font-size="30" font-weight="800" font-style="italic">Z5</text>
            textPaint.setTextSize(size * 0.68f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
            textPaint.setColor(primaryColor);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText("Z5", cx, baseline, textPaint);

        } else if (pkg.contains("hotstar")) {
            // Disney+ Hotstar: Clean sharp 5-pointed star
            drawStar(canvas, cx, cy, size * 0.38f, size * 0.17f);

        } else if (pkg.contains("crunchyroll") || label.toLowerCase().contains("crunchy")) {
            // Crunchyroll: Signature eye crescent
            float rOuter = size * 0.38f;
            canvas.drawCircle(cx, cy, rOuter, whiteFill);
            Paint bgCutter = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgCutter.setColor(0xFF000000);
            bgCutter.setXfermode(new android.graphics.PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawCircle(cx + rOuter * 0.25f, cy, rOuter * 0.65f, bgCutter);
            canvas.drawCircle(cx + rOuter * 0.28f, cy, rOuter * 0.32f, whiteFill);

        } else if (pkg.contains("sonyliv") || label.toLowerCase().contains("sonyliv") || label.toLowerCase().contains("liv")) {
            // SonyLIV: Bold "LIV" typography
            textPaint.setTextSize(size * 0.52f);
            textPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
            textPaint.setColor(primaryColor);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText("LIV", cx, baseline, textPaint);

        } else if (pkg.contains("browser") || label.toLowerCase().contains("browser")) {
            // TV Browser: Clean modern wireframe globe
            float r = size * 0.36f;
            canvas.drawCircle(cx, cy, r, whiteStroke);
            canvas.drawLine(cx - r, cy, cx + r, cy, whiteStroke);
            canvas.drawLine(cx, cy - r, cx, cy + r, whiteStroke);
            rect.set(cx - r * 0.55f, cy - r, cx + r * 0.55f, cy + r);
            canvas.drawOval(rect, whiteStroke);

        } else if (pkg.contains("videolan") || pkg.contains("vlc")) {
            // VLC Media Player: Traffic cone
            path.reset();
            float r = size * 0.36f;
            path.moveTo(cx - r * 0.2f, cy - r);
            path.lineTo(cx + r * 0.2f, cy - r);
            path.lineTo(cx + r * 0.65f, cy + r * 0.7f);
            path.lineTo(cx - r * 0.65f, cy + r * 0.7f);
            path.close();
            canvas.drawPath(path, whiteStroke);
            rect.set(cx - r * 0.85f, cy + r * 0.7f, cx + r * 0.85f, cy + r * 0.95f);
            canvas.drawRoundRect(rect, 3f * density, 3f * density, whiteFill);

        } else if (pkg.contains("sendfilestotv") || pkg.contains("yablio")) {
            // Send Files to TV: Opposing transfer arrows
            path.reset();
            float r = size * 0.34f;
            // Up arrow (left)
            path.moveTo(cx - r * 0.4f, cy + r * 0.8f);
            path.lineTo(cx - r * 0.4f, cy - r * 0.4f);
            path.lineTo(cx - r * 0.7f, cy - r * 0.1f);
            path.moveTo(cx - r * 0.4f, cy - r * 0.4f);
            path.lineTo(cx - r * 0.1f, cy - r * 0.1f);
            // Down arrow (right)
            path.moveTo(cx + r * 0.4f, cy - r * 0.8f);
            path.lineTo(cx + r * 0.4f, cy + r * 0.4f);
            path.lineTo(cx + r * 0.1f, cy + r * 0.1f);
            path.moveTo(cx + r * 0.4f, cy + r * 0.4f);
            path.lineTo(cx + r * 0.7f, cy + r * 0.1f);
            canvas.drawPath(path, whiteStroke);

        } else if (pkg.contains("smartshare") || pkg.contains("milink") || pkg.contains("cast")) {
            // Miracast / Screen Mirror: TV with cast waves
            float w = size * 0.70f;
            float h = size * 0.48f;
            rect.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            canvas.drawRoundRect(rect, 5f * density, 5f * density, whiteStroke);
            // Cast arcs at bottom left
            float arcX = cx - w / 2f + 6f * density;
            float arcY = cy + h / 2f - 6f * density;
            canvas.drawCircle(arcX, arcY, 2.5f * density, whiteFill);
            canvas.drawArc(new RectF(arcX - 8 * density, arcY - 8 * density, arcX + 8 * density, arcY + 8 * density), -90, 90, false, whiteStroke);

        } else if (pkg.contains("mediaexplorer") || pkg.contains("gallery")) {
            // Media Explorer / Gallery: Photos badge
            float w = size * 0.68f;
            float h = size * 0.52f;
            rect.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            canvas.drawRoundRect(rect, 6f * density, 6f * density, whiteStroke);
            // Sun & mountains
            canvas.drawCircle(cx - w * 0.2f, cy - h * 0.15f, 4f * density, whiteFill);
            path.reset();
            path.moveTo(cx - w * 0.35f, cy + h * 0.35f);
            path.lineTo(cx - w * 0.05f, cy - h * 0.05f);
            path.lineTo(cx + w * 0.15f, cy + h * 0.15f);
            path.lineTo(cx + w * 0.35f, cy + h * 0.35f);
            canvas.drawPath(path, whiteStroke);

        } else if (pkg.contains("vending") || label.toLowerCase().contains("play")) {
            // Google Play Store: Official Play triangle
            path.reset();
            float r = size * 0.35f;
            path.moveTo(cx - r * 0.6f, cy - r);
            path.lineTo(cx + r * 0.8f, cy);
            path.lineTo(cx - r * 0.6f, cy + r);
            path.close();
            canvas.drawPath(path, whiteStroke);

        } else if (pkg.contains("dream")) {
            // MiDream / Screensaver: Crisp vector crescent / half moon
            path.reset();
            float r = size * 0.36f;
            // Outer circular arc to left
            path.moveTo(cx, cy - r);
            path.cubicTo(cx - r * 1.35f, cy - r, cx - r * 1.35f, cy + r, cx, cy + r);
            // Inner hollow arc returning to top
            path.cubicTo(cx - r * 0.45f, cy + r * 0.70f, cx - r * 0.45f, cy - r * 0.70f, cx, cy - r);
            path.close();
            canvas.drawPath(path, whiteFill);

        } else if (pkg.contains("netmirror")) {
            // NetMirror: Hybrid Netflix "N" + Prime Video curved smile arrow
            textPaint.setTextSize(size * 0.56f);
            textPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
            textPaint.setColor(primaryColor);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = (cy - size * 0.06f) - (fm.ascent + fm.descent) / 2f;
            canvas.drawText("N", cx, baseline, textPaint);

            // Prime curved smile arrow beneath
            path.reset();
            float arrowY = cy + size * 0.26f;
            float x0 = cx - size * 0.30f;
            float x1 = cx + size * 0.28f;
            path.moveTo(x0, arrowY - size * 0.05f);
            path.quadTo(cx, arrowY + size * 0.10f, x1, arrowY);
            canvas.drawPath(path, whiteStroke);

            // Prime arrow head
            path.reset();
            path.moveTo(x1 - size * 0.09f, arrowY - size * 0.06f);
            path.lineTo(x1 + size * 0.03f, arrowY + size * 0.01f);
            path.lineTo(x1 - size * 0.03f, arrowY + size * 0.10f);
            canvas.drawPath(path, whiteStroke);

        } else if (pkg.contains("spotify")) {
            // Spotify: Clean vector circle with 3 curved sound wave arcs
            float r = size * 0.36f;
            canvas.drawCircle(cx, cy, r, whiteStroke);

            // Arc 1 (top, longest)
            path.reset();
            path.moveTo(cx - size * 0.20f, cy - size * 0.08f);
            path.quadTo(cx, cy - size * 0.18f, cx + size * 0.20f, cy - size * 0.08f);
            canvas.drawPath(path, whiteStroke);

            // Arc 2 (middle)
            path.reset();
            path.moveTo(cx - size * 0.16f, cy + size * 0.04f);
            path.quadTo(cx, cy - size * 0.04f, cx + size * 0.16f, cy + size * 0.04f);
            canvas.drawPath(path, whiteStroke);

            // Arc 3 (bottom, shortest)
            path.reset();
            path.moveTo(cx - size * 0.12f, cy + size * 0.15f);
            path.quadTo(cx, cy + size * 0.08f, cx + size * 0.12f, cy + size * 0.15f);
            canvas.drawPath(path, whiteStroke);

        } else if (pkg.contains("tv") || pkg.contains("live")) {
            // Live TV / Inputs: Modern TV Screen
            float w = size * 0.72f;
            float h = size * 0.50f;
            rect.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            canvas.drawRoundRect(rect, 5f * density, 5f * density, whiteStroke);
            canvas.drawLine(cx - 12f * density, cy + h / 2f + 5f * density, cx + 12f * density, cy + h / 2f + 5f * density, whiteStroke);
            canvas.drawLine(cx, cy + h / 2f, cx, cy + h / 2f + 5f * density, whiteStroke);

        } else if (pkg.contains("settings")) {
            // Settings: Clean precision gear
            float r = size * 0.30f;
            canvas.drawCircle(cx, cy, r, whiteStroke);
            canvas.drawCircle(cx, cy, r * 0.42f, whiteStroke);
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(i * 60);
                float x1 = (float) (cx + (r - 2 * density) * Math.cos(a));
                float y1 = (float) (cy + (r - 2 * density) * Math.sin(a));
                float x2 = (float) (cx + (r + 5 * density) * Math.cos(a));
                float y2 = (float) (cy + (r + 5 * density) * Math.sin(a));
                canvas.drawLine(x1, y1, x2, y2, whiteStroke);
            }

        } else {
            // Adaptive Icon foreground or clean monogram
            if (fallbackIcon instanceof AdaptiveIconDrawable) {
                Drawable fg = ((AdaptiveIconDrawable) fallbackIcon).getForeground();
                if (fg != null) {
                    fg.setBounds((int) (cx - size * 0.38f), (int) (cy - size * 0.38f),
                            (int) (cx + size * 0.38f), (int) (cy + size * 0.38f));
                    fg.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    fg.draw(canvas);
                    return;
                }
            }
            String text = label.length() >= 2 ? label.substring(0, 2).toUpperCase() : label.toUpperCase();
            textPaint.setTextSize(size * 0.50f);
            textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            textPaint.setColor(primaryColor);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(text, cx, baseline, textPaint);
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float outerR, float innerR) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? outerR : innerR;
            double angle = i * Math.PI / 5.0 - Math.PI / 2.0;
            float x = (float) (cx + r * Math.cos(angle));
            float y = (float) (cy + r * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, whiteFill);
    }

    @Override
    public void setAlpha(int alpha) {
        whiteFill.setAlpha(alpha);
        whiteStroke.setAlpha(alpha);
        redFill.setAlpha(alpha);
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        whiteFill.setColorFilter(colorFilter);
        whiteStroke.setColorFilter(colorFilter);
        redFill.setColorFilter(colorFilter);
        textPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
