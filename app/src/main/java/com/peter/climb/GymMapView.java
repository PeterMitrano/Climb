package com.peter.climb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.peter.Climb.Msgs;

import java.util.ArrayList;

public class GymMapView extends ViewGroup {

    private int background_color;
    private Paint paint;
    private WallsView walls_view;
    private ArrayList<RouteLabelView> label_views;
    private Msgs.Gym gym;

    public GymMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GymMap, 0, 0);
        try {
            background_color = a.getColor(R.styleable.GymMap_background_color, 0x000);
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
        requestLayout();
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
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void onDataChanged() {

        // create all the views from the Gym msg in the constructor
        if (gym != null) {
            walls_view.setWalls(gym.getWallsList());

            for (Msgs.Wall wall : gym.getWallsList()) {
                for (Msgs.Route route : wall.getRoutesList()) {
                    RouteLabelView label_view = new RouteLabelView(getContext());
                    label_view.setRouteGrade(route.getGrade());
                    label_view.setRouteName(route.getName());
                    label_view.setPosition(route.getPosition());
                    label_views.add(label_view);
                }
            }
        }
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        walls_view = new WallsView(getContext());
        addView(walls_view);

        label_views = new ArrayList<>();
    }
}
