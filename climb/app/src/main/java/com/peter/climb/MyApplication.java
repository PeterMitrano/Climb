package com.peter.climb;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.peter.Climb.Msgs;

public class MyApplication extends Application {

  private AppState state = new AppState();

  public AppState getState() {
    return state;
  }
}

class AppState {

  Msgs.Gyms gyms;

  private GoogleApiClient mClient = null;
  private Msgs.Gym currentGym;
  private int currentGymId;

  void setGoogleApiClient(GoogleApiClient mClient) {
    this.mClient = mClient;
  }

  int getCurrentGymId() {
    return currentGymId;
  }

  Msgs.Gym getCurrentGym() {
    return currentGym;
  }

  void setCurrentGym(int current_gym_id) {
    this.currentGymId = current_gym_id;
    this.currentGym = gyms.getGyms(current_gym_id);
  }

  void startSession() {
    Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLES)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              if (status.getStatusCode()
                  == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                Log.i(getClass().toString(), "Existing subscription for activity detected.");
              } else {
                Log.i(getClass().toString(), "Successfully subscribed!");
              }
            } else {
              Log.i(getClass().toString(), "There was a problem subscribing.");
            }
          }
        });
  }

  void endSession() {
    Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLES)
        .setResultCallback(new ResultCallback<Status>() {
          @Override
          public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
              Log.i(getClass().toString(), "Successfully unsubscribed");
            } else {
              // Subscription not removed
              Log.i(getClass().toString(), "Failed to unsubscribe");
            }
          }
        });
  }
}
