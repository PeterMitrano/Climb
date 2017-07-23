package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import com.peter.Climb.Msgs;
import java.util.ArrayList;
import java.util.List;

public class RouteLabelView extends View {

  private static final float GRADE_FONT_SIZE = 10f;
  private static final float NAME_FONT_SIZE = 6f;
  private static final float SEND_COUNT_FONT_SIZE = 4f;
  private static final float SHOW_GRADE_SCALE = 1.6f;
  private static final float SHOW_NAME_SCALE = 2.6f;
  private static final float MIN_SIZE = 10f;
  private static final float PADDING = 10f;
  private static final float GRADE_NAME_PADDING = 2f;
  private static final float SENDS_RECT_PADDING = 2f;
  private int routeGrade;
  private Msgs.Point2D position;
  private String routeName;
  private final Paint gradePaint;
  private final Paint namePaint;
  private final Paint markerPaint;
  private final Paint sendCountBubblePaint;
  private final Paint sendCountPaint;
  private final Rect gradeRect;
  private final Rect nameRect;
  private final Rect sendsRect;
  private float metersToPixels;
  private float scaleFactor = 1.f;
  private float x1;
  private float y1;
  private float x2;
  private float y2;
  private int routeColor;
  private List<RouteClickedListener> routeClickedListeners;
  private boolean routeOwnsEvent;
  private int sends;

  public RouteLabelView(Context context) {
    super(context);

    sends = 0;

    routeClickedListeners = new ArrayList<>();

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(Color.LTGRAY);

    gradeRect = new Rect();
    nameRect = new Rect();
    sendsRect = new Rect();

    gradePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gradePaint.setColor(Color.BLACK);

    namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    namePaint.setColor(Color.BLACK);

    sendCountBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    sendCountBubblePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

    sendCountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    sendCountPaint.setColor(Color.WHITE);

    setElevation(2);

    setWillNotDraw(false);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float cx = this.position.getX() * metersToPixels;
    float cy = this.position.getY() * metersToPixels;

    String gradeString = toGradeString(routeGrade);
    String sendsString = String.valueOf(sends);
    gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);
    namePaint.getTextBounds(routeName, 0, routeName.length(), nameRect);
    sendCountPaint.getTextBounds(sendsString, 0, sendsString.length(), sendsRect);

    float labelW = MIN_SIZE;
    float labelH = MIN_SIZE;
    if (scaleFactor > SHOW_NAME_SCALE) {
      labelW = Math.max(Math.max(gradeRect.width(), nameRect.width()), MIN_SIZE) + PADDING;
      labelH =
          Math.max(gradeRect.height() + nameRect.height(), MIN_SIZE) + PADDING + GRADE_NAME_PADDING;
    } else if (scaleFactor > SHOW_GRADE_SCALE) {
      labelW = Math.max(gradeRect.width(), MIN_SIZE) + PADDING;
      labelH = Math.max(gradeRect.height(), MIN_SIZE) + PADDING;
    }

    x1 = cx - labelW / 2f;
    y1 = cy - labelH / 2f;
    x2 = cx + labelW / 2f;
    y2 = cy + labelH / 2f;

    float sendCountRadius = Math.max(sendsRect.width(), sendsRect.height()) + SENDS_RECT_PADDING;
    float sendCountCx = x2;
    float sendCountCy = y1;

    canvas.drawRect(x1, y1, x2, y2, markerPaint);

    if (scaleFactor > SHOW_NAME_SCALE) {
      canvas.drawText(gradeString, cx - gradeRect.exactCenterX(),
          y1 + PADDING / 2f + gradeRect.height(), gradePaint);
      canvas.drawText(routeName, cx - nameRect.exactCenterX(), y2 - PADDING / 2f,
          namePaint);
    } else if (scaleFactor > SHOW_GRADE_SCALE) {
      canvas.drawText(gradeString, cx - gradeRect.exactCenterX(), cy - gradeRect.exactCenterY(),
          gradePaint);
    }

    if (sends > 0) {
      canvas.drawCircle(sendCountCx, sendCountCy, sendCountRadius, sendCountBubblePaint);
      canvas.drawText(sendsString, sendCountCx - sendsRect.exactCenterX(),
          sendCountCy - sendsRect.exactCenterY(), sendCountPaint);
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
    sendCountPaint.setTextSize(SEND_COUNT_FONT_SIZE * scaleFactor);
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
        if (within(ev.getX(), ev.getY()) && routeOwnsEvent && scaleFactor > SHOW_GRADE_SCALE) {
          onRouteClicked();
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

  private void onRouteClicked() {
    markerPaint.setColor(routeColor);
    sends++;

    invalidate();

    // Dispatch to parents who are listening
    for (RouteClickedListener listener : routeClickedListeners) {
      listener.onRouteClicked(this);
    }
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
