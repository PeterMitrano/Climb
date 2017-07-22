package com.peter.climb;

import static com.peter.climb.MyApplication.AppState.RESUME_FROM_NOTIFICATION_ACTION;

import android.app.NotificationManager;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.peter.Climb.Msgs.Gyms;
import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;
import com.peter.climb.FetchGymDataTask.FetchGymDataListener;
import com.peter.climb.GymMapView.AddRouteListener;
import com.peter.climb.MyApplication.AppState;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements OnClickListener, AddRouteListener,
    FetchGymDataListener {

  private AppState appState;
  private TextView timerView;
  private View decor_view;
  private GymMapView gymMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    appState = ((MyApplication) getApplicationContext())
        .fetchGymDataAndAppState(getApplicationContext(), this);
    decor_view = getWindow().getDecorView();

    Button endSessionButton = (Button) findViewById(R.id.end_session_button);
    endSessionButton.setOnClickListener(this);

    timerView = (TextView) findViewById(R.id.time);

    gymMapView = (GymMapView) findViewById(R.id.map_view);

    gymMapView.addAddRouteListener(this);
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
      if (appState.isSessionEmpty()) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.empty_session_message)
            .setPositiveButton(R.string.end_empty_session, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dismissNotificationAndFinish(RESULT_CANCELED);
              }
            })
            .setNegativeButton(R.string.cancel, null);
        builder.create().show();
      } else {
        appState.endSession(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {

            if (status.isSuccess()) {
              Toast.makeText(getApplicationContext(), "Session Saved!", Toast.LENGTH_SHORT).show();
              dismissNotificationAndFinish(RESULT_OK);
            } else if (status.hasResolution()) {
              try {
                status.getResolution().send();
              } catch (CanceledException e) {
                e.printStackTrace();
              }
            } else {
              Snackbar snack = Snackbar
                  .make(decor_view, "Failed to store your session in Google fit",
                      Snackbar.LENGTH_LONG);
              snack.setAction(R.string.ignore_end_session_error, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  dismissNotificationAndFinish(RESULT_CANCELED);
                }
              });
              snack.show();
            }
          }
        }, getApplicationContext());
      }
    }
  }

  @Override
  public void onAddRoute(Route route, Wall wall) {
    appState.addRouteIntoSession(route, wall);
  }

  private void dismissNotificationAndFinish(int resultCode) {
    // close the notification
    NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(MainActivity.SESSION_NOTIFICATION_ID);

    // allow a new session to be opened
    appState.inProgress = false;
    appState.clearSends();

    // close the activity
    setResult(resultCode);
    finish();
  }


  @Override
  public void onGymsFound(Gyms gyms) {
    Intent intent = getIntent();
    if (intent.getAction().equals(RESUME_FROM_NOTIFICATION_ACTION)) {
      appState.restoreFromIntent(intent);
    }

    gymMapView.setGym(appState.getCurrentGym());
    startSessionTimer();
  }

  @Override
  public void onNoGymsFound() {
    // TODO: this is a serious case that needs to be handled.
    Log.e(getClass().toString(), "well fuck...");
  }
}
