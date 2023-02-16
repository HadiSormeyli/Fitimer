package com.android.mhs.fitimer.ui.custom.stepview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;


import com.android.mhs.fitimer.R;

import java.util.ArrayList;
import java.util.List;


public class VerticalStepViewIndicator extends View {
    private final String TAG_NAME = this.getClass().getSimpleName();

    private int defaultStepIndicatorNum = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

    private float mCompletedLineHeight;
    private float mCircleRadius;

    private Drawable mCompleteIcon;
    private Drawable mDefaultIcon;
    private float mCenterX;
    private float mLeftY;
    private float mRightY;

    private int mStepNum = 0;
    private float mLinePadding;

    private List<Float> mCircleCenterPointPositionList;
    private Paint mUnCompletedPaint;
    private Paint mCompletedPaint;
    private int mUnCompletedLineColor = ContextCompat.getColor(getContext(), R.color.off_white);
    private int mCompletedLineColor = ContextCompat.getColor(getContext(), R.color.off_white);
    private PathEffect mEffects;

    private int mComplectingPosition;
    private Path mPath;

    private OnDrawIndicatorListener mOnDrawListener;
    private Rect mRect;
    private int mHeight;
    private boolean mIsReverseDraw;

    public VerticalStepViewIndicator(Context context) {
        this(context, null);
    }


    public VerticalStepViewIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalStepViewIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnDrawListener(OnDrawIndicatorListener onDrawListener) {
        mOnDrawListener = onDrawListener;
    }

    public float getCircleRadius() {
        return mCircleRadius;
    }

    private void init() {
        mPath = new Path();
        mEffects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);

        mCircleCenterPointPositionList = new ArrayList<>();

        mUnCompletedPaint = new Paint();
        mCompletedPaint = new Paint();
        mUnCompletedPaint.setAntiAlias(true);
        mUnCompletedPaint.setColor(mUnCompletedLineColor);
        mUnCompletedPaint.setStyle(Paint.Style.STROKE);
        mUnCompletedPaint.setStrokeWidth(2);

        mCompletedPaint.setAntiAlias(true);
        mCompletedPaint.setColor(mCompletedLineColor);
        mCompletedPaint.setStyle(Paint.Style.STROKE);
        mCompletedPaint.setStrokeWidth(2);

        mUnCompletedPaint.setPathEffect(mEffects);
        mCompletedPaint.setStyle(Paint.Style.FILL);

        mCompletedLineHeight = 0.05f * defaultStepIndicatorNum;
        mCircleRadius = 0.28f * defaultStepIndicatorNum;
        mLinePadding = 0.85f * defaultStepIndicatorNum;

        mCompleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_radio_button_checked_24);
        mDefaultIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_radio_button_unchecked_24);

        mIsReverseDraw = true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = defaultStepIndicatorNum;
        mHeight = 0;
        if (mStepNum > 0) {
            mHeight = (int) (getPaddingTop() + getPaddingBottom() + mCircleRadius * 2 * mStepNum + (mStepNum - 1) * mLinePadding);
        }
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
        }
        setMeasuredDimension(width, mHeight);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = getWidth() / 2;
        mLeftY = mCenterX - (mCompletedLineHeight / 2);
        mRightY = mCenterX + (mCompletedLineHeight / 2);

        mCircleCenterPointPositionList.add(0, -40F);
        for (int i = 0; i < mStepNum; i++) {
            mCircleCenterPointPositionList.add(88F + (mCircleRadius + i * mCircleRadius * 2 + i * mLinePadding));
        }

        if (mOnDrawListener != null) {
            mOnDrawListener.onDrawIndicator();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOnDrawListener != null) {
            mOnDrawListener.onDrawIndicator();
        }
        mUnCompletedPaint.setColor(mUnCompletedLineColor);
        mCompletedPaint.setColor(mCompletedLineColor);

        if (mIsReverseDraw) {
            canvas.drawRect(mLeftY, mCircleCenterPointPositionList.get(1) + mCircleRadius - 10, mRightY, mCircleCenterPointPositionList.get(0) - mCircleRadius + 6, mCompletedPaint);
        } else {
            canvas.drawRect(mLeftY, mCircleCenterPointPositionList.get(0) + mCircleRadius - 10, mRightY, mCircleCenterPointPositionList.get(1) - mCircleRadius + 6, mCompletedPaint);
        }

        for (int i = 1; i < mCircleCenterPointPositionList.size() - 3; i++) {
            final float preComplectedXPosition = mCircleCenterPointPositionList.get(i);
            final float afterComplectedXPosition = mCircleCenterPointPositionList.get(i + 1);


            if (i < mComplectingPosition) {
                if (mIsReverseDraw) {
                    canvas.drawRect(mLeftY, afterComplectedXPosition + mCircleRadius - 8, mRightY, preComplectedXPosition - mCircleRadius + 6, mCompletedPaint);
                } else {
                    if (i == mCircleCenterPointPositionList.size() - 4) {
                        canvas.drawRect(mLeftY, preComplectedXPosition + mCircleRadius - 8, mRightY, mHeight, mCompletedPaint);
                    } else
                        canvas.drawRect(mLeftY, preComplectedXPosition + mCircleRadius - 8, mRightY, afterComplectedXPosition - mCircleRadius + 6, mCompletedPaint);
                }
            } else {
                if (mIsReverseDraw) {
                    mPath.moveTo(mCenterX, afterComplectedXPosition + mCircleRadius);
                    mPath.lineTo(mCenterX, preComplectedXPosition - mCircleRadius);
                } else {
                    mPath.moveTo(mCenterX, preComplectedXPosition + mCircleRadius);
                    mPath.lineTo(mCenterX, afterComplectedXPosition - mCircleRadius);
                }

                canvas.drawPath(mPath, mUnCompletedPaint);
            }
        }

        int size = mCircleCenterPointPositionList.size() - 4;
        mPath.moveTo(mCenterX, mCircleCenterPointPositionList.get(size) + mCircleRadius);
        mPath.lineTo(mCenterX, mHeight);
        canvas.drawPath(mPath, mUnCompletedPaint);


        for (int i = 1; i < mCircleCenterPointPositionList.size() - 3; i++) {
            final float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            mRect = new Rect((int) (mCenterX - mCircleRadius), (int) (currentComplectedXPosition - mCircleRadius), (int) (mCenterX + mCircleRadius), (int) (currentComplectedXPosition + mCircleRadius));
            if (i < mComplectingPosition) {
                mCompleteIcon.setBounds(mRect);
                mCompleteIcon.draw(canvas);
            } else if (i == mComplectingPosition && mCircleCenterPointPositionList.size() != 1) {
                mCompletedPaint.setColor(Color.WHITE);
                canvas.drawCircle(mCenterX, currentComplectedXPosition, mCircleRadius * 1.1f, mCompletedPaint);
            } else {
                mDefaultIcon.setBounds(mRect);
                mDefaultIcon.draw(canvas);
            }
        }
    }

    public List<Float> getCircleCenterPointPositionList() {
        return mCircleCenterPointPositionList;
    }

    public void setStepNum(int stepNum) {
        this.mStepNum = stepNum;
        requestLayout();
    }

    public void setIndicatorLinePaddingProportion(float linePaddingProportion) {
        this.mLinePadding = linePaddingProportion * defaultStepIndicatorNum;
    }

    public void setCompletingPosition(int completingPosition) {
        this.mComplectingPosition = completingPosition;
        requestLayout();
    }

    public void setUnCompletedLineColor(int unCompletedLineColor) {
        this.mUnCompletedLineColor = unCompletedLineColor;
    }

    public void setCompletedLineColor(int completedLineColor) {
        this.mCompletedLineColor = completedLineColor;
    }

    public void reverseDraw(boolean isReverseDraw) {
        this.mIsReverseDraw = isReverseDraw;
        invalidate();
    }

    public void setDefaultIcon(Drawable defaultIcon) {
        this.mDefaultIcon = defaultIcon;
    }

    public void setCompleteIcon(Drawable completeIcon) {
        this.mCompleteIcon = completeIcon;
    }

    public interface OnDrawIndicatorListener {
        void onDrawIndicator();
    }
}
