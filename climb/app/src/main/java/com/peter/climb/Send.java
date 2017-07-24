package com.peter.climb;

import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;


class Send {

  private final int grade;
  private final long timeMillis;
  private final String routeColor;
  private final String routeName;
  private final String wallName;

  Send(Route route, Wall wall, long timeMillis) {
    this.grade = route.getGrade();
    this.timeMillis = timeMillis;
    this.routeColor = route.getColor();
    this.routeName = route.getName();
    this.wallName = wall.getName();
  }

  Send(int grade, long timeMillis, String routeColor, String routeName, String wallName) {
    this.grade = grade;
    this.timeMillis = timeMillis;
    this.routeColor = routeColor;
    this.routeName = routeName;
    this.wallName = wallName;
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
}
