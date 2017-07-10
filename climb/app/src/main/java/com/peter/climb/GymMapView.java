package com.peter.climb;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import com.peter.Climb.Msgs;
import java.util.ArrayList;
import java.util.List;

public class GymMapView extends ViewGroup {

  private int floorColor;
  private List<WallView> wallViews;
  private List<RouteLabelView> labelViews;
  private Msgs.Gym gym;
  private ScaleGestureDetector scaleGestureDetector;
  private float metersToPixels;
  private float scaleFactor;
  private float lastTouchX;
  private float lastTouchY;
  private int activePointerId;
  private float posX;
  private float posY;
  private Paint gymFloorPaint;
  private RectF gymFloorRect;


  public GymMapView(Context context) {
    super(context);
    init();
  }

  public GymMapView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GymMapView, 0, 0);
    try {
      floorColor = a.getColor(R.styleable.GymMapView_floor_color, 0xff000000);
    } finally {
      a.recycle();
    }

    init();
  }

  public int getFloorColor() {
    return floorColor;
  }

  public void setFloorColor(int backgroundColor) {
    this.floorColor = backgroundColor;
    gymFloorPaint.setColor(floorColor);
    invalidate();
  }

  public void setGym(Msgs.Gym gym) {
    this.gym = gym;

    onDataChanged();
    invalidate();
    invalidateChildren();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // Do nothing. Do not call the superclass method--that would start a layout pass
    // on this view's children. We lays out its children in onSizeChanged().
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    if (gym != null) {
      float gymAspectRatio = gym.getHeight() / gym.getWidth();
      float screenAspectRatio = (float) getHeight() / getWidth();

      if (gymAspectRatio < screenAspectRatio) {
        // gym fills width
        metersToPixels = getWidth() / gym.getWidth();
      } else {
        // gym fills height
        metersToPixels = getHeight() / gym.getHeight();
      }

      updateFloorRect();

      for (WallView wallView : wallViews) {
        wallView.setMetersToPixels(metersToPixels);
      }

      for (WallView wallView : wallViews) {
        wallView.layout(0, 0, w, h);
      }

      for (RouteLabelView labelView : labelViews) {
        labelView.layout(0, 0, w, h);
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Try for a width based on our minimum
    int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
    int w = Math.max(minW, MeasureSpec.getSize(widthMeasureSpec));

    int minH = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight();
    int h = Math.max(minH, MeasureSpec.getSize(heightMeasureSpec));

    setMeasuredDimension(w, h);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.translate(posX, posY);
    canvas.scale(scaleFactor, scaleFactor);
    canvas.drawRect(gymFloorRect, gymFloorPaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    // Let the ScaleGestureDetector inspect all events.
    scaleGestureDetector.onTouchEvent(ev);

    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();

        lastTouchX = x;
        lastTouchY = y;
        activePointerId = ev.getPointerId(0);
        break;
      }

      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = ev.findPointerIndex(activePointerId);
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        // Only move if the ScaleGestureDetector isn't processing a gesture.
        if (!scaleGestureDetector.isInProgress()) {
          final float dx = x - lastTouchX;
          final float dy = y - lastTouchY;

          posX = Math.min(0, posX + dx);
          posY = Math.min(0, posY + dy);

          invalidate();
          invalidateChildren();
        }

        lastTouchX = x;
        lastTouchY = y;

        break;
      }

      case MotionEvent.ACTION_UP: {
        activePointerId = INVALID_POINTER_ID;
        break;
      }

      case MotionEvent.ACTION_CANCEL: {
        activePointerId = INVALID_POINTER_ID;
        break;
      }

      case MotionEvent.ACTION_POINTER_UP: {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
          // This was our active pointer going up. Choose a new
          // active pointer and adjust accordingly.
          final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
          lastTouchX = ev.getX(newPointerIndex);
          lastTouchY = ev.getY(newPointerIndex);
          activePointerId = ev.getPointerId(newPointerIndex);
        }
        break;
      }
    }
    return true;
  }

  private void invalidateChildren() {
    for (WallView wallView : wallViews) {
      wallView.invalidate();
    }

    for (RouteLabelView routeLabelView : labelViews) {
      routeLabelView.invalidate();
    }
  }

  private void updateFloorRect() {
    int w = (int) (gym.getWidth() * metersToPixels);
    int h = (int) (gym.getHeight() * metersToPixels);
    gymFloorRect.set(0, 0, w, h);
  }

  private void onDataChanged() {
    // create all the views from the Gym msg in the constructor
    if (gym != null) {
      updateFloorRect();

      // add all the walls first
      for (Msgs.Wall wall : gym.getWallsList()) {
        WallView wallView = new WallView(getContext());
        wallView.setWall(wall);

        wallViews.add(wallView);
        addView(wallView);
      }

      // then add the routes on top
      for (Msgs.Wall wall : gym.getWallsList()) {
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
    scaleFactor = 1.f;

    gymFloorRect = new RectF();
    gymFloorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gymFloorPaint.setColor(floorColor);

    wallViews = new ArrayList<>();
    labelViews = new ArrayList<>();

    scaleGestureDetector = new ScaleGestureDetector(getContext(), new MapGestureListener());
  }

  /**
   * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom gesture
   * processing.
   */
  private class MapGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      scaleFactor *= detector.getScaleFactor();

      // Don't let the object get too small or too large.
      scaleFactor = Math.max(1f, Math.min(scaleFactor, 5.0f));

      return true;
    }
  }
}

