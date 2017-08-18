package com.peter.climb.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.peter.climb.R;

public class RightAlignedHintEdit extends LinearLayout implements OnFocusChangeListener {

  private static final float UNDERBAR_HEIGHT = 4;
  private static final String SUPER_STATE_KEY = "super_state_key";
  private static final java.lang.String VALUE_KEY = "value_key";

  private float maxValue;
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

  public long getValue() {
    return Long.valueOf(edit.getText().toString());
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

    float scale = getResources().getDisplayMetrics().density;

    int layoutPaddingTop = (int) (8 * scale);
    int layoutPaddingLeft = (int) (4 * scale);
    int layoutPaddingRight = (int) (4 * scale);
    int layoutPaddingBottom = (int) (8 * scale);
    setPadding(layoutPaddingLeft, layoutPaddingTop, layoutPaddingRight, layoutPaddingBottom);
    setOrientation(LinearLayout.HORIZONTAL);

    edit = new EditText(getContext());
    edit.setBackgroundColor(0x00000000);
    edit.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

    int paddingTop = (int) (16 * scale);
    edit.setPadding(0, paddingTop, 0, 0);
    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    edit.setId(View.generateViewId());

    hint = new TextView(getContext());
    hint.setLabelFor(edit.getId());
    hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    hint.setId(View.generateViewId());

    addView(edit);
    addView(hint);

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
    if (v.getId() == edit.getId()) {
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
