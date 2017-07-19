package com.peter.climb;

import static com.google.android.gms.fitness.data.DataSource.TYPE_DERIVED;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataSource.Builder;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.peter.Climb.Msgs.Gym;
import com.peter.Climb.Msgs.Gyms;
import com.peter.Climb.Msgs.Route;
import com.peter.climb.FetchGymDataTask.FetchGymDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

  private AppState state = new AppState();

  public AppState getState(Context applicationContext) {
    state.setApplicationContext(applicationContext);
    return state;
  }
}

class AppState implements FetchGymDataListener {

  static final String SESSION_START_TIME_EXTRA = "session_start_time_extra";
  static final String CURRENT_GYM_ID_EXTRA = "current_gym_id_extra";
  static final String RESUME_FROM_NOTIFICATION_ACTION = "resume_from_notification_action";
  static final int NO_GYM_ID = -1;
  static final long NO_START_TIME = -1;

  private Gyms gyms;

  GoogleApiClient mClient = null;

  // part of global app state
  private Gym currentGym;
  private int currentGymId;

  private DataType routeDataType;
  private Field gradeField;
  private Field nameField;
  private Field wallField;
  private Field colorField;
  private List<Send> sends;
  long startTimeMillis;
  boolean inProgress;
  SetCurrentGymListener setCurrentGymListener;

  private Context applicationContext;

  interface SetCurrentGymListener {

    void onSetCurrentGymSuccess();

    void onSetCurrentGymFail();
  }

  AppState() {
    gyms = null;
    sends = new ArrayList<>();
    currentGymId = NO_GYM_ID;
    inProgress = false;
    startTimeMillis = NO_START_TIME;
  }

  void setApplicationContext(Context applicationContext) {
    this.applicationContext = applicationContext;
  }

  void createDataType(String package_name) {
    gradeField = Field.zzm("Grade", Field.FORMAT_STRING);
    nameField = Field.zzm("Name", Field.FORMAT_STRING);
    wallField = Field.zzm("Wall", Field.FORMAT_STRING);
    colorField = Field.zzm("Color", Field.FORMAT_STRING);
    // Build a request to create a new data type
    DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
        // The prefix of your data type name must match your app's package name
        .setName(package_name + ".route_data_type")
        .addField(gradeField)
        .addField(nameField)
        .addField(wallField)
        .addField(colorField)
        .build();

    // Invoke the Config API with:
    // - The Google API client object
    // - The create data type request
    PendingResult<DataTypeResult> pendingResult = Fitness.ConfigApi
        .createCustomDataType(mClient, request);

    // Check the result asynchronously
    pendingResult.setResultCallback(
        new ResultCallback<DataTypeResult>() {
          @Override
          public void onResult(@NonNull DataTypeResult dataTypeResult) {
            routeDataType = dataTypeResult.getDataType();
          }
        }
    );
  }

  int getCurrentGymId() {
    return currentGymId;
  }

  Gym getCurrentGym() {
    return currentGym;
  }

  void setCurrentGym(int currentGymId, @Nullable SetCurrentGymListener listener) {
    if (gyms == null) {
      this.currentGymId = currentGymId;
      setCurrentGymListener = listener;
      refresh(null);
    } else {
      this.currentGymId = currentGymId;
      if (currentGymId != NO_GYM_ID) {
        this.currentGym = gyms.getGyms(currentGymId);
        listener.onSetCurrentGymSuccess();
      } else {
        listener.onSetCurrentGymFail();
      }

    }
  }

  void addRouteIntoSession(Route route) {
    sends.add(new Send(route, System.currentTimeMillis()));
  }

  void startSession() {
    if (!inProgress) {
      startTimeMillis = System.currentTimeMillis();
      inProgress = true;
    }
  }

  @Nullable
  PendingResult<Status> endSession() {
    if (sends.isEmpty()) {
      return null;
    }

    // Create the session to insert
    long endTime = System.currentTimeMillis();
    Session session = new Session.Builder()
        .setName("Climbing Session")
        .setIdentifier(UUID.randomUUID().toString())
        .setDescription("Climbing Session")
        .setStartTime(startTimeMillis, TimeUnit.MILLISECONDS)
        .setActivity(FitnessActivities.ROCK_CLIMBING)
        .setEndTime(endTime, TimeUnit.MILLISECONDS)
        .build();

    DataSource dataSource = new Builder().setDataType(routeDataType).setType(TYPE_DERIVED).build();
    DataSet dataset = DataSet.create(dataSource);

    // iterate over the save route information and create all the datapoints
    for (Send route : sends) {
      DataPoint pt = dataset.createDataPoint();
      pt.setTimestamp(route.getTimeMillis(), TimeUnit.MILLISECONDS);
      pt.getValue(gradeField).setString("V" + route.getGrade());
      pt.getValue(nameField).setString(route.getName());
      pt.getValue(colorField).setString(route.getColor());
      dataset.add(pt);
    }

    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
        .setSession(session)
        .addDataSet(dataset)
        .build();

    // attempt to insert the session into the user's google fit
    return Fitness.SessionsApi.insertSession(mClient, insertRequest);
  }

  boolean hasCurrentGym() {
    return currentGymId != NO_GYM_ID;
  }

  long getSessionLength() {
    return System.currentTimeMillis() - startTimeMillis;
  }

  void refresh(@Nullable FetchGymDataListener listener) {
    // this provider request makes an network request, so it must be run async
    FetchGymDataTask gymsAsyncTask = new FetchGymDataTask(applicationContext);
    gymsAsyncTask.addFetchGymDataListener(this);
    if (listener != null) {
      gymsAsyncTask.addFetchGymDataListener(listener);
    }
    gymsAsyncTask.execute();
  }

  void restoreFromIntent(Intent intent, SetCurrentGymListener listener) {
    long t = intent.getLongExtra(SESSION_START_TIME_EXTRA, -1);
    int gym_id = intent.getIntExtra(CURRENT_GYM_ID_EXTRA, -1);
    if (t != -1 && this.startTimeMillis == NO_START_TIME) {
      this.startTimeMillis = t;
      setCurrentGym(gym_id, listener);
    }
  }

  @Override
  public void onGymsFound(Gyms gyms) {
    this.gyms = gyms;
    if (currentGymId != NO_GYM_ID) {
      this.currentGym = gyms.getGyms(currentGymId);
      setCurrentGymListener.onSetCurrentGymSuccess();
    }
    else {
      setCurrentGymListener.onSetCurrentGymFail();
    }
  }

  @Override
  public void onNoGymsFound() {
    currentGymId = NO_GYM_ID;
    setCurrentGymListener.onSetCurrentGymFail();
  }
}
