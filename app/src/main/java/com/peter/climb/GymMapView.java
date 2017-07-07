package com.peter.climb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.peter.Climb.Msgs;

import java.util.ArrayList;
import java.util.List;

public class GymMapView extends ViewGroup {

    private int background_color;
    private List<WallView> wall_views;
    private List<RouteLabelView> label_views;
    private Msgs.Gym gym;

    public GymMapView(Context context) {
        super(context);
        init();
    }

    public GymMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GymMapView, 0, 0);
        try {
            background_color = a.getColor(R.styleable.GymMapView_background_color, 0xff000000);
        } finally {
            a.recycle();
        }

        init();
    }

    public int getBackgroundColor() {
        return background_color;
    }

    public void setGym(Msgs.Gym gym) {
        this.gym = gym;

        onDataChanged();
    }

    public void setBackgroundColor(int background_color) {
        this.background_color = background_color;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. We lays out its children in onSizeChanged().
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // update the layout. This usually happens if the phone is rotated.

        for (WallView wall_view : wall_views) {
//            wall_view.layout()
        }

        for (RouteLabelView label_view : label_views) {
//            label_view.layout()
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));

        int minh = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight();
        int h = Math.max(minh, MeasureSpec.getSize(heightMeasureSpec));

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(background_color);
    }

    private void onDataChanged() {
        // create all the views from the Gym msg in the constructor
        if (gym != null) {

            for (Msgs.Wall wall : gym.getWallsList()) {
                WallView wall_view = new WallView(getContext());
                wall_view.setWall(wall);

                wall_views.add(wall_view);
                addView(wall_view);

                for (Msgs.Route route : wall.getRoutesList()) {
                    RouteLabelView label_view = new RouteLabelView(getContext());
                    label_view.setRouteGrade(route.getGrade());
                    label_view.setRouteName(route.getName());
                    label_view.setPosition(route.getPosition());
                    label_views.add(label_view);
                    addView(label_view);
                }
            }
        }
    }

    private void init() {
        wall_views = new ArrayList<>();
        label_views = new ArrayList<>();
    }
}
