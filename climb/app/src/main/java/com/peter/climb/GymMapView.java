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

  private int backgroundColor;
  private List<WallView> wallViews;
  private List<RouteLabelView> labelViews;
  private Msgs.Gym gym;

  public GymMapView(Context context) {
    super(context);
    init();
  }

  public GymMapView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GymMapView, 0, 0);
    try {
      backgroundColor = a.getColor(R.styleable.GymMapView_background_color, 0xff000000);
    } finally {
      a.recycle();
    }

    init();
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
    invalidate();
  }

  public void setGym(Msgs.Gym gym) {
    this.gym = gym;

    onDataChanged();
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

    for (WallView wallView : wallViews) {
//            wallView.layout()
    }

    for (RouteLabelView labelView : labelViews) {
//            labelView.layout()
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
    canvas.drawColor(backgroundColor);
  }

  private void onDataChanged() {
    // create all the views from the Gym msg in the constructor
    if (gym != null) {

      for (Msgs.Wall wall : gym.getWallsList()) {
        WallView wallView = new WallView(getContext());
        wallView.setWall(wall);

        wallViews.add(wallView);
        addView(wallView);

        for (Msgs.Route route : wall.getRoutesList()) {
          RouteLabelView labelView = new RouteLabelView(getContext());
          labelView.setRouteGrade(route.getGrade());
          labelView.setRouteName(route.getName());
          labelView.setPosition(route.getPosition());
          labelViews.add(labelView);
          addView(labelView);
        }
      }
    }
  }

  private void init() {
    wallViews = new ArrayList<>();
    labelViews = new ArrayList<>();
  }
}
