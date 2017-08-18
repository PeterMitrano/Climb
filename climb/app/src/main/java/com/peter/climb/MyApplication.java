package com.peter.climb;

import android.app.Application;
import android.content.Context;
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
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.Climb.Msgs.Gym;
import com.peter.Climb.Msgs.Gyms;
import com.peter.Climb.Msgs.Route;
import com.peter.Climb.Msgs.Wall;
import com.peter.climb.CreateDataTypesTask.CreateDataTypesListener;
import com.peter.climb.FetchGymDataTask.FetchGymDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

  final private AppState state = new AppState();

  public AppState getState() {
    return state;
  }

  public AppState fetchGymData(@Nullable FetchGymDataListener fetchGymDataListener) {
    state.refreshGyms(fetchGymDataListener);
    return state;
  }

  interface GoogleFitListener {

    void onGoogleFitConnected();

    void onGoogleFitFailed();
  }

  class AppState {

    static final String NO_GYM_UUID = "00000000-0000-0000-0000-000000000000";
    static final long NO_START_TIME = -1;

    final private List<Send> sends;


    boolean inProgress;
    long startTimeMillis;
    DataType routeDataType;
    DataType metadataType;
    GoogleApiClient mClient = null;
    Gyms gyms = null;
    Gym currentGym;
    Field uuidField;
    Field gradeField;
    Field nameField;
    Field wallField;
    Field colorField;
    Field gymNameField;
    Field imageUrlField;

    private AppState() {
      gyms = null;
      sends = new ArrayList<>();
      inProgress = false;
      startTimeMillis = NO_START_TIME;
    }

    Gym getCurrentGym() {
      return currentGym;
    }

    void setCurrentGym(String currentGymUuid) {
      if (!currentGymUuid.equals(NO_GYM_UUID)) {
        for (Gym gym : gyms.getGymsList()) {
          if (gym.getUuid().equals(currentGymUuid)) {
            this.currentGym = gym;
            return;
          }
        }
      }
      throw new IllegalArgumentException("Gym UUID not found: " + currentGymUuid);
    }

    boolean hasGym(String gymUuid) {
      if (!gymUuid.equals(NO_GYM_UUID)) {
        for (Gym gym : gyms.getGymsList()) {
          if (gym.getUuid().equals(gymUuid)) {
            return true;
          }
        }
      }
      return false;
    }

    void addRouteIntoSession(Route route, Wall wall) {
      sends.add(new Send(route, wall, System.currentTimeMillis()));
    }

    void removeRouteFromSession(Route route, Wall wall) {
      for (int i = 0; i < sends.size(); ) {
        Send send = sends.get(i);
        if (send.getRoute() == route) {
          sends.remove(i);
        } else {
          ++i;
        }
      }
    }

    boolean isSessionEmpty() {
      return sends.isEmpty();
    }

    void startSession() {
      if (!inProgress) {
        startTimeMillis = System.currentTimeMillis();
        inProgress = true;
      }
    }

    void endSession(ResultCallback<Status> resultCallback, Context applicationContext) {
      DataSource metadataSource = new DataSource.Builder()
          .setAppPackageName(applicationContext)
          .setDataType(metadataType)
          .setName("metadata")
          .setType(DataSource.TYPE_DERIVED)
          .build();

      DataSource dataSource = new DataSource.Builder()
          .setAppPackageName(applicationContext)
          .setDataType(routeDataType)
          .setName("user_input")
          .setType(DataSource.TYPE_RAW)
          .build();

      DataSet dataset = DataSet.create(dataSource);
      DataSet metadataset = DataSet.create(metadataSource);

      DataPoint metadata = metadataset.createDataPoint();
      metadata.setTimeInterval(System.currentTimeMillis(), System.currentTimeMillis(),
          TimeUnit.MILLISECONDS);
      metadata.getValue(imageUrlField).setString(currentGym.getLargeIconUrl());
      metadata.getValue(gymNameField).setString(currentGym.getName());
      metadata.getValue(uuidField).setString(currentGym.getUuid());
      metadataset.add(metadata);

      // iterate over the save route information and create all the datapoints
      for (Send route : sends) {
        DataPoint pt = dataset.createDataPoint();
        pt.setTimeInterval(route.getTimeMillis(), route.getTimeMillis(), TimeUnit.MILLISECONDS);
        pt.setTimestamp(route.getTimeMillis(), TimeUnit.MILLISECONDS);
        pt.getValue(gradeField).setString("V" + route.getGrade());
        pt.getValue(nameField).setString(route.getName());
        pt.getValue(colorField).setString(route.getColor());
        pt.getValue(wallField).setString(route.getWallName());
        dataset.add(pt);
      }

      // Create the session to insert
      long endTime = System.currentTimeMillis();
      com.google.android.gms.fitness.data.Session session = new com.google.android.gms.fitness.data.Session.Builder()
          .setName("Climbing Session at " + currentGym.getName())
          .setDescription("Session at " + currentGym.getName())
          .setIdentifier(currentGym.getUuid())
          .setActivity(FitnessActivities.ROCK_CLIMBING)
          .setStartTime(startTimeMillis, TimeUnit.MILLISECONDS)
          .setEndTime(endTime, TimeUnit.MILLISECONDS)
          .build();

      SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
          .setSession(session)
          .addDataSet(dataset)
          .addDataSet(metadataset)
          .build();

      // attempt to insert the session into the user's google fit
      PendingResult<Status> result = Fitness.SessionsApi.insertSession(mClient, insertRequest);
      result.setResultCallback(resultCallback);
    }

    void deleteSession(com.google.android.gms.fitness.data.Session session, long startTime,
        long endTime,
        ResultCallback<Status> resultCallback) {
      DataDeleteRequest deleteRequest = new DataDeleteRequest.Builder()
          .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
          .deleteAllData()
          .addSession(session)
          .build();

      PendingResult<Status> pendingResult = Fitness.HistoryApi.deleteData(mClient, deleteRequest);
      pendingResult.setResultCallback(resultCallback);
    }

    boolean hasCurrentGym() {
      return hasGym(currentGym.getUuid());
    }

    long getSessionLength() {
      return System.currentTimeMillis() - startTimeMillis;
    }

    void getSessionHistory(final long startTime, final long endTime,
        final ResultCallback<SessionReadResult> resultCallback) {
      SessionReadRequest readRequest = new SessionReadRequest.Builder()
          .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
          .read(metadataType)
          .read(routeDataType)
          .build();

      PendingResult<SessionReadResult> pendingResult = Fitness.SessionsApi
          .readSession(mClient, readRequest);
      pendingResult.setResultCallback(resultCallback);
    }

    void clearSends() {
      sends.clear();
    }

    void refreshGyms(final @Nullable FetchGymDataListener fetchGymDataListener) {
      // this provider request makes an network request, so it must be run async
      new FetchGymDataTask(this, getApplicationContext(), fetchGymDataListener).execute();
    }

    void createDataTypes(@NonNull final CreateDataTypesListener createDataTypesListener) {
      new CreateDataTypesTask(this, getPackageName(), createDataTypesListener).execute();
    }

    boolean hasDataTypes() {
      return metadataType != null && routeDataType != null;
    }

    void reconnect() {
      reconnect(false);
    }

    void reconnect(boolean forceSignOut) {
      if (forceSignOut) {
        mClient.clearDefaultAccountAndReconnect();
      } else {
        mClient.reconnect();
      }
    }
  }
}
