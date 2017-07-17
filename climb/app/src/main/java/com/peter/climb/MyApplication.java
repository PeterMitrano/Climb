package com.peter.climb;

import static com.google.android.gms.fitness.data.DataSource.TYPE_DERIVED;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

  private AppState state = new AppState();

  public AppState getState() {
    return state;
  }
}

class AppState {

  static final int NO_GYM_ID = -1;
  private static final long FUDGE_FACTOR = 10;
  Gyms gyms;

  GoogleApiClient mClient = null;
  private Gym currentGym;
  private int currentGymId;
  private Session session;
  private DataType routeDataType;
  private List<Route> routesCompleted;
  private Field gradeField;
  private Field nameField;
  private Field wallField;
  private Field colorField;

  AppState() {
    routesCompleted = new ArrayList<>();
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

  void setCurrentGym(int current_gym_id) {
    this.currentGymId = current_gym_id;
    if (current_gym_id != NO_GYM_ID) {
      this.currentGym = gyms.getGyms(current_gym_id);
    }
  }

  void addRouteIntoSession(Route route) {
    routesCompleted.add(route);
  }

  void startSession(Context context) {
    session = new Session.Builder()
        .setName("Climbing Session")
        .setIdentifier(UUID.randomUUID().toString())
        .setDescription("Climbing Session")
        .setStartTime(System.currentTimeMillis() - FUDGE_FACTOR, TimeUnit.MILLISECONDS)
        .setActivity(FitnessActivities.ROCK_CLIMBING)
        .build();

  }

  void endSession() {
    if (routesCompleted.isEmpty()) {
      return;
    }

    DataSource dataSource = new Builder()
        .setDataType(routeDataType)
        .setType(TYPE_DERIVED)
        .build();

    // iterate over the save route information and create all the datapoints
    for (Route route : routesCompleted) {
      DataPoint pt = DataPoint.create(dataSource);
      pt.setTimestamp(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
      pt.getValue(gradeField).setString("V" + route.getGrade());
      pt.getValue(nameField).setString(route.getName());
      pt.getValue(colorField).setString(route.getColor());
    }

    DataSet dataset = DataSet.create(dataSource);
    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
        .setSession(session)
        .addDataSet(dataset)
        .build();

    PendingResult<Status> result = Fitness.SessionsApi.insertSession(mClient, insertRequest);

    result.setResultCallback(new ResultCallback<Status>() {
      @Override
      public void onResult(@NonNull Status status) {
        if (!status.isSuccess()) {
          Log.i(getClass().toString(), "There was a problem inserting the session: " +
              status.getStatusMessage());
        }
      }
    });
  }

  boolean hasCurrentGym() {
    return currentGymId != NO_GYM_ID;
  }
}
