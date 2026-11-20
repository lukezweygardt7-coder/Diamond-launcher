package com.kdt.mcgui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Animated space for the launcher background: a deep indigo sky, slowly drifting purple and blue nebulae,
 * three parallax layers of twinkling stars, a distant ringed planet and an occasional shooting star.
 * Capped at about 30 frames per second and stops animating while the view is off screen, so it stays cheap
 * on weak devices.
 */
public class SpaceBackgroundView extends View {
    private static final int FRAME_MS = 33;
    private static final int[] LAYER_COUNT = {90, 45, 18};
    private static final float[] LAYER_SPEED = {4f, 11f, 26f};       // dp per second, drifting left
    private static final float[] LAYER_SIZE = {0.7f, 1.2f, 2.0f};    // dp radius

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random mRandom = new Random(7);

    private float[][] mX, mY, mPhase, mTint;
    private int mW, mH;
    private float mDensity;

    private Shader mSky, mPlanet, mPlanetGlow;
    private final Shader[] mNebula = new Shader[4];
    private final float[][] mNebulaPos = new float[4][3]; // cx, cy, radius (fractions of width/height/width)
    private final int[][] mNebulaColors = {
            {0x669C4DFF, 0x009C4DFF}, {0x553B6CFF, 0x003B6CFF},
            {0x44D946EF, 0x00D946EF}, {0x4D2F4FFF, 0x002F4FFF}};

    private long mLast = SystemClock.uptimeMillis();
    private float mTime;
    private float mShootX, mShootY, mShootLife = -1f, mNextShoot = 4f;
    private boolean mRunning;

    public SpaceBackgroundView(Context context) { super(context); init(); }
    public SpaceBackgroundView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        mDensity = getResources().getDisplayMetrics().density;
        float[][] pos = {{0.15f, 0.25f, 0.55f}, {0.85f, 0.7f, 0.6f}, {0.55f, 0.1f, 0.4f}, {0.3f, 0.9f, 0.5f}};
        for (int i = 0; i < 4; i++) mNebulaPos[i] = pos[i];
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mW = w;
        mH = h;
        if (w == 0 || h == 0) return;

        mSky = new LinearGradient(0, 0, w * 0.4f, h, new int[]{0xFF04020D, 0xFF0B0626, 0xFF120A3A}, null, Shader.TileMode.CLAMP);
        for (int i = 0; i < 4; i++) {
            float r = mNebulaPos[i][2] * Math.max(w, h);
            mNebula[i] = new RadialGradient(0, 0, r, mNebulaColors[i][0], mNebulaColors[i][1], Shader.TileMode.CLAMP);
        }

        float planetR = Math.min(w, h) * 0.28f;
        mPlanet = new RadialGradient(-planetR * 0.35f, -planetR * 0.35f, planetR * 1.5f,
                new int[]{0xFFB794FF, 0xFF6D4AE8, 0xFF2A2F9E, 0xFF0A0B3A}, new float[]{0f, 0.35f, 0.7f, 1f}, Shader.TileMode.CLAMP);
        mPlanetGlow = new RadialGradient(0, 0, planetR * 1.35f,
                new int[]{0x00000000, 0x558A6BFF, 0x00000000}, new float[]{0.7f, 0.78f, 1f}, Shader.TileMode.CLAMP);

        int layers = LAYER_COUNT.length;
        mX = new float[layers][];
        mY = new float[layers][];
        mPhase = new float[layers][];
        mTint = new float[layers][];
        for (int l = 0; l < layers; l++) {
            int n = LAYER_COUNT[l];
            mX[l] = new float[n];
            mY[l] = new float[n];
            mPhase[l] = new float[n];
            mTint[l] = new float[n];
            for (int i = 0; i < n; i++) {
                mX[l][i] = mRandom.nextFloat() * w;
                mY[l][i] = mRandom.nextFloat() * h;
                mPhase[l][i] = mRandom.nextFloat() * 6.2831f;
                mTint[l][i] = mRandom.nextFloat();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSky == null) return;
        long now = SystemClock.uptimeMillis();
        float dt = Math.min(0.1f, (now - mLast) / 1000f);
        mLast = now;
        mTime += dt;

        // Sky
        mPaint.setShader(mSky);
        canvas.drawRect(0, 0, mW, mH, mPaint);

        // Nebulae, each drifting in a slow loop
        for (int i = 0; i < 4; i++) {
            float driftX = (float) Math.sin(mTime * 0.05f + i * 1.7f) * mW * 0.05f;
            float driftY = (float) Math.cos(mTime * 0.04f + i * 2.3f) * mH * 0.05f;
            canvas.save();
            canvas.translate(mNebulaPos[i][0] * mW + driftX, mNebulaPos[i][1] * mH + driftY);
            mPaint.setShader(mNebula[i]);
            float r = mNebulaPos[i][2] * Math.max(mW, mH);
            canvas.drawRect(-r, -r, r, r, mPaint);
            canvas.restore();
        }

        // Planet with glow and ring, bottom right, partly off screen
        float planetR = Math.min(mW, mH) * 0.28f;
        float px = mW * 0.88f, py = mH * 0.86f;
        canvas.save();
        canvas.translate(px, py);
        mPaint.setShader(mPlanetGlow);
        canvas.drawCircle(0, 0, planetR * 1.35f, mPaint);
        mPaint.setShader(mPlanet);
        canvas.drawCircle(0, 0, planetR, mPaint);
        mPaint.setShader(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(planetR * 0.06f);
        mPaint.setColor(0x66D8C8FF);
        canvas.rotate(-18f);
        canvas.drawOval(-planetR * 1.7f, -planetR * 0.32f, planetR * 1.7f, planetR * 0.32f, mPaint);
        mPaint.setStrokeWidth(planetR * 0.02f);
        mPaint.setColor(0x44FFFFFF);
        canvas.drawOval(-planetR * 1.5f, -planetR * 0.28f, planetR * 1.5f, planetR * 0.28f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.restore();

        // Stars: three layers drifting left at different speeds, twinkling
        mPaint.setShader(null);
        for (int l = 0; l < LAYER_COUNT.length; l++) {
            float speed = LAYER_SPEED[l] * mDensity * dt;
            float size = LAYER_SIZE[l] * mDensity;
            for (int i = 0; i < LAYER_COUNT[l]; i++) {
                float x = mX[l][i] - speed;
                if (x < -size * 4) {
                    x += mW + size * 8;
                    mY[l][i] = mRandom.nextFloat() * mH;
                }
                mX[l][i] = x;

                float twinkle = 0.55f + 0.45f * (float) Math.sin(mTime * (1.2f + l * 0.7f) + mPhase[l][i]);
                int alpha = (int) (255 * twinkle * (l == 0 ? 0.7f : 1f));
                float t = mTint[l][i];
                int rgb = t < 0.15f ? 0xB9C8FF : (t < 0.3f ? 0xE0C8FF : 0xFFFFFF);
                mPaint.setColor((alpha << 24) | rgb);
                canvas.drawCircle(x, mY[l][i], size, mPaint);

                if (l == 2) { // near stars get a soft glow
                    mPaint.setColor(((alpha / 5) << 24) | rgb);
                    canvas.drawCircle(x, mY[l][i], size * 3.2f, mPaint);
                }
            }
        }

        // Shooting star every so often
        mNextShoot -= dt;
        if (mShootLife < 0 && mNextShoot <= 0) {
            mShootLife = 0f;
            mShootX = mW * (0.3f + mRandom.nextFloat() * 0.7f);
            mShootY = mH * mRandom.nextFloat() * 0.5f;
            mNextShoot = 6f + mRandom.nextFloat() * 8f;
        }
        if (mShootLife >= 0) {
            mShootLife += dt;
            float p = mShootLife / 0.9f;
            if (p >= 1f) {
                mShootLife = -1f;
            } else {
                float len = 140 * mDensity;
                float hx = mShootX - p * mW * 0.35f;
                float hy = mShootY + p * mH * 0.35f;
                mPaint.setShader(new LinearGradient(hx, hy, hx + len * 0.8f, hy - len * 0.5f,
                        0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(1.6f * mDensity);
                mPaint.setAlpha((int) (255 * (1f - p)));
                canvas.drawLine(hx, hy, hx + len * 0.8f, hy - len * 0.5f, mPaint);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShader(null);
            }
        }

        if (mRunning) postInvalidateDelayed(FRAME_MS);
    }

    private void start() {
        if (!mRunning) {
            mRunning = true;
            mLast = SystemClock.uptimeMillis();
            postInvalidateOnAnimation();
        }
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); start(); }
    @Override protected void onDetachedFromWindow() { mRunning = false; super.onDetachedFromWindow(); }
    @Override protected void onVisibilityChanged(View changed, int visibility) {
        super.onVisibilityChanged(changed, visibility);
        if (visibility == VISIBLE && isAttachedToWindow()) start(); else mRunning = false;
    }
    @Override public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && isAttachedToWindow()) start(); else if (!hasWindowFocus) mRunning = false;
    }
}
