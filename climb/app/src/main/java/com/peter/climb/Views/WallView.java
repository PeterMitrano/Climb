package com.peter.climb.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import com.peter.Climb.Msgs;
import com.peter.climb.Utils;

public class WallView extends View {

  final private Paint wallPaint;
  final private Paint wallShadowPaint;
  private final float shadowOffsetX = 1;
  private final float shadowOffsetY = 1;
  private Msgs.Wall wall;
  private Path wallPath;
  private Path wallShadowPath;
  private float metersToPixels = 1.f;

  public WallView(Context context) {
    super(context);

    wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallPaint.setStyle(Style.FILL);

    wallShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    wallShadowPaint.setStyle(Style.FILL);
    wallShadowPaint.setColor(0x22000000);

    wallPath = new Path();
    wallShadowPath = new Path();

    // scaling paths aren't supported with HW acceleration
    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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

      wallPath.reset();
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

