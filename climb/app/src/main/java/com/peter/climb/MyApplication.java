package com.peter.climb;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.peter.Climb.Msgs;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

  private AppState state = new AppState();

  public AppState getState() {
    return state;
  }
}

class AppState {

  static final int NO_GYM_ID = -1;
  Msgs.Gyms gyms;

  GoogleApiClient mClient = null;
  private Msgs.Gym currentGym;
  private int currentGymId;
  private Session session;

  int getCurrentGymId() {
    return currentGymId;
  }

  Msgs.Gym getCurrentGym() {
    return currentGym;
  }

  void setCurrentGym(int current_gym_id) {
    this.currentGymId = current_gym_id;
    if (current_gym_id != NO_GYM_ID) {
      this.currentGym = gyms.getGyms(current_gym_id);
    }
  }

  void startSession() {
    session = new Session.Builder()
        .setName("Session Name 1")
        .setIdentifier("Session Identifier 1")
        .setDescription("Climbing Session")
        .setStartTime(System.currentTimeMillis() - 10, TimeUnit.MILLISECONDS)
        .setActivity(FitnessActivities.ROCK_CLIMBING)
        .build();

    PendingResult<Status> pendingResult =
        Fitness.SessionsApi.startSession(mClient, session);
    pendingResult.setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(@NonNull Status status) {
        Log.e(getClass().toString(), status.toString());
      }
    });
  }

  void endSession() {
    Log.e(getClass().toString(), String.valueOf(mClient.isConnected()) + " " + mClient.toString());
    PendingResult<SessionStopResult> pendingResult =
        Fitness.SessionsApi.stopSession(mClient, session.getIdentifier());
    pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
      @Override
      public void onResult(@NonNull SessionStopResult sessionStopResult) {
        Log.e(getClass().toString(), sessionStopResult.toString());
      }
    });
  }

  public boolean hasCurrentGym() {
    return currentGymId != NO_GYM_ID;
  }
}
