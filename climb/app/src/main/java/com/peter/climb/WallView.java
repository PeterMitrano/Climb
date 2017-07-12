package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.View;
import com.peter.Climb.Msgs;

public class WallView extends View {

  private Paint wallPaint;
  private Msgs.Wall wall;

  private Path wallPath;
  private float metersToPixels = 1.f;

  public WallView(Context context) {
    super(context);

    wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallPaint.setStyle(Style.FILL);

    wallPath = new Path();

    setElevation(1);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (Msgs.Point2D p : this.wall.getPolygon().getPointsList()) {
      float px = metersToPixels * p.getX();
      float py = metersToPixels * p.getY();
      if (wallPath.isEmpty()) {
        wallPath.moveTo(px, py);
      } else {
        wallPath.lineTo(px, py);
      }
    }

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
    }
  }

  public void setMetersToPixels(float metersToPixels) {
    this.metersToPixels = metersToPixels;

    wallPath = new Path();
    invalidate();
  }
}

