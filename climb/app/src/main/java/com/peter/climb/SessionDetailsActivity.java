package com.peter.climb;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.peter.climb.MyApplication.AppState;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SessionDetailsActivity extends AppCompatActivity {

  static final String SENDS_KEY = "sends_key";
  static final String DATASETS_KEY = "datasets_key";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // pass null for the listener because I'm lazy and we don't need it
    AppState appState = ((MyApplication) getApplicationContext())
        .fetchGymDataAndAppState(getApplicationContext(), null);

    Bundle bundle = getIntent().getExtras();
    Session session = bundle.getParcelable(SENDS_KEY);
    ArrayList<DataSet> dataSets = bundle.getParcelableArrayList(DATASETS_KEY);
    String activeTime = Utils.activeTimeStringHM(session);
    String calories = "Unknown";

    setContentView(R.layout.activity_session_details);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.details_layout);
    LinearLayout sendsLayout = (LinearLayout) findViewById(R.id.sends_layout);

    String sendCount = "0";
    if (session != null) {
      int sendCountInt = 0;
      if (dataSets != null) {
        for (DataSet dataSet : dataSets) {
          for (DataPoint pt : dataSet.getDataPoints()) {
            sendCountInt++;
            String name = pt.getValue(appState.nameField).asString();
            String grade = pt.getValue(appState.gradeField).asString();
            String color = pt.getValue(appState.colorField).asString();
            long timeSinceStartOfSession = pt.getStartTime(TimeUnit.MILLISECONDS) - session
                .getStartTime(TimeUnit.MILLISECONDS);
            String timeString = Utils.millisDurationHMS(timeSinceStartOfSession);
            sendsLayout.addView(addSend(name, grade, timeString, color));
          }
        }
      }

      sendCount = String.valueOf(sendCountInt);
    }

    detailsLayout.addView(
        addDetail(R.drawable.ic_terrain_black_24dp, R.string.problems_sent_label, sendCount));
    detailsLayout.addView(
        addDetail(R.drawable.ic_timer_black_24dp, R.string.active_time_label, activeTime));
    detailsLayout.addView(
        addDetail(R.drawable.ic_calories_black_24dp, R.string.calories_burned_label, calories));

    String title = "Session";
    toolbar.setTitle(title);
  }

  View addSend(String name, String grade, String time, String color) {
    ConstraintLayout sendItem = (ConstraintLayout) getLayoutInflater()
        .inflate(R.layout.session_send_item, null);
    TextView sendName = (TextView) sendItem.findViewById(R.id.session_send_name);
    sendName.setText(name);
    TextView sendGrade = (TextView) sendItem.findViewById(R.id.session_send_grade);
    sendGrade.setText(grade);
    TextView sendTime = (TextView) sendItem.findViewById(R.id.session_send_time);
    sendTime.setText(time);

    try {
      int c = Color.parseColor(color);
      sendName.setTextColor(c);
    } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
      sendName.setTextColor(Color.DKGRAY);
    }

    return sendItem;
  }

  View addDetail(int icon, int label, String text) {
    ConstraintLayout detailItem = (ConstraintLayout) getLayoutInflater()
        .inflate(R.layout.session_detail_item, null);
    ImageView detailIcon = (ImageView) detailItem.findViewById(R.id.session_detail_icon);
    detailIcon.setImageDrawable(getDrawable(icon));
    TextView detailLabel = (TextView) detailItem.findViewById(R.id.session_detail_label);
    detailLabel.setText(getString(label));
    TextView detailText = (TextView) detailItem.findViewById(R.id.session_detail_text);
    detailText.setText(text);

    return detailItem;
  }
}
