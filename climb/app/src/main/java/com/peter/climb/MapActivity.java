package com.peter.climb;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String STAT_TIME_MILLIS_KEY = "start_time_millis_key";
  private View decor_view;
  private Button endSessionButton;
  private AppState appState;
  private TextView timerView;
  private GymMapView gymMapView;
  private long startTimeMillis = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    appState = ((MyApplication) getApplicationContext()).getState();

    decor_view = getWindow().getDecorView();

    endSessionButton = (Button) findViewById(R.id.end_session_button);
    endSessionButton.setOnClickListener(this);

    timerView = (TextView) findViewById(R.id.time);

    gymMapView = (GymMapView) findViewById(R.id.map_view);
    gymMapView.setGym(appState.getCurrentGym());

    if (savedInstanceState != null) {
      Long savedStartTimeMillis = savedInstanceState.getLong(STAT_TIME_MILLIS_KEY, -1);
      if (savedStartTimeMillis != -1) {
        startTimeMillis = savedStartTimeMillis;
      }
    }

    if (startTimeMillis == -1) {
      Intent intent = getIntent();
      String action = intent.getAction();
      if (action != null && action.equals(MainActivity.START_SESSION_ACTION)) {
        startTimeMillis = System.currentTimeMillis();
        appState.sessionInProgress = AppState.SessionState.IN_PROGRESS;
      }
      else {
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        startTimeMillis = settings.getLong(STAT_TIME_MILLIS_KEY, -1);
      }
    }

    startSessionTimer();
    loadMap();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(STAT_TIME_MILLIS_KEY, startTimeMillis);

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onStop() {
    super.onStop();
    saveTime();
  }

  private void saveTime() {
    SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putLong(STAT_TIME_MILLIS_KEY, startTimeMillis);
    editor.apply();
  }

  private void startSessionTimer() {
    new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            long millis = System.currentTimeMillis() - startTimeMillis;
            String hms = String
                .format(Locale.US, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
            timerView.setText(hms);
          }

        });
      }

    }, 0, 1000);
  }

  private void loadMap() {
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
//    if (hasFocus) {

      // TODO: 7/9/17 this works, but the app bar shows up for a second which is bad 
//      int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//          | View.SYSTEM_UI_FLAG_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//
//      decor_view.setSystemUiVisibility(flags);
//    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.end_session_button) {
      appState.sessionInProgress = AppState.SessionState.NOT_IN_PROGRESS;
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(MainActivity.SESSION_NOTIFICATION_ID);

      finish();
    }
  }
}
