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

public class SessionDetailsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_session_details);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    LinearLayout detailsLayout = (LinearLayout) findViewById(R.id.details_layout);
    LinearLayout sendsLayout = (LinearLayout) findViewById(R.id.sends_layout);

    String activeTime = "1h 23m";
    String sendCount = "12";

    detailsLayout.addView(
        addDetail(R.drawable.ic_terrain_black_24dp, R.string.problems_sent_label, sendCount));
    detailsLayout.addView(
        addDetail(R.drawable.ic_timer_black_24dp, R.string.active_time_label, activeTime));

    sendsLayout.addView(addSend("Pikachu", "V3", "10:20pm 23s", "#ff00ff"));

    String title = "Session at Ascend PGH";
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
