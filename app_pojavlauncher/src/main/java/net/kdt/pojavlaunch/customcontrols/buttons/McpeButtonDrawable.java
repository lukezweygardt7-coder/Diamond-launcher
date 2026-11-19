package net.kdt.pojavlaunch.customcontrols.buttons;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

/**
 * Old Minecraft Pocket Edition style button: stone-speckled gray face, light bevel on the top and left,
 * dark bevel on the bottom and right, dark outline, rounded corners. Looks "pushed in" when pressed/activated.
 */
public class McpeButtonDrawable extends Drawable {
    private static final int FACE_ALPHA = 215;
    private static Shader sNoise;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mOuter = new RectF();
    private final RectF mFace = new RectF();
    private final Path mClip = new Path();

    private final int mBaseColor;
    private final float mRadius;
    private final float mBorder;
    private final int mStrokeColor;
    private final float mStrokeWidth;

    private boolean mPressed;
    private Shader mFaceShader;
    private int mAlpha = 255;

    public McpeButtonDrawable(int baseColor, float radiusPx, float borderPx, int strokeColor, float strokePx) {
        mBaseColor = baseColor;
        mRadius = radiusPx;
        mBorder = borderPx;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokePx;
    }

    /** A small grayscale speckle tile, built once. */
    private static Shader noise() {
        if (sNoise == null) {
            Bitmap tile = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            Random random = new Random(7);
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    int g = 70 + random.nextInt(150);
                    tile.setPixel(x, y, Color.argb(255, g, g, g));
                }
            }
            sNoise = new BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        }
        return sNoise;
    }

    private static int shade(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.rgb(r, g, b);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mOuter.set(bounds);
        mFace.set(bounds.left + mBorder, bounds.top + mBorder, bounds.right - mBorder, bounds.bottom - mBorder);
        mClip.reset();
        float faceRadius = Math.max(0, mRadius - mBorder);
        mClip.addRoundRect(mFace, faceRadius, faceRadius, Path.Direction.CW);
        buildFaceShader();
    }

    private void buildFaceShader() {
        int top = shade(mBaseColor, mPressed ? 0.80f : 1.12f);
        int bottom = shade(mBaseColor, mPressed ? 0.62f : 0.78f);
        mFaceShader = new LinearGradient(0, mFace.top, 0, mFace.bottom, top, bottom, Shader.TileMode.CLAMP);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mOuter.isEmpty()) return;
        float faceRadius = Math.max(0, mRadius - mBorder);
        float k = mAlpha / 255f;

        // 1. dark outline
        mPaint.setShader(null);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF1B1B1B);
        mPaint.setAlpha((int) (FACE_ALPHA * k));
        canvas.drawRoundRect(mOuter, mRadius, mRadius, mPaint);

        // 2. face gradient
        mPaint.setShader(mFaceShader);
        mPaint.setAlpha((int) (FACE_ALPHA * k));
        canvas.drawRoundRect(mFace, faceRadius, faceRadius, mPaint);

        // 3. stone speckle
        mPaint.setShader(noise());
        mPaint.setAlpha((int) (34 * k));
        canvas.drawRoundRect(mFace, faceRadius, faceRadius, mPaint);
        mPaint.setShader(null);

        // 4. bevel: light on top/left, dark on bottom/right (swapped when pressed)
        float bevel = Math.max(1.5f, mBorder);
        int light = Color.argb((int) (120 * k), 255, 255, 255);
        int dark = Color.argb((int) (110 * k), 0, 0, 0);
        int topLeft = mPressed ? dark : light;
        int bottomRight = mPressed ? light : dark;
        canvas.save();
        canvas.clipPath(mClip);
        mPaint.setColor(topLeft);
        canvas.drawRect(mFace.left, mFace.top, mFace.right, mFace.top + bevel, mPaint);
        canvas.drawRect(mFace.left, mFace.top, mFace.left + bevel, mFace.bottom, mPaint);
        mPaint.setColor(bottomRight);
        canvas.drawRect(mFace.left, mFace.bottom - bevel, mFace.right, mFace.bottom, mPaint);
        canvas.drawRect(mFace.right - bevel, mFace.top, mFace.right, mFace.bottom, mPaint);
        canvas.restore();

        // 5. optional custom outline from the control's stroke settings
        if (mStrokeWidth > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setColor(mStrokeColor);
            float half = mStrokeWidth / 2f;
            canvas.drawRoundRect(mOuter.left + half, mOuter.top + half, mOuter.right - half, mOuter.bottom - half,
                    Math.max(0, mRadius - half), Math.max(0, mRadius - half), mPaint);
            mPaint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean pressed = false;
        for (int s : state) {
            if (s == android.R.attr.state_pressed || s == android.R.attr.state_activated) {
                pressed = true;
                break;
            }
        }
        if (pressed != mPressed) {
            mPressed = pressed;
            buildFaceShader();
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
