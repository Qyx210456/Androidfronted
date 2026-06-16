package com.example.androidfronted.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.core.content.ContextCompat;

import com.example.androidfronted.R;

import java.lang.reflect.Field;

public class TermNumberPicker extends NumberPicker {

    private static final String TAG = "TermNumberPicker";
    private int selectedTextColor;
    private int unselectedTextColor;
    private Handler handler;
    private Runnable invalidateRunnable;
    private boolean reflectionAvailable = true;

    public TermNumberPicker(Context context) {
        super(context);
        init(context);
    }

    public TermNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TermNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        selectedTextColor = ContextCompat.getColor(context, R.color.number_amount);
        unselectedTextColor = ContextCompat.getColor(context, R.color.text_quaternary);
        
        handler = new Handler(Looper.getMainLooper());
        invalidateRunnable = () -> {
            if (reflectionAvailable) {
                setSelectorWheelPaintColor();
                setInputTextColor();
            }
            invalidate();
        };
        
        setWrapSelectorWheel(false);
        setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setSelectionDividerHeight(0);
        
        try {
            Field selectionDividerField = NumberPicker.class.getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(this, null);
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "mSelectionDivider field not found, reflection disabled");
            reflectionAvailable = false;
        } catch (Exception e) {
            Log.w(TAG, "Failed to set mSelectionDivider to null: " + e.getMessage());
            reflectionAvailable = false;
        }
        
        setOnScrollListener((picker, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                handler.removeCallbacks(invalidateRunnable);
                handler.postDelayed(invalidateRunnable, 50);
            }
        });
        
        setOnValueChangedListener((picker, oldVal, newVal) -> {
            handler.removeCallbacks(invalidateRunnable);
            handler.postDelayed(invalidateRunnable, 50);
        });
    }

    private void setSelectorWheelPaintColor() {
        if (!reflectionAvailable) return;
        
        try {
            Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            Paint paint = (Paint) selectorWheelPaintField.get(this);
            if (paint != null) {
                paint.setColor(unselectedTextColor);
            }
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "mSelectorWheelPaint field not found, reflection disabled");
            reflectionAvailable = false;
        } catch (Exception e) {
            Log.w(TAG, "Failed to set selector wheel paint color: " + e.getMessage());
            reflectionAvailable = false;
        }
    }

    private void setInputTextColor() {
        if (!reflectionAvailable) return;
        
        try {
            Field inputTextField = NumberPicker.class.getDeclaredField("mInputText");
            inputTextField.setAccessible(true);
            EditText inputText = (EditText) inputTextField.get(this);
            if (inputText != null) {
                inputText.setTextColor(selectedTextColor);
                inputText.setTextSize(20);
                inputText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                inputText.setVisibility(VISIBLE);
            }
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "mInputText field not found, reflection disabled");
            reflectionAvailable = false;
        } catch (Exception e) {
            Log.w(TAG, "Failed to set input text color: " + e.getMessage());
            reflectionAvailable = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (reflectionAvailable) {
            setSelectorWheelPaintColor();
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        handler.removeCallbacks(invalidateRunnable);
        handler.postDelayed(invalidateRunnable, 100);
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        handler.removeCallbacks(invalidateRunnable);
        handler.postDelayed(invalidateRunnable, 50);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacks(invalidateRunnable);
        handler.postDelayed(invalidateRunnable, 100);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(invalidateRunnable);
    }
}
