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
    Msgs.Gym current_gym;
    int current_gym_id;

    enum SessionState {
        IN_PROGRESS,
        PAUSED,
        NOT_IN_PROGRESS,
    }

    SessionState session_in_progress;
}
