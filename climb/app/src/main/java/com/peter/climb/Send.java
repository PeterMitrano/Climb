package com.peter.climb;

import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;

final class Send {

  private Route route;
  private long timeMillis;
  private Wall wall;

  Send(Route route, Wall wall, long timeMillis) {
    this.route = route;
    this.wall = wall;
    this.timeMillis = timeMillis;
  }

  int getGrade() {
    return route.getGrade();
  }

  String getName() {
    return route.getName();
  }

  String getColor() {
    return route.getColor();
  }

  long getTimeMillis() {
    return timeMillis;
  }

  String getWallName() {
    return wall.getName();
  }
}
