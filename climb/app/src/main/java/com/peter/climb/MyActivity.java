package com.peter.climb;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.peter.climb.CreateDataTypesTask.CreateDataTypesListener;
import com.peter.climb.FetchGymDataTask.FetchGymDataListener;
import com.peter.climb.MyApplication.AppState;
import com.peter.climb.MyApplication.GoogleFitListener;

public abstract class MyActivity extends AppCompatActivity implements OnConnectionFailedListener,
    ConnectionCallbacks, GoogleFitListener, FetchGymDataListener {

  public static final int REQUEST_RESOLVE_ERROR = 1001;
  public static final String DIALOG_ERROR = "GOOGLE_API_ERROR";
  boolean mResolvingError = false;
  AppState appState;

  abstract void onPermissionsDenied();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // calling this function ensures that gym data is fetched if need be
    appState = ((MyApplication) getApplicationContext()).fetchGymData(this);

    // We do this here so that whatever activity opens first (usually main) can setup google fit.
    // All activities that could possibly need google fit should call this in onCreate()
    if (appState.mClient == null) {
      appState.mClient = new GoogleApiClient.Builder(getApplicationContext())
          .addApi(Fitness.SESSIONS_API)
          .addApi(Fitness.CONFIG_API)
          .addApi(Fitness.HISTORY_API)
          .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
          .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
          .addScope(new Scope(Scopes.PROFILE))
          .build();
    }

    appState.mClient.registerConnectionCallbacks(this);
    appState.mClient.registerConnectionFailedListener(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_RESOLVE_ERROR) {
      mResolvingError = false;
      if (resultCode == RESULT_OK) {
        // Make sure the app is not already connected or attempting to connect
        if (!appState.mClient.isConnecting() &&
            !appState.mClient.isConnected()) {
          appState.mClient.connect();
        }
      } else {
        onPermissionsDenied();
      }
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    if (appState.hasDataTypes()) {
      onGoogleFitConnected();
    } else {
      appState.createDataTypes(new CreateDataTypesListener() {
        @Override
        public void onDataTypesCreated() {
          onGoogleFitConnected();
        }

        @Override
        public void onDataTypesNotCreated() {
          onGoogleFitFailed();
        }
      });
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
      Log.e(getClass().toString(), "Connection lost.  Cause: Network Lost.");
    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
      Log.e(getClass().toString(), "Connection lost.  Reason: Service Disconnected");
    }
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult result) {
    if (!mResolvingError && result.hasResolution()) {
      try {
        mResolvingError = true;
        result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
      } catch (SendIntentException e) {
        // There was an error with the resolution intent. Try again.
        appState.mClient.connect();
      }
    } else {
      showErrorDialog(result.getErrorCode());
      onGoogleFitFailed();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    unregisterGoogleFitListener();
  }

  void unregisterGoogleFitListener() {
    appState.mClient.unregisterConnectionCallbacks(this);
    appState.mClient.unregisterConnectionFailedListener(this);
  }

  private void showErrorDialog(int errorCode) {
    ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
    Bundle args = new Bundle();
    args.putInt(DIALOG_ERROR, errorCode);
    dialogFragment.setArguments(args);
    dialogFragment.show(this.getSupportFragmentManager(), getClass().toString());
  }

  /* Called from ErrorDialogFragment when the dialog is dismissed. */
  public void onDialogDismissed() {
    mResolvingError = false;
  }
}
