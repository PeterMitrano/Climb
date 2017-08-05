package com.peter.climb.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.View;
import com.peter.Climb.Msgs;

public class WallView extends View {

  final private Paint wallPaint;
  final private Paint wallShadowPaint;
  private Msgs.Wall wall;

  private Path wallPath;
  private Path wallShadowPath;
  private float metersToPixels = 1.f;
  private final float shadowOffsetX = 1;
  private final float shadowOffsetY = 1;

  public WallView(Context context) {
    super(context);

    wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallPaint.setStyle(Style.FILL);

    wallShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallShadowPaint.setStyle(Style.FILL);
    wallShadowPaint.setColor(0x22000000);

    wallPath = new Path();
    wallShadowPath = new Path();
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawPath(wallShadowPath, wallShadowPaint);
    canvas.drawPath(wallPath, wallPaint);
  }

  public void setWall(Msgs.Wall wall) {
    this.wall = wall;
    invalidate();

    onDataChanged();
  }

  private void onDataChanged() {
    if (wall != null) {
      Msgs.Polygon polygon = wall.getPolygon();
      String wallColorStr = polygon.getColor();

      try {
        int wallColor = Color.parseColor(wallColorStr);
        wallPaint.setColor(wallColor);
      } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
        wallPaint.setColor(Color.GRAY);
      }

      for (Msgs.Point p : this.wall.getPolygon().getPointsList()) {
        float px = metersToPixels * p.getX();
        float py = metersToPixels * p.getY();
        if (wallPath.isEmpty()) {
          wallPath.moveTo(px, py);
          wallShadowPath.moveTo(px + shadowOffsetX, py + shadowOffsetY);
        } else {
          wallPath.lineTo(px, py);
          wallShadowPath.lineTo(px + shadowOffsetX, py + shadowOffsetY);
        }
      }
    }
  }

  public void setMetersToPixels(float metersToPixels) {
    this.metersToPixels = metersToPixels;

    onDataChanged();
    invalidate();
  }
}

