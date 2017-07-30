package com.peter.climb;

import static com.peter.climb.SessionDetailsActivity.DATASETS_KEY;
import static com.peter.climb.SessionDetailsActivity.SENDS_KEY;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.peter.Climb.Msgs.Gym;
import com.peter.Climb.Msgs.Gyms;
import com.peter.climb.Views.RightAlignedHintEdit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class EditSessionActivity extends MyActivity {

  private Session session;
  private Bundle bundle;
  private ArrayList<DataSet> dataSets;
  private EditText dateView;
  private EditText timeView;
  private RightAlignedHintEdit hoursEdit;
  private RightAlignedHintEdit minutesEdit;
  private RightAlignedHintEdit sendsEdit;
  private LinearLayout layout;
  private Spinner gymSpinner;
  private ArrayAdapter<String> gymSpinerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_session);

    layout = (LinearLayout) findViewById(R.id.edit_session_layout);
    hoursEdit = (RightAlignedHintEdit) findViewById(R.id.hours_edit);
    minutesEdit = (RightAlignedHintEdit) findViewById(R.id.minutes_edit);
    sendsEdit = (RightAlignedHintEdit) findViewById(R.id.sends_edit);
    dateView = (EditText) findViewById(R.id.date_view);
    timeView = (EditText) findViewById(R.id.time_view);
    gymSpinner = (Spinner) findViewById(R.id.gym_spinner);

    gymSpinerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.gym_spinner_item,
        R.id.gym_spinner_name);
    gymSpinner.setAdapter(gymSpinerAdapter);
    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setTitle("Edit Session");
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    bundle = getIntent().getExtras();
    if (bundle != null) {
      session = bundle.getParcelable(SENDS_KEY);
      dataSets = bundle.getParcelableArrayList(DATASETS_KEY);
      final long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
      final long activeTime = Utils.activeTime(session);
      final int startHour = Utils.millisToHours(startTime);
      final int startMinute = Utils.millisToMinutes(startTime);

      timeView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          TimePickerDialog timePickerDialog;
          timePickerDialog = new TimePickerDialog(EditSessionActivity.this,
              new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int h, int m) {
                  EditSessionActivity.this.timeView.setText(h + ":" + m);
                }
              }, startHour, startMinute, true);
          timePickerDialog.show();
        }
      });

      dateView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Calendar c = Calendar.getInstance();
          c.setTimeInMillis(startTime);
          int year = c.get(Calendar.YEAR);
          int month = c.get(Calendar.MONTH);
          int day = c.get(Calendar.DAY_OF_MONTH);
          DatePickerDialog datePickerDialog;
          datePickerDialog = new DatePickerDialog(EditSessionActivity.this,
              new OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int y, int m, int d) {
                  EditSessionActivity.this.dateView.setText(Utils.HMDDate(y, m, d));
                }
              }, year, month, day);
          datePickerDialog.show();
        }
      });

      hoursEdit.setText(String.valueOf(Utils.millisToHours(activeTime)));
      minutesEdit.setText(String.valueOf(Utils.millisToMinutes(activeTime)));
      timeView.setText(startHour + ":" + startMinute);
      dateView.setText(Utils.millsDate(startTime));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.edit_session_toolbar_menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      setResult(RESULT_CANCELED);
      finish();
      return true;
    } else if (item.getItemId() == R.id.save_session_item) {
      Intent intent = getIntent();
      intent.putExtra(SENDS_KEY, session);
      intent.putParcelableArrayListExtra(DATASETS_KEY, dataSets);
      setResult(RESULT_OK, intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onGoogleFitConnected() {
  }

  @Override
  public void onGoogleFitFailed() {
    Toast.makeText(this, "Failed to parse Google Fit Session", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onGymsFound(Gyms gyms) {
    // fill spinner
    for (Gym gym : gyms.getGymsList()) {
      gymSpinerAdapter.add(gym.getName());
    }
    gymSpinerAdapter.notifyDataSetChanged();
  }

  @Override
  public void onNoGymsFound() {
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
}
