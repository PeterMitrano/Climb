package com.peter.climb.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.peter.climb.R;

public class RightAlignedHintEdit extends LinearLayout {

  private static final float UNDERBAR_HEIGHT = 4;
  private String inputType;
  private EditText edit;
  private TextView hint;
  private int unfocusColor;
  private int focusColor;

  private int editWidth;

  private String hintText;
  private RectF underbarRect;
  private Paint underbarRectPaint;
  private int viewWidth;
  private int viewHeight;

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
    edit.setWidth(editWidth);
  }
}
