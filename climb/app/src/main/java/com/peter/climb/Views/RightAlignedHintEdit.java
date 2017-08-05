package com.peter.climb.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.peter.climb.R;

public class RightAlignedHintEdit extends LinearLayout implements OnFocusChangeListener {

  private static final float UNDERBAR_HEIGHT = 4;

  private float maxValue;
  private String inputType;
  private EditText edit;
  private TextView hint;
  private int unfocusColor;
  private int focusColor;
  private int textColor;
  private int editWidth;
  private String hintText;
  private RectF underbarRect;
  private Paint underbarRectPaint;
  private int maxLength;

  public RightAlignedHintEdit(Context context) {
    super(context);
    init();
  }

  public RightAlignedHintEdit(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.RightAlignedHintEdit, 0, 0);

    try {
      focusColor = a.getColor(R.styleable.RightAlignedHintEdit_focusColor, Color.BLACK);
      unfocusColor = a.getColor(R.styleable.RightAlignedHintEdit_unfocusColor, Color.GRAY);
      editWidth = (int) a.getDimension(R.styleable.RightAlignedHintEdit_editWidth, 0f);
      hintText = a.getString(R.styleable.RightAlignedHintEdit_android_hint);
      textColor = a.getColor(R.styleable.RightAlignedHintEdit_android_textColor, Color.WHITE);
      maxValue = a.getFloat(R.styleable.RightAlignedHintEdit_maxValue, 100f);
      maxLength = a.getInt(R.styleable.RightAlignedHintEdit_maxLength, 3);
    } finally {
      a.recycle();
    }
    init();
  }

  @Override
  protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
    super.onSizeChanged(xNew, yNew, xOld, yOld);
    underbarRect.set(getPaddingLeft(), yNew - UNDERBAR_HEIGHT - getPaddingBottom(),
        xNew - getPaddingRight(), yNew - getPaddingBottom());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawRect(underbarRect, underbarRectPaint);
  }

  public float getEditWidth() {
    return editWidth;
  }

  public void setEditWidth(int editWidth) {
    this.editWidth = editWidth;
    edit.setWidth(editWidth);
    invalidate();
    requestLayout();
  }

  public String getHintText() {
    return hintText;
  }

  public void setHintText(String hintText) {
    this.hintText = hintText;
    hint.setText(this.hintText);
    invalidate();
  }

  public int getTextColor() {
    return textColor;
  }

  public void setTextColor(int textColor) {
    this.textColor = textColor;
    edit.setTextColor(textColor);
    hint.setTextColor(textColor);
    invalidate();
  }

  public void setText(String text) {
    edit.setText(text);
    invalidate();
  }

  private void init() {
    setWillNotDraw(false);

    inflate(getContext(), R.layout.right_aligned_hint_edit, this);
    edit = (EditText) findViewById(R.id.edit);
    hint = (TextView) findViewById(R.id.hint);

    underbarRect = new RectF();
    underbarRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    underbarRectPaint.setColor(unfocusColor);

    setHintText(hintText);
    setEditWidth(editWidth);
    setTextColor(textColor);
    edit.setWidth(editWidth);
    edit.setOnFocusChangeListener(this);
    edit.setFilters(new InputFilter[]{new ValueLengthFilter(maxValue, maxLength)});
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (v.getId() == R.id.edit) {
      if (hasFocus) {
        underbarRectPaint.setColor(focusColor);
      } else {
        underbarRectPaint.setColor(unfocusColor);
      }

      invalidate();
    }
  }

  private class ValueLengthFilter implements InputFilter {

    private final float maxValue;
    private final int maxLength;

    ValueLengthFilter(float maxValue, int maxLength) {
      this.maxValue = maxValue;
      this.maxLength = maxLength;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart,
        int dEnd) {
      try {
        // Remove the string out of destination that is to be replaced
        String newVal = dest.toString().substring(0, dStart) + dest.toString()
            .substring(dEnd, dest.toString().length());
        newVal = newVal.substring(0, dStart) + source.toString() + newVal
            .substring(dStart, newVal.length());
        Integer input = Integer.parseInt(newVal);

        if ((input <= maxValue) && (dest.length() <= maxLength)) {
          return null;
        }

      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
      return "";
    }
  }
}
