package com.peter.climb;

import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;


class Send {

  private final int grade;
  private final long timeMillis;
  private final String routeColor;
  private final String routeName;
  private final String wallName;
  private final Route route;

  Send(Route route, Wall wall, long timeMillis) {
    this.route = route;
    this.grade = route.getGrade();
    this.timeMillis = timeMillis;
    this.routeColor = route.getColor();
    this.routeName = route.getName();
    this.wallName = wall.getName();
  }

  Route getRoute() {
    return route;
  }

  int getGrade() {
    return grade;
  }

  long getTimeMillis() {
    return timeMillis;
  }

  String getColor() {
    return routeColor;
  }

  String getName() {
    return routeName;
  }

  String getWallName() {
    return wallName;
  }

  public String toString() {
    return "{name: " + getName() + ", grade: " + getGrade() + ", color: " + getColor() + ", time: "
        + String.valueOf(getTimeMillis()) + ", wall: " + getWallName() + "}";
  }
}
