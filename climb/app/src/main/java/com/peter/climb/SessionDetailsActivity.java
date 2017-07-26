package com.peter.climb;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.peter.climb.MyApplication.GoogleFitListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SessionDetailsActivity extends MyActivity implements GoogleFitListener {

  static final String SENDS_KEY = "sends_key";
  static final String DATASETS_KEY = "datasets_key";

  private LinearLayout layout;
  private ImageView appBarImage;
  private Toolbar toolbar;
  private Session session;
  private LinearLayout sendsLayout;
  private LinearLayout detailsLayout;
  private ArrayList<DataSet> dataSets;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_session_details);

    // pass null for the listener because we don't need it
    appState = ((MyApplication) getApplicationContext()).fetchGymData(null);
    setupGoogleFit(this);

    Bundle bundle = getIntent().getExtras();
    session = bundle.getParcelable(SENDS_KEY);
    dataSets = bundle.getParcelableArrayList(DATASETS_KEY);

    layout = (LinearLayout) findViewById(R.id.layout);
    appBarImage = (ImageView) findViewById(R.id.app_bar_image);
    toolbar = (Toolbar) findViewById(R.id.toolbar);

    if (session != null) {

      toolbar.setTitle(session.getDescription());
      setSupportActionBar(toolbar);

      detailsLayout = (LinearLayout) findViewById(R.id.details_layout);
      sendsLayout = (LinearLayout) findViewById(R.id.sends_layout);

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.session_details_toolbar_menu, menu);

    return true;
  }

  View addSend(String name, String grade, String time, String color) {
    @SuppressLint("InflateParams")
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

  @Override
  void onPermissionsDenied() {
    Snackbar snack = Snackbar
        .make(layout, R.string.no_fit_permission_msg, Snackbar.LENGTH_INDEFINITE);
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
  public void onGoogleFitConnected() {
    if (session == null) {
      return;
    }

    int sendCountInt = 0;
    if (dataSets != null && appState.hasDataTypes()) {
      for (DataSet dataSet : dataSets) {
        if (dataSet.getDataType().equals(appState.routeDataType)) {
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
        else if (dataSet.getDataType().equals(appState.metadataType)) {
          DataPoint metadata = dataSet.getDataPoints().get(0);
          String url = metadata.getValue(appState.imageUrlField).asString();

          ImageLoader.ImageListener listener = ImageLoader.getImageListener(
              appBarImage,
              0,
              R.drawable.ic_error_black_24dp);

          RequestorSingleton.getInstance(
              getApplicationContext()).getImageLoader().get(url, listener);

        }
      }
    } else {
      onGoogleFitFailed();
    }

    String activeTime = Utils.activeTimeStringHM(session);
    String sendCount = String.valueOf(sendCountInt);
    String calories = "Unknown";

    detailsLayout.addView(
        addDetail(R.drawable.ic_terrain_black_24dp, R.string.problems_sent_label, sendCount));
    detailsLayout.addView(
        addDetail(R.drawable.ic_timer_black_24dp, R.string.active_time_label, activeTime));
    detailsLayout.addView(
        addDetail(R.drawable.ic_calories_black_24dp, R.string.calories_burned_label, calories));

  }

  @Override
  public void onGoogleFitFailed() {
    Toast.makeText(this, "Failed to parse Google Fit Session", Toast.LENGTH_SHORT).show();
  }
}
