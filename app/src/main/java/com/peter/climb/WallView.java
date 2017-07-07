package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.peter.Climb.Msgs;

public class WallView extends View {

    private Paint wallOutlinePaint;
    private Paint wallPaint;
    private Msgs.Wall wall;

    public WallView(Context context) {
        super(context);

        wallOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallOutlinePaint.setColor(0x000000);

        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
            String wall_color_str = polygon.getColorCode();
            int wall_color = Color.parseColor(wall_color_str);

            wallOutlinePaint.setColor(Color.BLACK);
            wallPaint.setColor(wall_color);
        }
    }
}

