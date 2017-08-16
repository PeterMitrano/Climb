package com.peter.climb.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.peter.Climb.Msgs;
import com.peter.climb.R;
import com.peter.climb.Utils;
import java.util.ArrayList;
import java.util.List;

public class RouteLabelView extends View {

  private static final float GRADE_FONT_SIZE = 14f;
  private static final float NAME_FONT_SIZE = 10f;
  private static final float SEND_COUNT_FONT_SIZE = 4f;
  private static final float SHOW_GRADE_SCALE = 1.5f;
  private static final float SHOW_NAME_SCALE = 3f;
  private static final float MIN_SIZE = 10f;
  private static final float PADDING = 6f;
  private static final float GRADE_NAME_PADDING = 2f;
  private static final float SENDS_RECT_PADDING = 2f;
  private final Paint gradePaint;
  private final Paint namePaint;
  private final Paint markerPaint;
  private final Paint bottomShadowMarkerPaint;
  private final Paint leftShadowMarkerPaint;
  private final Paint sendCountBubblePaint;
  private final Paint sendCountPaint;
  private final Rect gradeRect;
  private final Rect nameRect;
  private final Rect sendsRect;
  private final Paint rightShadowMarkerPaint;
  private final Paint topShadowMarkerPaint;
  private int routeGrade;
  private Msgs.Point position;
  private float metersToPixels;
  private float scaleFactor = 1.f;
  private float x1;
  private float y1;
  private float x2;
  private float y2;
  private int routeColor;
  private List<RouteClickedListener> routeClickedListeners;
  private boolean routeOwnsEvent;
  private int sendCount;
  private long eventStartTime;
  private float cx;
  private float cy;
  private String gradeString;
  private String routeName;
  private String sendsString;

  public RouteLabelView(Context context) {
    super(context);

    // initialize fields
    sendCount = 0;
    gradeString = "";
    routeName = "";
    sendsString = "";
    position = Msgs.Point.newBuilder().build();

    routeClickedListeners = new ArrayList<>();

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(Color.LTGRAY);

    topShadowMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    bottomShadowMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    leftShadowMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    rightShadowMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float sendCountRadius = sendsString.length() * 2 + SENDS_RECT_PADDING;
    float sendCountCx = x2;
    float sendCountCy = y1;

    canvas.drawRect(x1, y1 - 1f, x1, y1, topShadowMarkerPaint);
    canvas.drawRect(x1, y2, x2, y2 + 2f, bottomShadowMarkerPaint);
    canvas.drawRect(x1 - 1f, y1, x1, y2, leftShadowMarkerPaint);
    canvas.drawRect(x2, y1, x2 + 1f, y2, rightShadowMarkerPaint);
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

    if (sendCount > 0) {
      canvas.drawCircle(sendCountCx, sendCountCy, sendCountRadius, sendCountBubblePaint);
      float offsetX = 0.5f + (sendsString.length() * .5f);
      canvas.drawText(sendsString, sendCountCx - offsetX,
          sendCountCy + 1.5f, sendCountPaint);
    }
  }

  @org.jetbrains.annotations.Contract(pure = true)
  private String toGradeString(int routeGrade) {
    return "V" + routeGrade;
  }

  public Msgs.Point getPosition() {
    return this.position;
  }

  public void setPosition(Msgs.Point position) {
    this.position = position;
    onDataChanged();
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
    onDataChanged();
  }

  public int getRouteGrade() {
    return this.routeGrade;
  }

  public void setRouteGrade(int routeGrade) {
    this.routeGrade = routeGrade;
    gradeString = toGradeString(routeGrade);
    onDataChanged();
  }

  public void setRouteColor(String color) {
    try {
      routeColor = Color.parseColor(color);
      markerPaint.setColor(routeColor);
    } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
      routeColor = Color.LTGRAY;
      markerPaint.setColor(Color.LTGRAY);
    }

    int r = (routeColor >> 16) & 0xff;  // extract red
    int g = (routeColor >> 8) & 0xff;  // extract green
    int b = routeColor & 0xff;  // extract blue
    int luma = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b); // per ITU-R BT.709

    if (luma < 40) {
      gradePaint.setColor(Color.WHITE);
      namePaint.setColor(Color.WHITE);
    }
    else {
      gradePaint.setColor(Color.BLACK);
      namePaint.setColor(Color.BLACK);
    }

    onDataChanged();
  }

  public void setMetersToPixels(float metersToPixels) {
    this.metersToPixels = metersToPixels;
    onDataChanged();
  }

  public void setScaleFactor(float scaleFactor) {
    this.scaleFactor = scaleFactor;

    float inverseScaleFactor = (float) Math.pow(scaleFactor, -0.2);

    gradePaint.setTextSize(GRADE_FONT_SIZE * inverseScaleFactor);
    namePaint.setTextSize(NAME_FONT_SIZE * inverseScaleFactor);
    sendCountPaint.setTextSize(SEND_COUNT_FONT_SIZE);
    onDataChanged();
  }

  private void onDataChanged() {
    cx = this.position.getX() * metersToPixels;
    cy = this.position.getY() * metersToPixels;

    sendsString = String.valueOf(sendCount);
    sendCountPaint.getTextBounds(sendsString, 0, sendsString.length(), sendsRect);
//    Log.e(getClass().toString(), sendsRect.exactCenterX() + " , " + sendsRect.exactCenterY());
    namePaint.getTextBounds(routeName, 0, routeName.length(), nameRect);
    gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);

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

    int SHADOW_START = 0x11000000;
    int SHADOW_END = 0x00000000;
    Shader topShader = new LinearGradient(0, y1 - 1f, 0, y1, SHADOW_END, SHADOW_START,
        TileMode.CLAMP);
    Shader bottomShader = new LinearGradient(0, y2, 0, y2 + 2f, SHADOW_START, SHADOW_END,
        TileMode.CLAMP);
    Shader leftShader = new LinearGradient(x1 - 1f, 0, x1, 0, SHADOW_END, SHADOW_START,
        TileMode.CLAMP);
    Shader rightShader = new LinearGradient(x1, 0, x1 + 1f, 0, SHADOW_START, SHADOW_END,
        TileMode.CLAMP);
    bottomShadowMarkerPaint.setShader(bottomShader);
    leftShadowMarkerPaint.setShader(leftShader);
    rightShadowMarkerPaint.setShader(rightShader);
    topShadowMarkerPaint.setShader(topShader);

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
          eventStartTime = System.currentTimeMillis();
          return true;
        }
        break;
      }
      case MotionEvent.ACTION_UP: {
        if (within(ev.getX(), ev.getY()) && routeOwnsEvent && scaleFactor > SHOW_GRADE_SCALE) {
          long eventDuration = System.currentTimeMillis() - eventStartTime;
          if (eventDuration > ViewConfiguration.getLongPressTimeout()) {
            onRouteLongPressed();
          } else {
            onRouteClicked();
          }
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

  int getSendCount() {
    return this.sendCount;
  }

  void setSendCount(int sendCount) {
    this.sendCount = sendCount;
    onDataChanged();
  }

  private void onRouteClicked() {
    markerPaint.setColor(routeColor);
    if (sendCount < 99) {
      sendCount++;

      // Dispatch to parents who are listening
      for (RouteClickedListener listener : routeClickedListeners) {
        listener.onRouteClicked(this);
      }

    }

    onDataChanged();
  }

  private void onRouteLongPressed() {
    markerPaint.setColor(routeColor);
    if (sendCount > 0) {
      sendCount--;

      // Dispatch to parents who are listening
      for (RouteClickedListener listener : routeClickedListeners) {
        listener.onRouteLongPressed(this);
      }
    }

    onDataChanged();
  }

  private boolean within(float x, float y) {
    return (x1 < x && x < x2) && (y1 < y && y < y2);
  }

  public void addRouteClickedListener(RouteClickedListener listener) {
    routeClickedListeners.add(listener);
  }

  interface RouteClickedListener {

    void onRouteClicked(RouteLabelView view);

    void onRouteLongPressed(RouteLabelView view);
  }
}
