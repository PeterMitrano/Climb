package com.peter.climb;

import android.graphics.Color;
import com.google.android.gms.fitness.data.Session;
import java.util.Locale;
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

  static String activeTimeStringHMS(Session session) {
    long milliseconds =
        session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
    return millisDurationHMS(milliseconds);
  }

  static String activeTimeStringHM(Session session) {
    long milliseconds =
        session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
    return millisDurationHM(milliseconds);
  }

  /**
   * Has minimum of 1 minute output
   *
   * @param millis input time duration in milliseconds
   * @return formatting string of hours + minutes
   */
  static String millisDurationHMS(long millis) {
    int seconds = (int) (millis / 1000) % 60;
    int minutes = (int) ((millis / (1000 * 60)) % 60);
    int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

    if (hours == 0 && minutes == 0 && seconds == 0) {
      seconds = 1;
    }

    return String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, seconds);
  }

  static String millisDurationHM(long millis) {
    int minutes = (int) ((millis / (1000 * 60)) % 60);
    int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

    if (hours == 0 && minutes == 0) {
      minutes = 1;
    }

    return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
  }
}
