package com.peter.climb;

import static com.peter.climb.SessionDetailsActivity.DATASETS_KEY;
import static com.peter.climb.SessionDetailsActivity.METADATASET_KEY;
import static com.peter.climb.SessionDetailsActivity.SESSION_KEY;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.peter.Climb.Msgs.Gyms;
import com.peter.climb.Views.RightAlignedHintEdit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EditSessionActivity extends ActivityWrapper {

  private Bundle bundle;
  private ArrayList<DataSet> routeDataSets;
  private DataPoint metadata;
  private EditText dateView;
  private EditText timeView;
  private LinearLayout layout;
  private RightAlignedHintEdit hoursEdit;
  private RightAlignedHintEdit minutesEdit;
  private Session session;
  private long newEndTimeMillis;
  private long newStartDateMillis;
  private long newStartHrMinMillis;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.edit_session_toolbar_menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.discard_changes)
          .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              setResult(RESULT_CANCELED);
              finish();
            }
          })
          .setNegativeButton(R.string.cancel, null);
      builder.create().show();
      return true;
    } else if (item.getItemId() == R.id.save_session_item) {
      // gather current state of UI and update a copy of the dataset

      long newStartTimeMillis = newStartDateMillis + newStartHrMinMillis;
      newEndTimeMillis = newStartTimeMillis;
      newEndTimeMillis += Utils.HMToMillis(hoursEdit.getValue(), minutesEdit.getValue());

      DataSource newMetadataSource = new DataSource.Builder()
          .setAppPackageName(getApplicationContext())
          .setDataType(appState.metadataType)
          .setName("metadata_edited")
          .setType(DataSource.TYPE_DERIVED)
          .build();

      DataSource newDataSource = new DataSource.Builder()
          .setAppPackageName(getApplicationContext())
          .setDataType(appState.routeDataType)
          .setName("user_input_edited")
          .setType(DataSource.TYPE_DERIVED)
          .build();

      DataSet newDataset = DataSet.create(newDataSource);
      DataSet newMetadataset = DataSet.create(newMetadataSource);

      // copy some existing metadata info that this interface can't yet change
      String imageUrl = metadata.getValue(appState.imageUrlField).asString();
      String gymName = metadata.getValue(appState.gymNameField).asString();
      String uuid = metadata.getValue(appState.uuidField).asString();

      // new metadataset based on new times
      DataPoint newMetadata = newMetadataset.createDataPoint();
      newMetadata.setTimeInterval(newStartTimeMillis, newEndTimeMillis, TimeUnit.MILLISECONDS);
      newMetadata.getValue(appState.imageUrlField).setString(imageUrl);
      newMetadata.getValue(appState.gymNameField).setString(gymName);
      newMetadata.getValue(appState.uuidField).setString(uuid);
      newMetadataset.add(newMetadata);

      // iterate over the save route information and copy the route datapoints
      for (DataSet ds : routeDataSets) {
        for (DataPoint pt : ds.getDataPoints()) {
          DataPoint nPt = newDataset.createDataPoint();
          long newPtMillis = newStartTimeMillis + (pt.getTimestamp(TimeUnit.MILLISECONDS) - session
              .getStartTime(TimeUnit.MILLISECONDS));
          nPt.setTimeInterval(newPtMillis, newPtMillis, TimeUnit.MILLISECONDS);
          nPt.setTimestamp(newPtMillis, TimeUnit.MILLISECONDS);
          nPt.getValue(appState.gradeField).setString(pt.getValue(appState.gradeField).asString());
          nPt.getValue(appState.nameField).setString(pt.getValue(appState.nameField).asString());
          nPt.getValue(appState.colorField).setString(pt.getValue(appState.colorField).asString());
          nPt.getValue(appState.wallField).setString(pt.getValue(appState.wallField).asString());
          newDataset.add(nPt);
        }
      }

      // Create the session to insert
      final com.google.android.gms.fitness.data.Session newSession = new com.google.android.gms.fitness.data.Session.Builder()
          .setName("Climbing Session")
          .setDescription("Session at " + gymName)
          .setIdentifier(UUID.randomUUID().toString())
          .setActivity(FitnessActivities.ROCK_CLIMBING)
          .setStartTime(newStartTimeMillis, TimeUnit.MILLISECONDS)
          .setEndTime(newEndTimeMillis, TimeUnit.MILLISECONDS)
          .build();

      SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
          .setSession(newSession)
          .addDataSet(newDataset)
          .addDataSet(newMetadataset)
          .build();

      // delete the old session and insert the new one
      final ArrayList<DataSet> newRouteDatasets = new ArrayList<>();
      newRouteDatasets.add(newDataset);
      final PendingResult<Status> insertResult = Fitness.SessionsApi
          .insertSession(appState.mClient, insertRequest);
      long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
      long endTime = session.getEndTime(TimeUnit.MILLISECONDS);
      appState.deleteSession(session, startTime, endTime, new ResultCallback<Status>() {
        @Override
        public void onResult(@NonNull Status deleteStatus) {
          if (deleteStatus.isSuccess()) {
            Status insertStatus = insertResult.await(1000, TimeUnit.MILLISECONDS);
            if (insertStatus.isSuccess()) {
              Toast.makeText(getApplicationContext(), "Session updated", Toast.LENGTH_SHORT).show();
              Intent intent = getIntent();
              intent.putExtra(SESSION_KEY, newSession);
              intent.putParcelableArrayListExtra(DATASETS_KEY, newRouteDatasets);
              setResult(RESULT_OK, intent);
              finish();
            } else {
              Snackbar.make(layout, "Failed to update session", Snackbar.LENGTH_SHORT).show();
            }
          } else {
            Snackbar.make(layout, "Failed to update session", Snackbar.LENGTH_SHORT).show();
          }
        }
      });
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_session);

    layout = (LinearLayout) findViewById(R.id.edit_session_layout);
    hoursEdit = (RightAlignedHintEdit) findViewById(R.id.hours_edit);
    minutesEdit = (RightAlignedHintEdit) findViewById(R.id.minutes_edit);
    dateView = (EditText) findViewById(R.id.date_view);
    timeView = (EditText) findViewById(R.id.time_view);

    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setTitle("Edit Session");
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    bundle = getIntent().getExtras();
    if (bundle != null) {
      session = bundle.getParcelable(SESSION_KEY);
      DataSet metadataset = bundle.getParcelable(METADATASET_KEY);
      if (metadataset != null && metadataset.getDataPoints().size() == 1) {
        metadata = metadataset.getDataPoints().get(0);
      } else {
        // this is a serious error, handle it well.
        metadata = null;
      }

      routeDataSets = bundle.getParcelableArrayList(DATASETS_KEY);

      final long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
      final long activeTime = Utils.activeTime(session);
      final int startHour = Utils.millisToHours(startTime);
      final int startMinute = Utils.millisToMinutes(startTime);
      newStartHrMinMillis = Utils.HMToMillis(startHour, startMinute);

      timeView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          TimePickerDialog timePickerDialog;
          timePickerDialog = new TimePickerDialog(EditSessionActivity.this,
              new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int h, int m) {
                  newStartHrMinMillis = Utils.HMToMillis(h, m);
                  EditSessionActivity.this.timeView.setText(Utils.timeStr(h, m));
                }
              }, startHour, startMinute, true);
          timePickerDialog.show();
        }
      });

      newStartDateMillis = startTime;
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
                  Calendar cal = Calendar.getInstance();
                  cal.set(y, m, d);
                  newStartDateMillis = cal.getTimeInMillis();
                  EditSessionActivity.this.dateView.setText(Utils.HMDDate(y, m, d));
                }
              }, year, month, day);
          datePickerDialog.show();
        }
      });

      int activeHours = Utils.millisToHours(activeTime);
      int activeMinutes = Math.max(1, Utils.millisToMinutes(activeTime));
      hoursEdit.setText(String.valueOf(activeHours));
      minutesEdit.setText(String.valueOf(activeMinutes));
      timeView.setText(Utils.timeStr(startHour, startMinute));
      dateView.setText(Utils.millsDate(startTime));
    }
  }
}
