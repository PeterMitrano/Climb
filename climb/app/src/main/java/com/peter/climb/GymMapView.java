package com.peter.climb;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import com.peter.Climb.Msgs.Gym;
import com.peter.Climb.Msgs.Point2D;
import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;
import com.peter.climb.RouteLabelView.RouteClickedListener;
import java.util.ArrayList;
import java.util.List;

public class GymMapView extends ViewGroup implements RouteClickedListener {

  public static final float MAX_ZOOM_FACTOR = 4.0f;
  public static final int GYM_FLOOR_OUTLINE_STROKE_WIDTH = 24;
  private static final int GYM_FLOOR_OUTLINE_COLOR = 0xff3d3d3d;
  private static final float MIN_ZOOM_FACTOR = 0.85f;
  private List<WallView> wallViews;

  private List<RouteLabelView> routeLabelViews;
  Gym gym;
  private ScaleGestureDetector scaleGestureDetector;
  private Paint gymFloorPaint;
  private Paint gymFloorOutlinePaint;
  private RectF gymFloorRect;
  private float metersToPixels;
  private float scaleFactor = 1f;
  private float lastTouchX;
  private float lastTouchY;

  private float posX;
  private float posY;
  private int activePointerId;
  private int floorColor;
  private int floor = 0;
  private List<Route> routes;
  private List<AddRouteListener> addRouteListeners;

  interface AddRouteListener {

    void onAddRoute(Route route);
  }

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

  public void setGym(Gym gym) {
    this.gym = gym;
    onDataChanged();

    // gym size has changed (if gym was null), so we must update scale
    updateScale();
    invalidate();
    invalidateChildren();
  }

  public WallView getWallView(int index) {
    return wallViews.get(index);
  }

  public RouteLabelView getRouteLabelView(int index) {
    return routeLabelViews.get(index);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // Do nothing. Do not call the superclass method--that would start a layout pass
    // on this view's children. We lays out its children in onSizeChanged().
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    // this is an unavoidable check. we must be able to draw without the gym ready
    if (gym != null) {
      // screen shape has changed, so we must update scale
      updateScale();

      for (WallView wallView : wallViews) {
        wallView.layout(0, 0, w, h);
      }

      for (RouteLabelView labelView : routeLabelViews) {
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
    canvas.scale(scaleFactor, scaleFactor, getWidth() / 2, getHeight() / 2);
    canvas.translate(posX, posY);
    canvas.drawRect(gymFloorRect, gymFloorPaint);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    // We can draw on top of children here
    canvas.drawRect(gymFloorRect, gymFloorOutlinePaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    // Let the ScaleGestureDetector inspect all events.
    scaleGestureDetector.onTouchEvent(ev);

    for (RouteLabelView routeLabelView : routeLabelViews) {
      float p[] = toLabelFrame(new float[]{ev.getX(), ev.getY()});
      MotionEvent routeLabelEvent = MotionEvent.obtain(ev);
      routeLabelEvent.setLocation(p[0], p[1]);
      boolean handled = routeLabelView.handleMotionEvent(routeLabelEvent);

      if (handled) {
        return true;
      }
    }

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

          posX = posX + dx;
          posY = posY + dy;

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

  void updateScale() {
    if (gym != null) {
      float gymAspectRatio = gym.getFloors(floor).getHeight() / gym.getFloors(floor).getWidth();
      float screenAspectRatio = (float) getHeight() / getWidth();

      if (gymAspectRatio < screenAspectRatio) {
        // gym fills width
        metersToPixels = getWidth() / gym.getFloors(floor).getWidth();
      } else {
        // gym fills height
        metersToPixels = getHeight() / gym.getFloors(floor).getHeight();
      }

      updateFloorRect();

      for (WallView wallView : wallViews) {
        wallView.setMetersToPixels(metersToPixels);
      }

      for (RouteLabelView labelView : routeLabelViews) {
        labelView.setMetersToPixels(metersToPixels);
      }
    }
  }

  void invalidateChildren() {
    for (WallView wallView : wallViews) {
      wallView.invalidate();
    }

    for (RouteLabelView routeLabelView : routeLabelViews) {
      routeLabelView.invalidate();
    }
  }

  private float[] toLabelFrame(float[] point) {
    if (point.length != 2) {
      throw new IllegalArgumentException("point must have length 2, not " + point.length);
    }

    Matrix transform = new Matrix();
    transform.postScale(1 / scaleFactor, 1 / scaleFactor, getWidth() / 2, getHeight() / 2);
    transform.postTranslate(-posX, -posY);

    float new_point[] = new float[2];
    transform.mapPoints(new_point, point);
    return new_point;
  }

  void updateFloorRect() {
    float w = gym.getFloors(floor).getWidth() * metersToPixels;
    float h = gym.getFloors(floor).getHeight() * metersToPixels;
    gymFloorRect.set(0, 0, w, h);
  }

  void onDataChanged() {
    // create all the views from the Gym msg in the constructor
    if (gym != null) {
      updateFloorRect();
      removeAllViews();

      addWalls();
      addRoutes();
    }
  }

  void addWalls() {
    // add all the walls first
    wallViews.clear();
    for (Wall wall : gym.getFloors(floor).getWallsList()) {
      WallView wallView = new WallView(getContext());
      wallView.setWall(wall);

      wallViews.add(wallView);
      addView(wallView);
    }
  }

  void addRoutes() {
    // then add the routes on top
    routeLabelViews.clear();
    routes.clear();
    for (Wall wall : gym.getFloors(floor).getWallsList()) {
      for (Route route : wall.getRoutesList()) {
        RouteLabelView routeLabelView = new RouteLabelView(getContext());
        routeLabelView.setRouteGrade(route.getGrade());
        routeLabelView.setRouteName(route.getName());
        routeLabelView.setPosition(route.getPosition());
        routeLabelView.setRouteColor(route.getColor());
        routeLabelView.addRouteClickedListener(this);
        routeLabelViews.add(routeLabelView);
        routes.add(route);
        Log.e(getClass().toString(), routeLabelView.toString());
        addView(routeLabelView);
      }
    }
  }

  void addRoute() {
    Point2D p = Point2D.newBuilder().setX(4).setY(5).build();
    RouteLabelView v = new RouteLabelView(getContext());
    v.setRouteGrade(1);
    v.setRouteName("Happiness");
    v.setRouteColor("0xff0000");
    v.setPosition(p);
    v.setMetersToPixels(54);
    routeLabelViews.add(v);
    addView(v);
  }

  private void init() {
    scaleFactor = 1.f;

    gymFloorRect = new RectF();
    gymFloorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gymFloorPaint.setColor(floorColor);

    gymFloorOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    gymFloorOutlinePaint.setStyle(Paint.Style.STROKE);
    gymFloorOutlinePaint.setStrokeWidth(GYM_FLOOR_OUTLINE_STROKE_WIDTH);
    gymFloorOutlinePaint.setColor(GYM_FLOOR_OUTLINE_COLOR);

    wallViews = new ArrayList<>();
    routeLabelViews = new ArrayList<>();
    routes = new ArrayList<>();
    addRouteListeners = new ArrayList<>();

    scaleGestureDetector = new ScaleGestureDetector(getContext(), new MapScaleGestureListener());
  }

  public void addAddRouteListener(AddRouteListener listener) {
    addRouteListeners.add(listener);
  }

  @Override
  public void onRouteClicked(RouteLabelView view) {
    // indicate the route has been added
    Snackbar.make(this, "Route added!", Snackbar.LENGTH_SHORT).show();
    int index = routeLabelViews.indexOf(view);
    Route route = routes.get(index);

    for (AddRouteListener listener : addRouteListeners) {
      listener.onAddRoute(route);
    }
  }

  private class MapScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      scaleFactor *= detector.getScaleFactor();

      // Don't let the object get too small or too large.
      scaleFactor = Math.max(MIN_ZOOM_FACTOR, Math.min(scaleFactor, MAX_ZOOM_FACTOR));

      for (RouteLabelView labelView : routeLabelViews) {
        labelView.setScaleFactor(scaleFactor);
      }

      invalidate();
      invalidateChildren();
      return true;
    }
  }
}

