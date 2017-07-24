package com.peter.climb;

import android.graphics.Color;
import com.google.android.gms.fitness.data.Session;
import java.util.concurrent.TimeUnit;

class Utils {

  /**
   * Make a color lighter or darker
   *
   * @param color the color to change
   * @param factor >1f is lighter, <1f is darker
   * @return the new color
   */
  static int manipulateColor(int color, float factor) {
    int a = Color.alpha(color);
    int r = Math.round(Color.red(color) * factor);
    int g = Math.round(Color.green(color) * factor);
    int b = Math.round(Color.blue(color) * factor);
    return Color.argb(a,
        Math.min(r, 255),
        Math.min(g, 255),
        Math.min(b, 255));
  }

  static String activeTimeString(Session session) {
    long milliseconds =
        session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
    return activeTimeString(milliseconds);
  }

  private static String activeTimeString(long millis) {
    int hours = (int) millis / (1000 * 60 * 60);
    int minutes = (int) millis / (1000 * 60);
    return hours + " h " + minutes + " min";

  }
}
