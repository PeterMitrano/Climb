package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.peter.Climb.Msgs;
import java.util.ArrayList;
import java.util.List;

public class RouteLabelView extends View {

  public static final int GRADE_FONT_SIZE = 10;
  public static final int NAME_FONT_SIZE = 6;
  private static final float SHOW_GRADE_SCALE = 1.6f;
  private static final float SHOW_NAME_SCALE = 2.6f;
  public static final float MIN_SIZE = 10f;
  private static final float PADDING = 10;
  private static final float GRADE_NAME_PADDING = 2;
  private final Rect nameRect;
  private int routeGrade;
  private Msgs.Point2D position;
  private String routeName;
  private Paint markerPaint;
  private float metersToPixels;
  private float scaleFactor = 1.f;
  private Paint gradePaint;
  private Paint namePaint;
  private Rect gradeRect;
  private float x1;
  private float y1;
  private float x2;
  private float y2;
  private int routeColor;
  private List<RouteClickedListener> routeClickedListeners;
  private boolean routeOwnsEvent;

  public RouteLabelView(Context context) {
    super(context);

    routeClickedListeners = new ArrayList<>();

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(Color.LTGRAY);

    gradeRect = new Rect();
    nameRect = new Rect();

    gradePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gradePaint.setColor(Color.BLACK);

    namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    namePaint.setColor(Color.BLACK);

    setElevation(2);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float cx = this.position.getX() * metersToPixels;
    float cy = this.position.getY() * metersToPixels;

    String gradeString = toGradeString(routeGrade);
    gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);
    namePaint.getTextBounds(routeName, 0, routeName.length(), nameRect);

    float w = MIN_SIZE;
    float h = MIN_SIZE;
    if (scaleFactor > SHOW_NAME_SCALE) {
      w = Math.max(Math.max(gradeRect.width(), nameRect.width()), MIN_SIZE) + PADDING;
      h = Math.max(gradeRect.height() + nameRect.height(), MIN_SIZE) + PADDING + GRADE_NAME_PADDING;
    } else if (scaleFactor > SHOW_GRADE_SCALE) {
      w = Math.max(gradeRect.width(), MIN_SIZE) + PADDING;
      h = Math.max(gradeRect.height(), MIN_SIZE) + PADDING;
    }
    x1 = cx - w / 2;
    y1 = cy - h / 2;
    x2 = cx + w / 2;
    y2 = cy + h / 2;

    canvas.drawRect(x1, y1, x2, y2, markerPaint);

    if (scaleFactor > SHOW_NAME_SCALE) {
      canvas.drawText(gradeString, cx - gradeRect.exactCenterX(),
          y1 + PADDING / 2 + gradeRect.height(), gradePaint);
      canvas.drawText(routeName, cx - nameRect.exactCenterX(), y2 - PADDING / 2,
          namePaint);
    } else if (scaleFactor > SHOW_GRADE_SCALE) {
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
      routeColor = Color.LTGRAY;
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
    namePaint.setTextSize(NAME_FONT_SIZE * scaleFactor);
    invalidate();
  }

  public boolean handleMotionEvent(MotionEvent ev) {
    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
      // if you press DOWN on the route, the route owns the event
      case MotionEvent.ACTION_DOWN: {
        if (within(ev.getX(), ev.getY())) {
          // change color to show selection
          int highlightedColor = Utils.manipulateColor(routeColor, 0.7f);
          markerPaint.setColor(highlightedColor);
          invalidate();
          routeOwnsEvent = true;
          return true;
        }
        break;
      }
      case MotionEvent.ACTION_UP: {
        if (within(ev.getX(), ev.getY()) && routeOwnsEvent) {

          // and actual click happened
          for (RouteClickedListener listener : routeClickedListeners) {
            listener.onRouteClicked(this);
          }

          markerPaint.setColor(routeColor);
          invalidate();
        }

        // release ownership of this event
        if (routeOwnsEvent) {
          routeOwnsEvent = false;
        }
        break;
      }
      default: {
        if (routeOwnsEvent) {
          if (!within(ev.getX(), ev.getY())) {
            markerPaint.setColor(routeColor);
            invalidate();
          }

          return true;
        }
        break;
      }
    }

    return false;
  }

  private boolean within(float x, float y) {
    return (x1 < x && x < x2) && (y1 < y && y < y2);
  }

  public void addRouteClickedListener(RouteClickedListener listener) {
    routeClickedListeners.add(listener);
  }

  interface RouteClickedListener {

    void onRouteClicked(RouteLabelView view);
  }
}
