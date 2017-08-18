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

  private static final int BUBBLE_RADIUS_PX = 10;
  private static final float MIN_SIZE_M = 0.25f;
  final int TEXT_PADDING_PX = 6;
  private final Paint gradePaint;
  private final Paint namePaint;
  private final Paint markerPaint;
  private final Paint bottomShadowMarkerPaint;
  private final Paint leftShadowMarkerPaint;
  private final Paint sendCountBubblePaint;
  private final Paint sendCountPaint;
  private final Paint rightShadowMarkerPaint;
  private final Paint topShadowMarkerPaint;

  private Msgs.Point position;
  private float metersToPixels;
  private float scaleFactor = 1.f;
  private int x1_px;
  private int y1_px;
  private int x2_px;
  private int y2_px;
  private int routeColor;
  private List<RouteClickedListener> routeClickedListeners;
  private boolean routeOwnsEvent;
  private int sendCount;
  private long eventStartTime;
  private int cx_px;
  private int cy_px;
  private String gradeString;
  private String routeName;
  private String sendsString;
  private boolean showName;
  private boolean showGrade;
  private Rect gradeRect;
  private Rect nameRect;
  private Rect sendCountRect;

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

    gradePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gradePaint.setColor(Color.BLACK);

    namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    namePaint.setColor(Color.BLACK);

    sendCountBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    sendCountBubblePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

    sendCountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    sendCountPaint.setColor(Color.WHITE);

    gradeRect = new Rect();
    nameRect = new Rect();
    sendCountRect = new Rect();
  }

  /**
   * Sets the text size for a Paint object so a given string of text will be a
   * given width.
   *
   * @param paint the Paint to set the text size for
   * @param desiredWidthPx the desired width
   * @param text the text that should be that width
   */
  private static void setTextSizeForWidth(Paint paint, int desiredWidthPx,
      String text) {

    final float testTextSizeSp = 96f;

    // Get the bounds of the text, using our testTextSize.
    paint.setTextSize(testTextSizeSp);
    Rect bounds = new Rect();
    paint.getTextBounds(text, 0, text.length(), bounds);

    // Calculate the desired size as a proportion of our testTextSize.
    float desiredTextSizeSp = testTextSizeSp * desiredWidthPx / bounds.width();

    // Set the paint for that size.
    paint.setTextSize(desiredTextSizeSp);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Draw shadows and the main route label box
    canvas.drawRect(x1_px, y1_px - 1f, x1_px, y1_px, topShadowMarkerPaint);
    canvas.drawRect(x1_px, y2_px, x2_px, y2_px + 2f, bottomShadowMarkerPaint);
    canvas.drawRect(x1_px - 1f, y1_px, x1_px, y2_px, leftShadowMarkerPaint);
    canvas.drawRect(x2_px, y1_px, x2_px + 1f, y2_px, rightShadowMarkerPaint);
    canvas.drawRect(x1_px, y1_px, x2_px, y2_px, markerPaint);

    // if the scale is big enough, show more info
    if (showName && showGrade) {
      canvas.drawText(gradeString, cx_px - gradeRect.exactCenterX(),
          y1_px + TEXT_PADDING_PX + gradeRect.height(), gradePaint);
      canvas
          .drawText(routeName, cx_px - nameRect.exactCenterX(), y2_px - TEXT_PADDING_PX, namePaint);
    } else if (showGrade) {
      canvas
          .drawText(gradeString, cx_px - gradeRect.exactCenterX(), cy_px - gradeRect.exactCenterY(),
              gradePaint);
    }

    if (sendCount > 0) {
      canvas.drawCircle(
          x2_px,
          y1_px,
          BUBBLE_RADIUS_PX,
          sendCountBubblePaint);
      canvas.drawText(sendsString,
          x2_px - sendCountRect.exactCenterX(),
          y1_px + 4.5f,
          sendCountPaint);
    }
  }

  @org.jetbrains.annotations.Contract(pure = true)
  private String toGradeString(int routeGrade) {
    return "V" + routeGrade;
  }

  public void setPosition(Msgs.Point position) {
    this.position = position;
    onDataChanged();
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
    onDataChanged();
  }

  public void setRouteGrade(int routeGrade) {
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
    } else {
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
    onDataChanged();
  }

  private void onDataChanged() {
    final int MIN_W_OF_GRADE_FONT_PX = 20;
    final int MIN_W_OF_NAME_FONT_PX_PER_CHAR = 12;

    cx_px = (int) (this.position.getX() * metersToPixels);
    cy_px = (int) (this.position.getY() * metersToPixels);

    // The width/height of the box is based on the current scale factor
    int labelWPx = (int) (metersToPixels * MIN_SIZE_M);
    int labelHPx = (int) (metersToPixels * MIN_SIZE_M);

    int min_w_name_font_px = MIN_W_OF_NAME_FONT_PX_PER_CHAR * routeName.length();
    // Check how big the text would be if we showed both grade and name
    int minWForGradeAndNamePx =
        TEXT_PADDING_PX * 2 + Math.max(MIN_W_OF_GRADE_FONT_PX, min_w_name_font_px);
    int minWForGradePx = TEXT_PADDING_PX * 2 + MIN_W_OF_GRADE_FONT_PX;

    if (labelWPx * scaleFactor > minWForGradeAndNamePx) {
      showName = true;
      showGrade = true;
      int maxWForTextPx = labelWPx - TEXT_PADDING_PX * 2;
      setTextSizeForWidth(gradePaint, maxWForTextPx, gradeString);
      setTextSizeForWidth(namePaint, maxWForTextPx, routeName);
      gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);
      namePaint.getTextBounds(routeName, 0, routeName.length(), nameRect);
      labelHPx = Math.max(labelHPx, TEXT_PADDING_PX * 3 + nameRect.height() + gradeRect.height());
    } else if (labelWPx * scaleFactor > minWForGradePx) {
      showName = false;
      showGrade = true;
      int wForGradePx = labelWPx - TEXT_PADDING_PX * 2;
      setTextSizeForWidth(gradePaint, wForGradePx, gradeString);
      gradePaint.getTextBounds(gradeString, 0, gradeString.length(), gradeRect);
      labelHPx = Math.max(labelHPx, TEXT_PADDING_PX * 3 + gradeRect.height());
    } else {
      showName = false;
      showGrade = false;
    }

    x1_px = cx_px - labelWPx / 2;
    x2_px = cx_px + labelWPx / 2;
    y1_px = cy_px - labelHPx / 2;
    y2_px = cy_px + labelHPx / 2;
    sendsString = String.valueOf(sendCount);
    sendCountPaint.getTextBounds(sendsString, 0, sendsString.length(), sendCountRect);

    int SHADOW_START = 0x11000000;
    int SHADOW_END = 0x00000000;
    Shader topShader = new LinearGradient(0, y1_px - 2f, 0, y1_px, SHADOW_END, SHADOW_START,
        TileMode.CLAMP);
    Shader bottomShader = new LinearGradient(0, y2_px, 0, y2_px + 4f, SHADOW_START, SHADOW_END,
        TileMode.CLAMP);
    Shader leftShader = new LinearGradient(x1_px - 2f, 0, x1_px, 0, SHADOW_END, SHADOW_START,
        TileMode.CLAMP);
    Shader rightShader = new LinearGradient(x2_px, 0, x2_px + 2f, 0, SHADOW_START, SHADOW_END,
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
        if (within(ev.getX(), ev.getY()) && routeOwnsEvent && (showGrade || showName)) {
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
    return (x1_px < x && x < x2_px) && (y1_px < y && y < y2_px);
  }

  public void addRouteClickedListener(RouteClickedListener listener) {
    routeClickedListeners.add(listener);
  }

  interface RouteClickedListener {

    void onRouteClicked(RouteLabelView view);

    void onRouteLongPressed(RouteLabelView view);
  }
}
