package com.peter.climb;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

  private View decor_view;
  private Button endSessionButton;
  private AppState appState;
  private TextView timerView;
  private GymMapView gymMapView;

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

    if (getIntent().getAction().equals(MainActivity.START_SESSION_ACTION)) {
      appState.sessionInProgress = AppState.SessionState.IN_PROGRESS;
      startSessionTimer();
    }

    loadMap();
  }

  private void startSessionTimer() {
    Timer t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            int minutes = 12;
            int seconds = 34;
            timerView.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
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
    if (hasFocus) {

      // TODO: 7/9/17 this works, but the app bar shows up for a second which is bad 
//      int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//          | View.SYSTEM_UI_FLAG_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//
//      decor_view.setSystemUiVisibility(flags);
    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.end_session_button) {
      appState.sessionInProgress = AppState.SessionState.NOT_IN_PROGRESS;
      finish();
    }
  }
}
