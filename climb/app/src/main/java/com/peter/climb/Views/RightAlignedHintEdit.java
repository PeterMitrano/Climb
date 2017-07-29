package com.peter.climb.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.peter.climb.R;

public class RightAlignedHintEdit extends LinearLayout {

  private String inputType;
  private float paddingTop;
  private float paddingBottom;
  private float paddingLeft;
  private float paddingRight;
  private EditText edit;
  private TextView hint;
  private int hintColor;
  private int focusColor;

  private String hintText;

  public RightAlignedHintEdit(Context context) {
    super(context);
    init();
  }

  public RightAlignedHintEdit(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.RightAlignedHintEdit, 0, 0);

    try {
      hintColor = a.getColor(R.styleable.RightAlignedHintEdit_hintColor, 0xffeeeeee);
      focusColor = a.getColor(R.styleable.RightAlignedHintEdit_focusColor, 0xff000000);
      paddingTop = a.getDimension(R.styleable.RightAlignedHintEdit_android_paddingTop, 0);
      paddingBottom = a.getDimension(R.styleable.RightAlignedHintEdit_android_paddingBottom, 0);
      paddingRight = a.getDimension(R.styleable.RightAlignedHintEdit_android_paddingRight, 0);
      paddingLeft = a.getDimension(R.styleable.RightAlignedHintEdit_android_paddingLeft, 0);
      inputType = a.getNonResourceString(R.styleable.RightAlignedHintEdit_android_inputType);
    } finally {
      a.recycle();
    }
    init();
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
    inflate(getContext(), R.layout.right_aligned_hint_edit, this);
    edit = (EditText) findViewById(R.id.edit);
    hint = (TextView) findViewById(R.id.hint);
  }
}
