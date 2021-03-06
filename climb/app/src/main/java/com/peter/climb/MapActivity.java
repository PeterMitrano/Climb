package com.peter.climb;

import static com.peter.climb.MainActivity.START_SESSION_ACTION;

import android.app.NotificationManager;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.peter.Climb.Msgs.Gyms;
import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;
import com.peter.climb.SessionInfoFragment.SessionInfoListener;
import com.peter.climb.Views.GymMapView;
import com.peter.climb.Views.GymMapView.RouteListener;
import java.util.Locale;

public class MapActivity extends ActivityWrapper implements RouteListener, SessionInfoListener {

  private static final int SEEKBAR_MAX = 200;
  private View decor_view;
  private GymMapView gymMapView;
  private SessionInfoFragment sessionInfoFragment;
  private SeekBar floorSeekbar;
  private TextView floorTitle;
  private boolean notifyOnConnect = false;
  private int progressTicksPerFloor;
  private boolean justLooking;

  @Override
  public void onBackPressed() {
    endSession();
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
  public void onAddRoute(Route route, Wall wall) {
    appState.addRouteIntoSession(route, wall);
  }

  @Override
  public void onRemoveRoute(Route route, Wall wall) {
    appState.removeRouteFromSession(route, wall);
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
    String action = intent.getAction();

    gymMapView.setGym(appState.getCurrentGym());
    int floorCount = appState.getCurrentGym().getFloorsCount();
    progressTicksPerFloor = Math.max(1, SEEKBAR_MAX / floorCount);
    if (floorCount == 1) {
      floorSeekbar.setEnabled(false);
    } else {
      floorSeekbar.setEnabled(true);
    }
    setFloorText();

    if (action != null && action.equals(START_SESSION_ACTION)) {
      justLooking = false;
      // add sessionInfoFragment
      sessionInfoFragment.startSessionTimer();
    } else {
      justLooking = true;
      // remove sessionInfoFragment
    }
  }

  @Override
  public void onNoGymsFound() {
    // TODO: this is a serious case that needs to be handled.
    Log.e(getClass().toString(), "Failed to get Gym data, retrying...");
    appState.refreshGyms(this);
  }

  @Override
  public void onSessionEnded() {
    endSession();
  }

  private void endSession() {
    if (justLooking) {
      dismissNotificationAndFinish(RESULT_OK);
    } else if (appState.isSessionEmpty()) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.empty_session_message)
          .setPositiveButton(R.string.end_empty_session, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dismissNotificationAndFinish(RESULT_CANCELED);
            }
          })
          .setNegativeButton(R.string.cancel, null);
      builder.create().show();
    } else if (appState.mClient.isConnected()) {
      appState.endSession(new ResultCallback<Status>() {
        @Override
        public void onResult(@NonNull Status status) {

          if (status.isSuccess()) {
            Toast.makeText(getApplicationContext(), "Session Saved", Toast.LENGTH_SHORT).show();
            dismissNotificationAndFinish(RESULT_OK);
          } else if (status.hasResolution()) {
            try {
              status.getResolution().send();
            } catch (CanceledException e) {
              e.printStackTrace();
            }
          } else {
            showGoogleFitFailure();
          }
        }
      }, getApplicationContext());
    } else {
      showGoogleFitFailure();
    }
  }

  private void showGoogleFitFailure() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.session_save_failure)
        .setPositiveButton(R.string.ignore_end_session_error, new OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dismissNotificationAndFinish(RESULT_CANCELED);
          }
        })
        .setNegativeButton(R.string.retry, new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            notifyOnConnect = true;
            appState.mClient.connect();
          }
        });
    builder.create().show();
  }

  @Override
  void onPermissionsDenied() {
    Snackbar snack = Snackbar
        .make(gymMapView, R.string.no_fit_permission_msg, Snackbar.LENGTH_INDEFINITE);
    snack.setAction(R.string.ask_again, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        appState.mClient.reconnect();
        mResolvingError = false;
      }
    });
    snack.show();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    progressTicksPerFloor = SEEKBAR_MAX;

    decor_view = getWindow().getDecorView();

    sessionInfoFragment = (SessionInfoFragment) getSupportFragmentManager()
        .findFragmentById(R.id.session_info_fragment);
    sessionInfoFragment.setSessionInfoListener(this);

    gymMapView = (GymMapView) findViewById(R.id.map_view);
    gymMapView.addAddRouteListener(this);

    floorSeekbar = (SeekBar) findViewById(R.id.floor_seekbar);
    floorSeekbar.setMax(SEEKBAR_MAX);
    floorSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int floor = Math.min(progress, SEEKBAR_MAX - 1) / progressTicksPerFloor;
        gymMapView.setCurrentFloor(floor);
        setFloorText();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    floorTitle = (TextView) findViewById(R.id.floor_title);
  }

  private void setFloorText() {
    int i = gymMapView.getCurrentFloor() + 1;
    int floorsCount = appState.getCurrentGym().getFloorsCount();
    floorTitle.setText(String.format(Locale.getDefault(), "Floor %d / %d", i, floorsCount));
  }

  @Override
  public void onGoogleFitConnected() {
    if (notifyOnConnect) {
      String message = "Sign in was Successful";
      Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
      notifyOnConnect = false;
    }
  }

  @Override
  public void onGoogleFitFailed() {
    if (notifyOnConnect) {
      String message = "Google Fit Disconnected";
      Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
      notifyOnConnect = false;
    }
  }
}
