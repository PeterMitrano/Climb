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
import com.peter.climb.MyApplication.AppState;
import com.peter.climb.MyApplication.GoogleFitListener;

public abstract class MyActivity extends AppCompatActivity implements OnConnectionFailedListener,
    ConnectionCallbacks {

  public static final int REQUEST_RESOLVE_ERROR = 1001;
  public static final String DIALOG_ERROR = "GOOGLE_API_ERROR";
  boolean mResolvingError = false;
  AppState appState;
  GoogleFitListener googleFitListener;

  abstract void onPermissionsDenied();

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
    appState.createDataTypes(new CreateDataTypesListener() {
      @Override
      public void onDataTypesCreated() {
        if (googleFitListener != null) {
          googleFitListener.onGoogleFitConnected();
        }
      }

      @Override
      public void onDataTypesNotCreated() {
        if (googleFitListener != null) {
          googleFitListener.onGoogleFitFailed();
        }
      }
    });
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
      googleFitListener.onGoogleFitFailed();
    }
  }

  void setupGoogleFit(@Nullable GoogleFitListener googleFitListener) {
    this.googleFitListener = googleFitListener;

    if (appState.mClient == null) {
      appState.mClient = new GoogleApiClient.Builder(getApplicationContext())
          .addApi(Fitness.SESSIONS_API)
          .addApi(Fitness.CONFIG_API)
          .addApi(Fitness.HISTORY_API)
          .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
          .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
          .addScope(new Scope(Scopes.PROFILE))
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .build();
      appState.mClient.connect();
    } else if (appState.mClient.isConnected()) {
      onConnected(null);
    }
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
