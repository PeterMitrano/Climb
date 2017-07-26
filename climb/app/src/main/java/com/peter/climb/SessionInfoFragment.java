package com.peter.climb;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.peter.climb.MyApplication.AppState;
import java.util.Timer;
import java.util.TimerTask;

public class SessionInfoFragment extends Fragment implements OnClickListener {

  interface SessionInfoListener {

    void onSessionEnded();
  }

  private TextView timerView;
  private AppState appState;

  private SessionInfoListener sessionInfoListener;

  public SessionInfoFragment() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    FragmentActivity activity = getActivity();
    if (activity != null) {
      Context applicationContext = activity.getApplicationContext();
      appState = ((MyApplication) applicationContext)
          .fetchGymDataAndAppState(applicationContext, null);
    }

    return inflater.inflate(R.layout.session_info, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    view.setOnClickListener(this);

    Button endSessionButton = (Button) view.findViewById(R.id.end_session_button);
    endSessionButton.setOnClickListener(this);

    timerView = (TextView) view.findViewById(R.id.session_time);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.end_session_button) {
      if (sessionInfoListener != null) {
        sessionInfoListener.onSessionEnded();
      }
    }
  }

  public void setSessionInfoListener(
      SessionInfoListener sessionInfoListener) {
    this.sessionInfoListener = sessionInfoListener;
  }


  void startSessionTimer() {
    new Timer().scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
          return;
        }

        activity.runOnUiThread(new Runnable() {

          @Override
          public void run() {
            long millis = appState.getSessionLength();
            String hms = Utils.millisDurationHMS(millis);
            timerView.setText(hms);
          }

        });
      }

    }, 0, 1000);
  }
}
