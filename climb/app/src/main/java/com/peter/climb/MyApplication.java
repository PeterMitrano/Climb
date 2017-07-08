package com.peter.climb;

import android.app.Application;
import com.peter.Climb.Msgs;

public class MyApplication extends Application {

  private AppState state = new AppState();

  public AppState getState() {
    return state;
  }
}

class AppState {

  Msgs.Gyms gyms;
  SessionState sessionInProgress;
  private Msgs.Gym currentGym;
  private int currentGymId;

  public int getCurrentGymId() {
    return currentGymId;
  }

  public Msgs.Gym getCurrentGym() {
    return currentGym;
  }

  public void setCurrentGym(int current_gym_id) {
    this.currentGymId = current_gym_id;
    this.currentGym = gyms.getGyms(current_gym_id);
  }

  enum SessionState {
    IN_PROGRESS,
    PAUSED,
    NOT_IN_PROGRESS,
  }
}
