package com.android.mhs.fitimer.ui.custom.numberpicker;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.core.content.res.ResourcesCompat;


import com.android.mhs.fitimer.R;
import com.android.mhs.fitimer.data.local.prefs.AppPreferencesHelper;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class CustomNumberPicker extends LinearLayout {


    private final int SELECTOR_WHEEL_ITEM_COUNT;

    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;

    private final int SELECTOR_MIDDLE_ITEM_INDEX;

    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;

    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;

    private static final int SNAP_SCROLL_DURATION = 300;

    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;

    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;

    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48;

    private static final int DEFAULT_LAYOUT_RESOURCE_ID = 0;


    private static final int SIZE_UNSPECIFIED = -1;
    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();
    private static final char[] DIGIT_CHARACTERS = new char[]{
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668'
            , '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8'
            , '\u06f9'
    };
    private final ImageButton mIncrementButton;
    private final ImageButton mDecrementButton;
    private final EditText mInputText;
    private final int mSelectionDividersDistance;
    private final int mMinHeight;
    private int mMaxHeight;
    private final int mMinWidth;
    private final boolean mComputeMaxWidth;
    private final int mTextSize;
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<>();
    private final int[] mSelectorIndices;
    private final Paint mSelectorWheelPaint;
    private final Paint mSelectedWheelPaint;
    private final Drawable mVirtualButtonPressedDrawable;
    private final Scroller mFlingScroller;
    private final Scroller mAdjustScroller;
    private final int mSolidColor;
    private final boolean mHasSelectorWheel;
    private final Drawable mSelectionDivider;
    private final int mSelectionDividerHeight;
    private final PressedStateHelper mPressedStateHelper;
    private int mMaxWidth;
    private int mSelectorTextGapHeight;
    private String[] mDisplayedValues;
    private int mMinValue;
    private int mMaxValue;
    private int mValue;
    private OnValueChangeListener mOnValueChangeListener;
    private OnScrollListener mOnScrollListener;
    private Formatter mFormatter;
    private long mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;
    private int mSelectorElementHeight;
    private int mInitialScrollOffset = Integer.MIN_VALUE;
    private int mCurrentScrollOffset;
    private int mPreviousScrollerY;
    private SetSelectionCommand mSetSelectionCommand;
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;
    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;
    private float mLastDownEventY;
    private long mLastDownEventTime;
    private float mLastDownOrMoveEventY;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private boolean mWrapSelectorWheel;

    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private boolean mIngonreMoveEvents;
    private boolean mShowSoftInputOnTap;
    private int mTopSelectionDividerTop;
    private int mBottomSelectionDividerBottom;
    private int mLastHoveredChildVirtualViewId;
    private boolean mIncrementVirtualButtonPressed;
    private boolean mDecrementVirtualButtonPressed;
    private SupportAccessibilityNodeProvider mAccessibilityNodeProvider;
    private int mLastHandledDownDpadKeyCode = -1;
    private boolean mFirstLoad = true;
    private int mContentFullWeight;

    public CustomNumberPicker(Context context) {
        this(context, null);
    }


    public CustomNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CustomNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // process style attributes
        @SuppressLint("CustomViewStyleable")
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyle, 0);
        final int layoutResId = attributesArray.getResourceId(R.styleable.NumberPicker_internalLayout, DEFAULT_LAYOUT_RESOURCE_ID);

        mHasSelectorWheel = (layoutResId != DEFAULT_LAYOUT_RESOURCE_ID);

        SELECTOR_WHEEL_ITEM_COUNT = attributesArray.getInteger(R.styleable.NumberPicker_mSelectorWheelItem, 5);
        SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT / 2;
        mSelectorIndices  = new int[SELECTOR_WHEEL_ITEM_COUNT];

        mSolidColor = attributesArray.getColor(R.styleable.NumberPicker_solidColor, 0);

        mSelectionDivider = attributesArray.getDrawable(R.styleable.NumberPicker_selectionDivider);

        final int defSelectionDividerHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT,
                getResources().getDisplayMetrics());
        mSelectionDividerHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividerHeight, defSelectionDividerHeight);

        final int defSelectionDividerDistance = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE,
                getResources().getDisplayMetrics());

        mSelectionDividersDistance = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividersDistance, defSelectionDividerDistance);

        mTextSize = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_mTextSize, 24);

        final int mSelectedTextSize = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_mSelectedTextSize, 32);

        mMinHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED);

        mMaxHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxHeight, SIZE_UNSPECIFIED);
        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED
                && mMinHeight > mMaxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }

        mMinWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinWidth, SIZE_UNSPECIFIED);

        mMaxWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxWidth, SIZE_UNSPECIFIED);
        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED
                && mMinWidth > mMaxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }

        mComputeMaxWidth = (mMaxWidth == SIZE_UNSPECIFIED);

        mVirtualButtonPressedDrawable = attributesArray.getDrawable(
                R.styleable.NumberPicker_virtualButtonPressedDrawable);

        attributesArray.recycle();

        mPressedStateHelper = new PressedStateHelper();


        setWillNotDraw(!mHasSelectorWheel);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutResId, this, true);

        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(View v) {
                hideSoftInput();
                mInputText.clearFocus();
                changeValueByOne(v.getId() == R.id.np__increment);
            }
        };

        OnLongClickListener onLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                hideSoftInput();
                mInputText.clearFocus();
                postChangeCurrentByOneFromLongPress(v.getId() == R.id.np__increment, 0);
                return true;
            }
        };

        // increment button
        if (!mHasSelectorWheel) {
            mIncrementButton = (ImageButton) findViewById(R.id.np__increment);
            mIncrementButton.setOnClickListener(onClickListener);
            mIncrementButton.setOnLongClickListener(onLongClickListener);
        } else {
            mIncrementButton = null;
        }

        // decrement button
        if (!mHasSelectorWheel) {
            mDecrementButton = (ImageButton) findViewById(R.id.np__decrement);
            mDecrementButton.setOnClickListener(onClickListener);
            mDecrementButton.setOnLongClickListener(onLongClickListener);
        } else {
            mDecrementButton = null;
        }

        // input text
        mInputText = findViewById(R.id.np__numberpicker_input);
        mInputText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mInputText.selectAll();
            } else {
                validateInputTextView(mInputText.getText().toString());
            }
        });

        mInputText.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (mFirstLoad) {
                mContentFullWeight = mInputText.getHeight() ;
                mFirstLoad = false;;
            }

            if (mInputText.getHeight()  >= mContentFullWeight){
                validateInputTextView(mInputText.getText().toString());
            }
        });

        mInputText.setFilters(new InputFilter[]{new InputTextFilter()});

        mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        mInputText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mInputText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectedTextSize);
        // initialize constants
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity()
                / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;

        // create the selector wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(mTextSize);
        ColorStateList colors = mInputText.getTextColors();
        int color = colors.getColorForState(ENABLED_STATE_SET, Color.WHITE);
        paint.setColor(color);


        Paint paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setTextAlign(Align.CENTER);
        paint2.setTextSize(mSelectedTextSize);
        int color2 = colors.getColorForState(ENABLED_STATE_SET, Color.GRAY);
        paint2.setColor(color2);

        String lan = Locale.getDefault().getLanguage();
        if (lan.equals(AppPreferencesHelper.EN)) {
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.roboto_bold);
            paint.setTypeface(typeface);
            paint2.setTypeface(typeface);
        } else if (lan.equals(AppPreferencesHelper.FA)) {
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.yekan_bakh_fa_bold);
            paint.setTypeface(typeface);
            paint2.setTypeface(typeface);
        }

        mSelectorWheelPaint = paint;
        mSelectedWheelPaint = paint2;
        mInputText.setTypeface(paint2.getTypeface());


        // create the fling and adjust scrollers
        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));

        updateInputTextView();

        // If not explicitly specified this view is important for accessibility.
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public static Formatter getTwoDigitFormatter() {
        return sTwoDigitFormatter;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    public void setSelectedTextSize(int size) {
        mSelectedWheelPaint.setTextSize(size);
    }

    public void setTextSize(int size) {
        mSelectorWheelPaint.setTextSize(size);
    }

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    static private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();

        // Input text centered horizontally.
        final int inptTxtMsrdWdth = mInputText.getMeasuredWidth();
        final int inptTxtMsrdHght = mInputText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel();
            initializeFadingEdges();
            mTopSelectionDividerTop = (getHeight() - mSelectionDividersDistance) / 2
                    - mSelectionDividerHeight;
            mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2 * mSelectionDividerHeight
                    + mSelectionDividersDistance;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Try greedily to fit the max width and height.
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        // Flag if we are measured with width or height less than the respective min.
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, getMeasuredWidth(),
                widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, getMeasuredHeight(),
                heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }


    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementHeight;
        int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight;
                } else {
                    overshootAdjustment += mSelectorElementHeight;
                }
            }
            amountToScroll += overshootAdjustment;
            scrollBy(0, amountToScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            removeAllCallbacks();
            mInputText.setVisibility(View.INVISIBLE);
            mLastDownOrMoveEventY = mLastDownEventY = event.getY();
            mLastDownEventTime = event.getEventTime();
            mIngonreMoveEvents = false;
            mShowSoftInputOnTap = false;
            // Handle pressed state before any state change.
            if (mLastDownEventY < mTopSelectionDividerTop) {
                if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mPressedStateHelper.buttonPressDelayed(
                            PressedStateHelper.BUTTON_DECREMENT);
                }
            } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mPressedStateHelper.buttonPressDelayed(
                            PressedStateHelper.BUTTON_INCREMENT);
                }
            }
            // Make sure we support flinging inside scrollables.
            getParent().requestDisallowInterceptTouchEvent(true);
            if (!mFlingScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } else if (!mAdjustScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
            } else if (mLastDownEventY < mTopSelectionDividerTop) {
                hideSoftInput();
                postChangeCurrentByOneFromLongPress(
                        false, ViewConfiguration.getLongPressTimeout());
            } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                hideSoftInput();
                postChangeCurrentByOneFromLongPress(
                        true, ViewConfiguration.getLongPressTimeout());
            } else {
                mShowSoftInputOnTap = true;
                postBeginSoftInputOnLongPressCommand();
            }
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mHasSelectorWheel) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mIngonreMoveEvents) {
                    break;
                }
                float currentMoveY = event.getY();
                if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks();
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                } else {
                    int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                    scrollBy(0, deltaMoveY);
                    invalidate();
                }
                mLastDownOrMoveEventY = currentMoveY;
            }
            break;
            case MotionEvent.ACTION_UP: {
                removeBeginSoftInputCommand();
                removeChangeCurrentByOneFromLongPress();
                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    int eventY = (int) event.getY();
                    int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                    long deltaTime = event.getEventTime() - mLastDownEventTime;
                    long tapTimeout = ViewConfiguration.getTapTimeout();
                    if (deltaMoveY <= mTouchSlop) { // && deltaTime < ViewConfiguration.getTapTimeout()) {
                        if (mShowSoftInputOnTap) {
                            mShowSoftInputOnTap = false;
                            showSoftInput();
                        } else {
                            int selectorIndexOffset = (eventY / mSelectorElementHeight)
                                    - SELECTOR_MIDDLE_ITEM_INDEX;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                                mPressedStateHelper.buttonTapped(
                                        PressedStateHelper.BUTTON_INCREMENT);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                                mPressedStateHelper.buttonTapped(
                                        PressedStateHelper.BUTTON_DECREMENT);
                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeAllCallbacks();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!mHasSelectorWheel) {
                    break;
                }
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        if (mWrapSelectorWheel || (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                                ? getValue() < getMaxValue() : getValue() > getMinValue()) {
                            requestFocus();
                            mLastHandledDownDpadKeyCode = keyCode;
                            removeAllCallbacks();
                            if (mFlingScroller.isFinished()) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                            return true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1;
                            return true;
                        }
                        break;
                }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (!mHasSelectorWheel) {
            return super.dispatchHoverEvent(event);
        }

        if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
            final int eventY = (int) event.getY();
            final int hoveredVirtualViewId;
            if (eventY < mTopSelectionDividerTop) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_DECREMENT;
            } else if (eventY > mBottomSelectionDividerBottom) {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INCREMENT;
            } else {
                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INPUT;
            }
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            SupportAccessibilityNodeProvider provider = getSupportAccessibilityNodeProvider();

            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER: {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                    provider.performAction(hoveredVirtualViewId,
                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                }
                break;
                case MotionEvent.ACTION_HOVER_MOVE: {
                    if (mLastHoveredChildVirtualViewId != hoveredVirtualViewId
                            && mLastHoveredChildVirtualViewId != View.NO_ID) {
                        provider.sendAccessibilityEventForVirtualView(
                                mLastHoveredChildVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                        provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                                AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                        mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
                        provider.performAction(hoveredVirtualViewId,
                                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    }
                }
                break;
                case MotionEvent.ACTION_HOVER_EXIT: {
                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
                            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
                    mLastHoveredChildVirtualViewId = View.NO_ID;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!mHasSelectorWheel) {
            mIncrementButton.setEnabled(enabled);
        }
        if (!mHasSelectorWheel) {
            mDecrementButton.setEnabled(enabled);
        }
        mInputText.setEnabled(enabled);
    }

    @Override
    public void scrollBy(int x, int y) {
        int[] selectorIndices = mSelectorIndices;
        if (!mWrapSelectorWheel && y > 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        if (!mWrapSelectorWheel && y < 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        mCurrentScrollOffset += y;
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
    }

    @Override
    public int getSolidColor() {
        return mSolidColor;
    }

    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    private void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (mHasSelectorWheel) {
                mInputText.setVisibility(View.VISIBLE);
            }
            mInputText.requestFocus();
            inputMethodManager.showSoftInput(mInputText, 0);
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive(mInputText)) {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            if (mHasSelectorWheel) {
                mInputText.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            final int valueCount = mDisplayedValues.length;
            for (int i = 0; i < valueCount; i++) {
                final float textWidth = mSelectorWheelPaint.measureText(mDisplayedValues[i]);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
        maxTextWidth += mInputText.getPaddingLeft() + mInputText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            if (maxTextWidth > mMinWidth) {
                mMaxWidth = maxTextWidth;
            } else {
                mMaxWidth = mMinWidth;
            }
            invalidate();
        }
    }


    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }


    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        final boolean wrappingAllowed = (mMaxValue - mMinValue) >= mSelectorIndices.length;
        if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != mWrapSelectorWheel) {
            mWrapSelectorWheel = wrapSelectorWheel;
        }
    }


    public void setOnLongPressUpdateInterval(long intervalMillis) {
        mLongPressUpdateInterval = intervalMillis;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        if (mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    public int getMaxValue() {
        return mMaxValue;
    }


    public void setMaxValue(int maxValue) {
        if (mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }

    public void setDisplayedValues(String[] displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        if (mDisplayedValues != null) {
            // Allow text entry rather than strictly numeric entry.
            mInputText.setRawInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        }
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        float x = (getRight() - getLeft()) / 2;
        float y = mCurrentScrollOffset;

        // draw the virtual buttons pressed state if needed
        if (mVirtualButtonPressedDrawable != null
                && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mDecrementVirtualButtonPressed) {
                //mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable.setState(PRESSED_ENABLED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0, 0, getRight(), mTopSelectionDividerTop);
                mVirtualButtonPressedDrawable.draw(canvas);
            }
            if (mIncrementVirtualButtonPressed) {
                //mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable.setState(PRESSED_ENABLED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0, mBottomSelectionDividerBottom, getRight(), getBottom());
                mVirtualButtonPressedDrawable.draw(canvas);
            }
        }

        // draw the selector wheel
        int[] selectorIndices = mSelectorIndices;
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = selectorIndices[i];
            String scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex);
            if (i != SELECTOR_MIDDLE_ITEM_INDEX) {
                canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint);
            } else if (mInputText.getVisibility() != VISIBLE) {
                canvas.drawText(scrollSelectorValue, x, y, mSelectedWheelPaint);
            }
            y += mSelectorElementHeight;
        }

        // draw the selection dividers
        if (mSelectionDivider != null) {
            // draw the top divider
            int topOfTopDivider = mTopSelectionDividerTop;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.draw(canvas);

            // draw the bottom divider
            int bottomOfBottomDivider = mBottomSelectionDividerBottom;
            int topOfBottomDivider = bottomOfBottomDivider - mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.draw(canvas);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CustomNumberPicker.class.getName());
        event.setScrollable(true);
        event.setScrollY((mMinValue + mValue) * mSelectorElementHeight);
        event.setMaxScrollY((mMaxValue - mMinValue) * mSelectorElementHeight);
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (!mHasSelectorWheel) {
            return super.getAccessibilityNodeProvider();
        }
        if (mAccessibilityNodeProvider == null) {
            mAccessibilityNodeProvider = new SupportAccessibilityNodeProvider();
        }
        return mAccessibilityNodeProvider.mProvider;
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    private int resolveSizeAndStateRespectingMinSize(
            int minSize, int measuredSize, int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            return measuredSize;
        }
    }

    private void initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear();
        int[] selectorIndices = mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - SELECTOR_MIDDLE_ITEM_INDEX);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    private void setValueInternal(int current, boolean notifyChange) {
        if (mValue == current) {
            return;
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        } else {
            current = Math.max(current, mMinValue);
            current = Math.min(current, mMaxValue);
        }
        int previous = mValue;
        mValue = current;
        updateInputTextView();
        if (notifyChange) {
            notifyChange(previous, current);
        }
        initializeSelectorWheelIndices();
        invalidate();
    }

    private void changeValueByOne(boolean increment) {
        if (mHasSelectorWheel) {
            mInputText.setVisibility(View.INVISIBLE);
            if (!moveToFinalScrollerPosition(mFlingScroller)) {
                moveToFinalScrollerPosition(mAdjustScroller);
            }
            mPreviousScrollerY = 0;
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementHeight, SNAP_SCROLL_DURATION);
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementHeight, SNAP_SCROLL_DURATION);
            }
            invalidate();
        } else {
            if (increment) {
                setValueInternal(mValue + 1, true);
            } else {
                setValueInternal(mValue - 1, true);
            }
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = mSelectorIndices;
        int totalTextHeight = selectorIndices.length * mTextSize;
        float totalTextGapHeight = (getBottom() - getTop()) - totalTextHeight;
        float textGapCount = selectorIndices.length;
        mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f);
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
        // Ensure that the middle item is positioned the same as the text in
        // mInputText
        int editTextTextPosition = mInputText.getBaseline() + mInputText.getTop();
        mInitialScrollOffset = editTextTextPosition
                - (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength((getBottom() - getTop() - mTextSize) / 2);
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView();
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        } else {
            if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                updateInputTextView();
            }
        }
    }


    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    private void fling(int velocityY) {
        mPreviousScrollerY = 0;

        if (velocityY > 0) {
            mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }

        invalidate();
    }


    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }


    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }


    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                scrollSelectorValue = mDisplayedValues[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value) : formatNumberWithLocale(value);
    }

    private void validateInputTextView(String str) {
        if (TextUtils.isEmpty(str)) {
            // Restore to the old value as we don't allow empty values
            updateInputTextView();
        } else {
            // Check the new value and ensure it's in range
            int current = getSelectedPos(str);
            setValueInternal(current, false);
        }
    }

    private void updateInputTextView() {
        String text = (mDisplayedValues == null) ? formatNumber(mValue)
                : mDisplayedValues[mValue - mMinValue];
        if (!TextUtils.isEmpty(text) && !text.equals(mInputText.getText().toString())) {
            mInputText.setText(text);
        }
    }

    private void notifyChange(int previous, int current) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, mValue);
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    private void removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    private void postBeginSoftInputOnLongPressCommand() {
        if (mBeginSoftInputOnLongPressCommand == null) {
            mBeginSoftInputOnLongPressCommand = new BeginSoftInputOnLongPressCommand();
        } else {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        postDelayed(mBeginSoftInputOnLongPressCommand, ViewConfiguration.getLongPressTimeout());
    }

    private void removeBeginSoftInputCommand() {
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
    }

    private void removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        if (mSetSelectionCommand != null) {
            removeCallbacks(mSetSelectionCommand);
        }
        if (mBeginSoftInputOnLongPressCommand != null) {
            removeCallbacks(mBeginSoftInputOnLongPressCommand);
        }
        mPressedStateHelper.cancel();
    }

    private int getSelectedPos(String value) {
        // Ignore as if it's not a number we don't care
        if (mDisplayedValues != null) {
            for (int i = 0; i < mDisplayedValues.length; i++) {
                // Don't force the user to type in jan when ja will do
                value = value.toLowerCase();
                if (mDisplayedValues[i].toLowerCase().startsWith(value)) {
                    return mMinValue + i;
                }
            }

        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Ignore as if it's not a number we don't care
        }
        return mMinValue;
    }

    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = new SetSelectionCommand();
        } else {
            removeCallbacks(mSetSelectionCommand);
        }
        mSetSelectionCommand.mSelectionStart = selectionStart;
        mSetSelectionCommand.mSelectionEnd = selectionEnd;
        post(mSetSelectionCommand);
    }

    private boolean ensureScrollWheelAdjusted() {
        // adjust to the closest value
        int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
        if (deltaY != 0) {
            mPreviousScrollerY = 0;
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += (deltaY > 0) ? -mSelectorElementHeight : mSelectorElementHeight;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            invalidate();
            return true;
        }
        return false;
    }

    private SupportAccessibilityNodeProvider getSupportAccessibilityNodeProvider() {
        return new SupportAccessibilityNodeProvider();
    }

    public void hasTwoDigitFormatter() {
        setFormatter(sTwoDigitFormatter);
    }

    public interface OnValueChangeListener {
        void onValueChange(CustomNumberPicker picker, int oldVal, int newVal);
    }


    public interface OnScrollListener {

        int SCROLL_STATE_IDLE = 0;
        int SCROLL_STATE_TOUCH_SCROLL = 1;

        int SCROLL_STATE_FLING = 2;

        void onScrollStateChange(CustomNumberPicker view, int scrollState);
    }


    public interface Formatter {
        String format(int value);
    }

    private static class TwoDigitFormatter implements Formatter {
        final StringBuilder mBuilder = new StringBuilder();
        final Object[] mArgs = new Object[1];
        char mZeroDigit;
        java.util.Formatter mFmt;

        TwoDigitFormatter() {
            final Locale locale = Locale.getDefault();
            init(locale);
        }

        private static char getZeroDigit(Locale locale) {
            // return LocaleData.get(locale).zeroDigit;
            return new DecimalFormatSymbols(locale).getZeroDigit();
        }

        private void init(Locale locale) {
            mFmt = createFormatter(locale);
            mZeroDigit = getZeroDigit(locale);
        }

        public String format(int value) {
            final Locale currentLocale = Locale.getDefault();
            if (mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale);
            }
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(mBuilder, locale);
        }
    }

    public static class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                clearFocus();
            }
        }
    }

    class InputTextFilter extends NumberKeyListener {

        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT;
        }

        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }

        @Override
        public CharSequence filter(
                CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (mDisplayedValues == null) {
                CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }

                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());

                if ("".equals(result)) {
                    return result;
                }
                int val = getSelectedPos(result);

                if (val > mMaxValue) {
                    return "";
                } else {
                    return filtered;
                }
            } else {
                CharSequence filtered = String.valueOf(source.subSequence(start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());
                String str = String.valueOf(result).toLowerCase();
                for (String val : mDisplayedValues) {
                    String valLowerCase = val.toLowerCase();
                    if (valLowerCase.startsWith(str)) {
                        postSetSelectionCommand(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                }
                return "";
            }
        }
    }

    class PressedStateHelper implements Runnable {
        public static final int BUTTON_INCREMENT = 1;
        public static final int BUTTON_DECREMENT = 2;

        private final int MODE_PRESS = 1;
        private final int MODE_TAPPED = 2;

        private int mManagedButton;
        private int mMode;

        public void cancel() {
            mMode = 0;
            mManagedButton = 0;
            CustomNumberPicker.this.removeCallbacks(this);
            if (mIncrementVirtualButtonPressed) {
                mIncrementVirtualButtonPressed = false;
                invalidate(0, mBottomSelectionDividerBottom, getRight(), getBottom());
            }
            mDecrementVirtualButtonPressed = false;
            if (mDecrementVirtualButtonPressed) {
                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
            }
        }

        public void buttonPressDelayed(int button) {
            cancel();
            mMode = MODE_PRESS;
            mManagedButton = button;
            CustomNumberPicker.this.postDelayed(this, ViewConfiguration.getTapTimeout());
        }

        public void buttonTapped(int button) {
            cancel();
            mMode = MODE_TAPPED;
            mManagedButton = button;
            CustomNumberPicker.this.post(this);
        }

        @Override
        public void run() {
            switch (mMode) {
                case MODE_PRESS: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            mIncrementVirtualButtonPressed = true;
                            invalidate(0, mBottomSelectionDividerBottom, getRight(), getBottom());
                        }
                        break;
                        case BUTTON_DECREMENT: {
                            mDecrementVirtualButtonPressed = true;
                            invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                        }
                    }
                }
                break;
                case MODE_TAPPED: {
                    switch (mManagedButton) {
                        case BUTTON_INCREMENT: {
                            if (!mIncrementVirtualButtonPressed) {
                                CustomNumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mIncrementVirtualButtonPressed ^= true;
                            invalidate(0, mBottomSelectionDividerBottom, getRight(), getBottom());
                        }
                        break;
                        case BUTTON_DECREMENT: {
                            if (!mDecrementVirtualButtonPressed) {
                                CustomNumberPicker.this.postDelayed(this,
                                        ViewConfiguration.getPressedStateDuration());
                            }
                            mDecrementVirtualButtonPressed ^= true;
                            invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                        }
                    }
                }
                break;
            }
        }
    }

    class SetSelectionCommand implements Runnable {
        private int mSelectionStart;

        private int mSelectionEnd;

        public void run() {
            mInputText.setSelection(mSelectionStart, mSelectionEnd);
        }
    }

    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        private void setStep(boolean increment) {
            mIncrement = increment;
        }

        @Override
        public void run() {
            changeValueByOne(mIncrement);
            postDelayed(this, mLongPressUpdateInterval);
        }
    }

    class BeginSoftInputOnLongPressCommand implements Runnable {

        @Override
        public void run() {
            showSoftInput();
            mIngonreMoveEvents = true;
        }
    }

    class SupportAccessibilityNodeProvider {

        AccessibilityNodeProviderImpl mProvider;

        private SupportAccessibilityNodeProvider() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mProvider = new AccessibilityNodeProviderImpl();
            }
        }

        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            if (mProvider != null) {
                return mProvider.performAction(virtualViewId, action, arguments);
            }

            return false;
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            if (mProvider != null)
                mProvider.sendAccessibilityEventForVirtualView(virtualViewId, eventType);
        }
    }

    class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int UNDEFINED = Integer.MIN_VALUE;

        private static final int VIRTUAL_VIEW_ID_INCREMENT = 1;

        private static final int VIRTUAL_VIEW_ID_INPUT = 2;

        private static final int VIRTUAL_VIEW_ID_DECREMENT = 3;

        private final Rect mTempRect = new Rect();

        private final int[] mTempArray = new int[2];

        private int mAccessibilityFocusedView = UNDEFINED;

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            switch (virtualViewId) {
                case View.NO_ID:
                    return createAccessibilityNodeInfoForNumberPicker(getScrollX(), getScrollY(),
                            getScrollX() + (getRight() - getLeft()), getScrollY() + (getBottom() - getTop()));
                case VIRTUAL_VIEW_ID_DECREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_DECREMENT,
                            getVirtualDecrementButtonText(), getScrollX(), getScrollY(),
                            getScrollX() + (getRight() - getLeft()),
                            mTopSelectionDividerTop + mSelectionDividerHeight);
                case VIRTUAL_VIEW_ID_INPUT:
                    return createAccessibiltyNodeInfoForInputText();
                case VIRTUAL_VIEW_ID_INCREMENT:
                    return createAccessibilityNodeInfoForVirtualButton(VIRTUAL_VIEW_ID_INCREMENT,
                            getVirtualIncrementButtonText(), getScrollX(),
                            mBottomSelectionDividerBottom - mSelectionDividerHeight,
                            getScrollX() + (getRight() - getLeft()), getScrollY() + (getBottom() - getTop()));
            }
            return super.createAccessibilityNodeInfo(virtualViewId);
        }

        @Override
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String searched,
                                                                            int virtualViewId) {
            if (TextUtils.isEmpty(searched)) {
                return Collections.emptyList();
            }
            String searchedLowerCase = searched.toLowerCase();
            List<AccessibilityNodeInfo> result = new ArrayList<AccessibilityNodeInfo>();
            switch (virtualViewId) {
                case View.NO_ID: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_DECREMENT, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_INPUT, result);
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase,
                            VIRTUAL_VIEW_ID_INCREMENT, result);
                    return result;
                }
                case VIRTUAL_VIEW_ID_DECREMENT:
                case VIRTUAL_VIEW_ID_INCREMENT:
                case VIRTUAL_VIEW_ID_INPUT: {
                    findAccessibilityNodeInfosByTextInChild(searchedLowerCase, virtualViewId,
                            result);
                    return result;
                }
            }
            return super.findAccessibilityNodeInfosByText(searched, virtualViewId);
        }

        @Override
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            switch (virtualViewId) {
                case View.NO_ID: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                // requestAccessibilityFocus();
                                performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                // clearAccessibilityFocus();
                                performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS, null);
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                            if (CustomNumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() < getMaxValue())) {
                                changeValueByOne(true);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                            if (CustomNumberPicker.this.isEnabled()
                                    && (getWrapSelectorWheel() || getValue() > getMinValue())) {
                                changeValueByOne(false);
                                return true;
                            }
                        }
                        return false;
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_FOCUS: {
                            if (CustomNumberPicker.this.isEnabled() && !mInputText.isFocused()) {
                                return mInputText.requestFocus();
                            }
                        }
                        break;
                        case AccessibilityNodeInfo.ACTION_CLEAR_FOCUS: {
                            if (CustomNumberPicker.this.isEnabled() && mInputText.isFocused()) {
                                mInputText.clearFocus();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (CustomNumberPicker.this.isEnabled()) {
                                showSoftInput();
                                return true;
                            }
                            return false;
                        }
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                mInputText.invalidate();
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                mInputText.invalidate();
                                return true;
                            }
                        }
                        return false;
                        default: {
                            return mInputText.performAccessibilityAction(action, arguments);
                        }
                    }
                }
                return false;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (CustomNumberPicker.this.isEnabled()) {
                                CustomNumberPicker.this.changeValueByOne(true);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate(0, mBottomSelectionDividerBottom, getRight(), getBottom());
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate(0, mBottomSelectionDividerBottom, getRight(), getBottom());
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return false;
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    switch (action) {
                        case AccessibilityNodeInfo.ACTION_CLICK: {
                            if (CustomNumberPicker.this.isEnabled()) {
                                final boolean increment = (virtualViewId == VIRTUAL_VIEW_ID_INCREMENT);
                                CustomNumberPicker.this.changeValueByOne(increment);
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_CLICKED);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView != virtualViewId) {
                                mAccessibilityFocusedView = virtualViewId;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        }
                        return false;
                        case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS: {
                            if (mAccessibilityFocusedView == virtualViewId) {
                                mAccessibilityFocusedView = UNDEFINED;
                                sendAccessibilityEventForVirtualView(virtualViewId,
                                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                                invalidate(0, 0, getRight(), mTopSelectionDividerTop);
                                return true;
                            }
                        }
                        return false;
                    }
                }
                return false;
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        public void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    if (hasVirtualDecrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                                getVirtualDecrementButtonText());
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INPUT: {
                    sendAccessibilityEventForVirtualText(eventType);
                }
                break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    if (hasVirtualIncrementButton()) {
                        sendAccessibilityEventForVirtualButton(virtualViewId, eventType,
                                getVirtualIncrementButtonText());
                    }
                }
                break;
            }
        }

        private void sendAccessibilityEventForVirtualText(int eventType) {
            if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                mInputText.onInitializeAccessibilityEvent(event);
                mInputText.onPopulateAccessibilityEvent(event);
                event.setSource(CustomNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
                requestSendAccessibilityEvent(CustomNumberPicker.this, event);
            }
        }

        private void sendAccessibilityEventForVirtualButton(int virtualViewId, int eventType,
                                                            String text) {
            if (((AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
                event.setClassName(Button.class.getName());
                event.setPackageName(getContext().getPackageName());
                event.getText().add(text);
                event.setEnabled(CustomNumberPicker.this.isEnabled());
                event.setSource(CustomNumberPicker.this, virtualViewId);
                requestSendAccessibilityEvent(CustomNumberPicker.this, event);
            }
        }

        private void findAccessibilityNodeInfosByTextInChild(String searchedLowerCase,
                                                             int virtualViewId, List<AccessibilityNodeInfo> outResult) {
            switch (virtualViewId) {
                case VIRTUAL_VIEW_ID_DECREMENT: {
                    String text = getVirtualDecrementButtonText();
                    if (!TextUtils.isEmpty(text)
                            && text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_DECREMENT));
                    }
                }
                return;
                case VIRTUAL_VIEW_ID_INPUT: {
                    CharSequence text = mInputText.getText();
                    if (!TextUtils.isEmpty(text) &&
                            text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                        return;
                    }
                    CharSequence contentDesc = mInputText.getText();
                    if (!TextUtils.isEmpty(contentDesc) &&
                            contentDesc.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INPUT));
                        return;
                    }
                }
                break;
                case VIRTUAL_VIEW_ID_INCREMENT: {
                    String text = getVirtualIncrementButtonText();
                    if (!TextUtils.isEmpty(text)
                            && text.toString().toLowerCase().contains(searchedLowerCase)) {
                        outResult.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_INCREMENT));
                    }
                }
                return;
            }
        }

        private AccessibilityNodeInfo createAccessibiltyNodeInfoForInputText() {
            AccessibilityNodeInfo info = mInputText.createAccessibilityNodeInfo();
            info.setSource(CustomNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            if (mAccessibilityFocusedView != VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == VIRTUAL_VIEW_ID_INPUT) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForVirtualButton(int virtualViewId,
                                                                                  String text, int left, int top, int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(Button.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(CustomNumberPicker.this, virtualViewId);
            info.setParent(CustomNumberPicker.this);
            info.setText(text);
            info.setClickable(true);
            info.setLongClickable(true);
            info.setEnabled(CustomNumberPicker.this.isEnabled());
            Rect boundsInParent = mTempRect;
            boundsInParent.set(left, top, right, bottom);
            // TODO info.setVisibleToUser(isVisibleToUser(boundsInParent));
            info.setBoundsInParent(boundsInParent);
            Rect boundsInScreen = boundsInParent;
            int[] locationOnScreen = mTempArray;
            getLocationOnScreen(locationOnScreen);
            boundsInScreen.offset(locationOnScreen[0], locationOnScreen[1]);
            info.setBoundsInScreen(boundsInScreen);

            if (mAccessibilityFocusedView != virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == virtualViewId) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (CustomNumberPicker.this.isEnabled()) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            return info;
        }

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(int left, int top,
                                                                                 int right, int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(CustomNumberPicker.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(CustomNumberPicker.this);

            if (hasVirtualDecrementButton()) {
                info.addChild(CustomNumberPicker.this, VIRTUAL_VIEW_ID_DECREMENT);
            }
            info.addChild(CustomNumberPicker.this, VIRTUAL_VIEW_ID_INPUT);
            if (hasVirtualIncrementButton()) {
                info.addChild(CustomNumberPicker.this, VIRTUAL_VIEW_ID_INCREMENT);
            }

            info.setParent((View) getParentForAccessibility());
            info.setEnabled(CustomNumberPicker.this.isEnabled());
            info.setScrollable(true);

            if (mAccessibilityFocusedView != View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (mAccessibilityFocusedView == View.NO_ID) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (CustomNumberPicker.this.isEnabled()) {
                if (getWrapSelectorWheel() || getValue() < getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                }
                if (getWrapSelectorWheel() || getValue() > getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                }
            }

            return info;
        }

        private boolean hasVirtualDecrementButton() {
            return getWrapSelectorWheel() || getValue() > getMinValue();
        }

        private boolean hasVirtualIncrementButton() {
            return getWrapSelectorWheel() || getValue() < getMaxValue();
        }

        private String getVirtualDecrementButtonText() {
            int value = mValue - 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value >= mMinValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }

        private String getVirtualIncrementButtonText() {
            int value = mValue + 1;
            if (mWrapSelectorWheel) {
                value = getWrappedSelectorIndex(value);
            }
            if (value <= mMaxValue) {
                return (mDisplayedValues == null) ? formatNumber(value)
                        : mDisplayedValues[value - mMinValue];
            }
            return null;
        }
    }
}