/*
 * The supported modes and corresponding restrictions are listed as following,
 * Single dot with or without lapping, no color gradient, single color, default white;
 * Filling with or without lapping, or with or without color gradient, lapping and color gradient are
 * mutually exclusive, color gradient requies two colors, default both black;
 * Lapping feature requires sweeping angle to be 360 and color array;
 */
package com.gloomy.ShreddingRobot.Widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.gloomy.ShreddingRobot.R;

public class CustomGauge extends View {

    private Paint mPaint;
    private RectF mRect;
    private Matrix mRotationM;
    private BlurMaskFilter blurMask;
    private int mColorPalettesId;
    private int[] mColorPalettes;

    private boolean useLapping, useShader, dualValue;
    private int maxLap;
    private int pointSize;

    private float mTrackWidth, mStrokeWidth;
    private int mTrackColor;
    private String mStrokeCap;

    private int mStartAngle, mSweepAngle;
    private int mStartValue, mEndValue, mValue, mSecValue;
    private float mRectLeft, mRectTop, mRectRight, mRectBottom;
    private int mPointStartColor, mPointEndColor;

    private float paddingLeft, paddingRight, paddingTop, paddingBottom;
    private float height, width, radius;

    public CustomGauge(Context context) {
        super(context);
        init();
    }

    public CustomGauge(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Extracting user defined attributes
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomGauge, 0, 0);
        readPara(a);
        a.recycle();

        if (useLapping) {
            TypedArray ca = context.getResources().obtainTypedArray(
                    R.array.speed_gauge_color);
            mColorPalettes = new int[ca.length()];
            maxLap = ca.length();
            for (int i = 0; i < ca.length(); i++) {
                mColorPalettes[i] = ca.getColor(i, 0);
            }
            ca.recycle();
        } else {
            mColorPalettes = null;
            maxLap = 1;
        }

        init();
    }

    private void readPara(TypedArray a) {
        // Determine modes
        useLapping = a.getBoolean(R.styleable.CustomGauge_useLapping, false);
        useShader = a.getBoolean(R.styleable.CustomGauge_useShader, false);
        dualValue = a.getBoolean(R.styleable.CustomGauge_dualValue, false);
        pointSize = a.getInt(R.styleable.CustomGauge_pointSize, 0);
        useLapping = !useShader && useLapping;

        if (dualValue) {
            useShader = false;
            useLapping = false;
        } else if (useShader) {
            pointSize = 0;
        }

        // Gauge background track and stroke style
        mTrackWidth = a.getDimension(R.styleable.CustomGauge_trackWidth, 10);
        mTrackColor = a.getColor(R.styleable.CustomGauge_trackColor, android.R.color.white);
        mStrokeCap = a.getString(R.styleable.CustomGauge_strokeCap);
        mStrokeWidth = a.getDimension(R.styleable.CustomGauge_strokeWidth, mTrackWidth);
        mStrokeWidth = useLapping || (mStrokeWidth > mTrackWidth) || (mStrokeWidth <= 0) ? mTrackWidth : mStrokeWidth;

        // Scale (from mStartValue to mEndValue)
        mStartValue = a.getInt(R.styleable.CustomGauge_startValue, 0);
        mEndValue = a.getInt(R.styleable.CustomGauge_endValue, 100);

        // StartAngle (clockwise starting from 3 o'clock direction 0, 90, 180, 270)
        // SweepAngle (also clockwise)
        mStartAngle = a.getInt(R.styleable.CustomGauge_startAngle, 0);
        mSweepAngle = a.getInt(R.styleable.CustomGauge_sweepAngle, 360);
        mSweepAngle = mSweepAngle >= 360 ? 360: mSweepAngle;
        mSweepAngle = mSweepAngle <= -360 ? -360: mSweepAngle;
        useLapping = (mSweepAngle==360||mSweepAngle==-360) && useLapping;

        // Get colors
        mPointStartColor = a.getColor(R.styleable.CustomGauge_strokeColor1, android.R.color.darker_gray);
        mPointEndColor = a.getColor(R.styleable.CustomGauge_strokeColor2, android.R.color.black);
//		mColorPalettesId = a.getInt(R.styleable.CustomGauge_colorPalettesId, -1);
        mPointEndColor = !useLapping ? mPointEndColor : mPointStartColor;
        mColorPalettesId = useLapping ? -1 : mColorPalettesId;
    }

    private void init() {
        // Setting main paint
        mPaint = new Paint();
        resetPaint();
        mPaint.setAntiAlias(true);

        // End style, default is BUTT
        if (!TextUtils.isEmpty(mStrokeCap)) {
            if (mStrokeCap.equals("BUTT"))
                mPaint.setStrokeCap(Paint.Cap.BUTT);
            else if (mStrokeCap.equals("ROUND"))
                mPaint.setStrokeCap(Paint.Cap.ROUND);
        } else
            mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStyle(Paint.Style.STROKE);

        mValue = mStartValue;
        blurMask = new BlurMaskFilter(15, BlurMaskFilter.Blur.SOLID );
        mRect = new RectF();
        mRotationM = new Matrix();
    }

    // Reset paint for empty track drawing
    private void resetPaint() {
        if (useLapping) {
            int lap = mValue/mEndValue;
            if (lap == 0 || pointSize!=0) {
                mPaint.setColor(mTrackColor);
            } else {
                mPaint.setColor(mColorPalettes[lap-1]);
            }
        } else {
            mPaint.setColor(mTrackColor);
        }
        mPaint.setStrokeWidth(mTrackWidth);
        mPaint.setShader(null);
        mPaint.setMaskFilter(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Determine location of gauge on the canvas
        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        width = getWidth() - (paddingLeft + paddingRight);
        height = getHeight() - (paddingTop + paddingBottom);
        radius = (width < height ? width / 2 : height / 2);
        mRectLeft = paddingLeft + width / 2 - radius;
        mRectTop = paddingTop + height / 2 - radius;
        mRectRight = mRectLeft + 2*radius;
        mRectBottom = mRectTop + 2*radius;
        mRect.set(mRectLeft, mRectTop, mRectRight, mRectBottom);

        // Determine value and capping maximum value
        if (dualValue) {
            mValue = mValue < mStartValue ? mStartValue : mValue;
            mValue = mValue < mEndValue? mValue : mEndValue;
            mSecValue = mSecValue < mStartValue ? mStartValue : mSecValue;
            mSecValue = mSecValue < mEndValue? mSecValue : mEndValue;
        } else if(useLapping) {
            mValue = mValue < mStartValue ? mStartValue : mValue;
            mValue = mValue < mStartValue+maxLap*(mEndValue-mStartValue)?
                    mValue : mStartValue+maxLap*(mEndValue-mStartValue);
        } else {
            mValue = mValue < mStartValue ? mStartValue : mValue;
            mValue = mValue < mEndValue? mValue : mEndValue;
        }

        // Drawing the empty gauge
        resetPaint();
        canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);

        if (dualValue) {
            mPaint.setColor(mPointEndColor);
            mPaint.setStrokeWidth(mStrokeWidth);
            int secPointAngle = mSweepAngle*(mSecValue - mStartValue)/(mEndValue - mStartValue);
            canvas.drawArc(mRect, mStartAngle, secPointAngle, false, mPaint);
        }

        //Setting main paint
        mPaint.setColor(mPointStartColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setMaskFilter(blurMask);

        if (useShader) {
            int[] shaderColors = {mPointStartColor, mPointEndColor};
            float[] shaderPos = {0.0f, mSweepAngle/360.0f};
            SweepGradient mShader = new SweepGradient (mRectLeft+radius, mRectTop+radius,
                    shaderColors, shaderPos);

            mRotationM.setRotate(mStartAngle, mRectLeft+radius, mRectTop+radius);
            mShader.setLocalMatrix(mRotationM);
            mPaint.setShader(mShader);
        } else if(useLapping) {
            int lap = (mValue-1)/mEndValue;
            mPaint.setColor(mColorPalettes[lap]);
            mValue = mValue - lap*(mEndValue-mStartValue);
        }

        // Drawing the content of gauge
        int pointAngle = mSweepAngle*(mValue - mStartValue)/(mEndValue - mStartValue);
        if (pointSize!=0) {
            canvas.drawArc(mRect, mStartAngle + pointAngle - pointSize/2, pointSize, false, mPaint);
        } else {
            canvas.drawArc(mRect, mStartAngle, pointAngle, false, mPaint);
        }
    }

    public void setValue(int value) {
        mValue = value;
        invalidate();
    }

    public void setValue(int value, int secValue) {
        mValue = value;
        mSecValue = secValue;
        invalidate();
    }

    public int getValue() {
        return mValue;
    }

    public int getMaxLap() {
        return maxLap;
    }

    public int getStartValue() {
        return mStartValue;
    }

    public int getEndValue() {
        return mEndValue;
    }
}
