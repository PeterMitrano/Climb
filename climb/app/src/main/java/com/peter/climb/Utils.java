package com.peter.climb;

import android.graphics.Color;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {

  /**
   * Make a color lighter or darker
   *
   * @param color the color to change
   * @param factor >1f is lighter, <1f is darker
   * @return the new color
   */
  public static int manipulateColor(int color, float factor) {
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

  static long activeTime(Session session) {
    return session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
  }

  static String activeTimeStringHM(Session session) {
    return millisDurationHM(activeTime(session));
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

  static int millisToHours(long millis) {
    return (int) ((millis / (1000 * 60 * 60)) % 24);
  }

  static int millisToMinutes(long millis) {
    return (int) ((millis / (1000 * 60)) % 60);
  }

  static String millisDurationHM(long millis) {
    int hours = millisToHours(millis);
    int minutes = millisToMinutes(millis);

    if (hours == 0 && minutes == 0) {
      minutes = 1;
    }

    return HMToString(hours, minutes);
  }

  static String HMToString(int hours, int minutes) {
    return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
  }

  static String HMDDate(int year, int month, int day) {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, day);
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    return formatter.format(cal.getTime());
  }

  static String millsDate(long millis) {
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    return formatter.format(new Date(millis));
  }

  static long HMToMillis(long hours, long minutes) {
    long m = hours * 60L + minutes;
    return m * 60L * 1000L;
  }

  static long HMToMillis(int hours, int minutes) {
    return (hours * 60L + minutes) * 60L * 1000L;
  }

  static String timeStr(int h, int m) {
    m = m <= 0 ? 1 : m;
    return String.format(Locale.getDefault(), "%02d:%02d", h, m);
  }

  static int sendCount(Iterable<DataSet> dataSets, DataType routeType) {
    int c = 0;
    for (DataSet dataSet : dataSets) {
      if (dataSet.getDataType().equals(routeType)) {
        c += dataSet.getDataPoints().size();
      }

    }
    return c;
  }

  public static String m() {
    final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
    return ste[5].getClassName() + "." + ste[5].getMethodName() + "(...)";
  }
}
