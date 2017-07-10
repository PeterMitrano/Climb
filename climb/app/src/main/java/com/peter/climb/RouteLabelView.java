package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.peter.Climb.Msgs;

public class RouteLabelView extends View {

  private int routeGrade;
  private Msgs.Point2D position;
  private String routeName;
  private Paint markerPaint;
  private float metersToPixels;

  public RouteLabelView(Context context) {
    super(context);

    markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    markerPaint.setColor(Color.BLACK);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    float cx = this.position.getX() * metersToPixels;
    float cy = this.position.getY() * metersToPixels;
    float radius = Math.max(10, 0.1f * metersToPixels);
    canvas.drawCircle(cx, cy, radius, markerPaint);
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

  public void setMetersToPixels(float metersToPixels) {
    this.metersToPixels = metersToPixels;
  }
}
