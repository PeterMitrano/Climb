package com.peter.climb;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.peter.Climb.Msgs.Route;
import com.peter.climb.GymMapView.AddRouteListener;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements OnClickListener, AddRouteListener {

  private AppState appState;
  private TextView timerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    appState = ((MyApplication) getApplicationContext()).getState();

    View decor_view = getWindow().getDecorView();

    Button endSessionButton = (Button) findViewById(R.id.end_session_button);
    endSessionButton.setOnClickListener(this);

    timerView = (TextView) findViewById(R.id.time);

    GymMapView gymMapView = (GymMapView) findViewById(R.id.map_view);
    gymMapView.setGym(appState.getCurrentGym());
    gymMapView.addAddRouteListener(this);

    startSessionTimer();
    loadMap();
  }

  private void startSessionTimer() {
    new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            long millis = appState.getSessionLength();
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
      appState.endSession();
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(MainActivity.SESSION_NOTIFICATION_ID);

      finish();
    }
  }

  @Override
  public void onAddRoute(Route route) {
    appState.addRouteIntoSession(route);
  }
}
