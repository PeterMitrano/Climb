package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.peter.Climb.Msgs;

public class RouteLabelView extends View {

  public static final int GRADE_FONT_SIZE = 12;
  private static final float SHOW_GRADE_SCALE = 1.6f;
  private static final float CLICKABLE_SCALE = 1.3f;
  private int routeGrade;
  private Msgs.Point2D position;
  private String routeName;
  private Paint markerPaint;
  private float metersToPixels;
  private float scaleFactor = 1.f;
  private Paint gradePaint;
  private Rect gradeRect;
  private float x1;
  private float y1;
  private float x2;
  private float y2;
  private int routeColor;

  public RouteLabelView(Context context) {
    super(context);

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(Color.LTGRAY);

    gradeRect = new Rect();

    gradePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gradePaint.setColor(Color.BLACK);

    setElevation(2);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float cx = this.position.getX() * metersToPixels;
    float cy = this.position.getY() * metersToPixels;
    float size = Math.max(10, 0.15f * metersToPixels * (float) Math.pow(scaleFactor, 3));
    x1 = cx - size / 2;
    y1 = cy - size / 2;
    x2 = cx + size / 2;
    y2 = cy + size / 2;

    canvas.drawRect(x1, y1, x2, y2, markerPaint);

    if (scaleFactor > SHOW_GRADE_SCALE) {
      String gradeString = toGradeString(routeGrade);
      gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);
      canvas.drawText(gradeString, cx - gradeRect.exactCenterX(), cy - gradeRect.exactCenterY(),
          gradePaint);
    }
  }

  private String toGradeString(int routeGrade) {
    return "V" + routeGrade;
  }

  public void setPosition(Msgs.Point2D position) {
    this.position = position;
    invalidate();
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
    invalidate();
  }

  public void setRouteGrade(int routeGrade) {
    this.routeGrade = routeGrade;
    invalidate();
  }

  public void setRouteColor(String color) {
    try {
      routeColor = Color.parseColor(color);
      markerPaint.setColor(routeColor);
    } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
      markerPaint.setColor(Color.LTGRAY);
    }

    invalidate();
  }

  public void setMetersToPixels(float metersToPixels) {
    this.metersToPixels = metersToPixels;
  }

  public void setScaleFactor(float scaleFactor) {
    this.scaleFactor = scaleFactor;

    gradePaint.setTextSize(GRADE_FONT_SIZE * scaleFactor);
    invalidate();
  }

  public boolean handleMotionEvent(MotionEvent ev) {
    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        if (within(ev.getX(), ev.getY())) {
          // change color to show selection
          int highlightedColor = Utils.manipulateColor(routeColor, 0.9f);
          markerPaint.setColor(highlightedColor);

          invalidate();
          return true;
        }
      }
      case MotionEvent.ACTION_UP: {
        if (within(ev.getX(), ev.getY())) {
          markerPaint.setColor(routeColor);

          // indicate the route has been added

          invalidate();
          return true;
        }
      }
    }

    return false;
  }

  private boolean within(float x, float y) {
    return (x1 < x && x < x2) && (y1 < y && y < y2);
  }
}
