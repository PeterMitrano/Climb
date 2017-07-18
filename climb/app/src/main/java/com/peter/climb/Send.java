package com.peter.climb;

import com.peter.Climb.Msgs.Route;

final class Send {

  private Route route;
  private long timeMillis;

  Send(Route route, long timeMillis) {
    this.route = route;
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
}
