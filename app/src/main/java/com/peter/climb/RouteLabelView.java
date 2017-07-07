package com.peter.climb;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.peter.Climb.Msgs;

public class RouteLabelView extends View {

    private int routeGrade;
    private Msgs.Point2D position;
    private String routeName;

    public RouteLabelView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    public void setPosition(Msgs.Point2D position) {
        this.position = position;
        invalidate();
        requestLayout();
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
        invalidate();
        requestLayout();
    }

    public void setRouteGrade(int routeGrade) {
        this.routeGrade = routeGrade;
        invalidate();
        requestLayout();
    }
}
