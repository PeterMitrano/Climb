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

  private Paint wallOutlinePaint;
  private Paint wallPaint;
  private Msgs.Wall wall;

  private Path wallPath;

  public WallView(Context context) {
    super(context);

    wallOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallOutlinePaint.setStyle(Style.STROKE);
    wallOutlinePaint.setColor(0x000000);

    wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallPaint.setStyle(Style.FILL);

    wallPath = new Path();
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Msgs.Polygon polygon = this.wall.getPolygon();
    for (Msgs.Point2D p : polygon.getPointsList()) {
      float px = p.getX();
      float py = p.getY();
      if (wallPath.isEmpty()) {
        wallPath.moveTo(px, py);
      } else {
        wallPath.lineTo(px, py);
      }
    }

    Msgs.Point2D p0 = polygon.getPoints(0);
    float x0 = p0.getX();
    float y0 = p0.getY();
    wallPath.lineTo(x0, y0);

    canvas.drawPath(wallPath, wallPaint);
    canvas.drawPath(wallPath, wallOutlinePaint);

  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
  }

  public void setWall(Msgs.Wall wall) {
    this.wall = wall;
    invalidate();

    onDataChanged();
  }

  private void onDataChanged() {
    if (wall != null) {
      Msgs.Polygon polygon = wall.getPolygon();
      String wallColorStr = polygon.getColorCode();

      try {
        int wallColor = Color.parseColor(wallColorStr);
        wallPaint.setColor(wallColor);
      } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
        wallPaint.setColor(Color.GRAY);
      }

      wallOutlinePaint.setColor(Color.BLACK);
    }
  }
}

