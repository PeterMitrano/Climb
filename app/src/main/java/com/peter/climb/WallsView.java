package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.peter.Climb.Msgs;

import java.util.List;

public class WallsView extends View {

    private List<Msgs.Wall> walls;

    public WallsView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    public void setWalls(List<Msgs.Wall> walls) {
        this.walls = walls;
        invalidate();
        requestLayout();
    }
}

