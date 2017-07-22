package com.peter.climb;

import static java.text.DateFormat.getTimeInstance;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Create and execute a {@link SessionInsertRequest} to insert a session into the History API,
 * and then create and execute a {@link SessionReadRequest} to verify the insertion succeeded.
 * By using an AsyncTask to make our calls, we can schedule synchronous calls, so that we can
 * query for sessions after confirming that our insert was successful. Using asynchronous calls
 * and callbacks would not guarantee that the insertion had concluded before the read request
 * was made. An example of an asynchronous call using a callback can be found in the example
 * on deleting sessions below.
 */
class InsertAndVerifySessionTask extends AsyncTask<Void, Void, Void> {

  private final GoogleApiClient mClient;
  private final String SAMPLE_SESSION_NAME = "sample session name";
  private String packageName;

  InsertAndVerifySessionTask(GoogleApiClient client, String packageName) {
    this.mClient = client;
    this.packageName = packageName;
  }

  protected Void doInBackground(Void... params) {
    //First, create a new session and an insertion request.
    SessionInsertRequest insertRequest = insertFitnessSession();

    // [START insert_session]
    // Then, invoke the Sessions API to insert the session and await the result,
    // which is possible here because of the AsyncTask. Always include a timeout when
    // calling await() to avoid hanging that can occur from the service being shutdown
    // because of low memory or other conditions.
    Log.e(getClass().toString(), "Inserting the session in the History API");
    com.google.android.gms.common.api.Status insertStatus =
        Fitness.SessionsApi.insertSession(mClient, insertRequest)
            .await(1, TimeUnit.MINUTES);

    // Before querying the session, check to see if the insertion succeeded.
    if (!insertStatus.isSuccess()) {
      Log.e(getClass().toString(), "There was a problem inserting the session: " +
          insertStatus.getStatusMessage());
      return null;
    }

    // At this point, the session has been inserted and can be read.
    Log.e(getClass().toString(), "Session insert was successful!");
    // [END insert_session]

    // Begin by creating the query.
    SessionReadRequest readRequest = readFitnessSession();

    // [START read_session]
    // Invoke the Sessions API to fetch the session with the query and wait for the result
    // of the read request. Note: Fitness.SessionsApi.readSession() requires the
    // ACCESS_FINE_LOCATION permission.
    SessionReadResult sessionReadResult =
        Fitness.SessionsApi.readSession(mClient, readRequest)
            .await(1, TimeUnit.MINUTES);

    // Get a list of the sessions that match the criteria to check the result.
    Log.e(getClass().toString(), "Session read was successful. Number of returned sessions is: "
        + sessionReadResult.getSessions().size());
    for (Session session : sessionReadResult.getSessions()) {
      // Process the session
      dumpSession(session);

      // Process the data sets for this session
      List<DataSet> dataSets = sessionReadResult.getDataSet(session);
      for (DataSet dataSet : dataSets) {
        dumpDataSet(dataSet);
      }
    }
    // [END read_session]

    return null;
  }


  /**
   * Create a {@link SessionInsertRequest} for a run that consists of 10 minutes running,
   * 10 minutes walking, and 10 minutes of running. The request contains two {@link DataSet}s:
   * speed data and activity segments data.
   *
   * {@link Session}s are time intervals that are associated with all Fit data that falls into
   * that time interval. This data can be inserted when inserting a session or independently,
   * without affecting the association between that data and the session. Future queries for
   * that session will return all data relevant to the time interval created by the session.
   *
   * Sessions may contain {@link DataSet}s, which are comprised of {@link DataPoint}s and a
   * {@link DataSource}.
   * A {@link DataPoint} is associated with a Fit {@link DataType}, which may be
   * derived from the {@link DataSource}, as well as a time interval, and a value. A given
   * {@link DataSet} may only contain data for a single data type, but a {@link Session} can
   * contain multiple {@link DataSet}s.
   */
  private SessionInsertRequest insertFitnessSession() {
    Log.e(getClass().toString(), "Creating a new session for an afternoon run");
    // Setting start and end times for our run.
    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    // Set a range of the run, using a start time of 30 minutes before this moment,
    // with a 10-minute walk in the middle.
    long endTime = cal.getTimeInMillis();
    cal.add(Calendar.MINUTE, -10);
    long endWalkTime = cal.getTimeInMillis();
    cal.add(Calendar.MINUTE, -10);
    long startWalkTime = cal.getTimeInMillis();
    cal.add(Calendar.MINUTE, -10);
    long startTime = cal.getTimeInMillis();

    // Create a data source
    DataSource speedDataSource = new DataSource.Builder()
        .setAppPackageName(packageName)
        .setDataType(DataType.TYPE_CALORIES_EXPENDED)
        .setName(SAMPLE_SESSION_NAME + "- speed")
        .setType(DataSource.TYPE_RAW)
        .build();

    float runSpeedMps = 10;
    float walkSpeedMps = 3;
    // Create a data set of the run speeds to include in the session.
    DataSet speedDataSet = DataSet.create(speedDataSource);

    DataPoint firstRunSpeed = speedDataSet.createDataPoint()
        .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
    firstRunSpeed.getValue(Field.FIELD_CALORIES).setFloat(runSpeedMps);
    speedDataSet.add(firstRunSpeed);

    DataPoint walkSpeed = speedDataSet.createDataPoint()
        .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
    walkSpeed.getValue(Field.FIELD_CALORIES).setFloat(walkSpeedMps);
    speedDataSet.add(walkSpeed);

    DataPoint secondRunSpeed = speedDataSet.createDataPoint()
        .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
    secondRunSpeed.getValue(Field.FIELD_CALORIES).setFloat(runSpeedMps);
    speedDataSet.add(secondRunSpeed);

    // [START build_insert_session_request_with_activity_segments]
    // Create a second DataSet of ActivitySegments to indicate the runner took a 10-minute walk
    // in the middle of the run.
    DataSource activitySegmentDataSource = new DataSource.Builder()
        .setAppPackageName(packageName)
        .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
        .setName(SAMPLE_SESSION_NAME + "-activity segments")
        .setType(DataSource.TYPE_RAW)
        .build();
    DataSet activitySegments = DataSet.create(activitySegmentDataSource);

    DataPoint firstRunningDp = activitySegments.createDataPoint()
        .setTimeInterval(startTime, startWalkTime, TimeUnit.MILLISECONDS);
    firstRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
    activitySegments.add(firstRunningDp);

    DataPoint walkingDp = activitySegments.createDataPoint()
        .setTimeInterval(startWalkTime, endWalkTime, TimeUnit.MILLISECONDS);
    walkingDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.WALKING);
    activitySegments.add(walkingDp);

    DataPoint secondRunningDp = activitySegments.createDataPoint()
        .setTimeInterval(endWalkTime, endTime, TimeUnit.MILLISECONDS);
    secondRunningDp.getValue(Field.FIELD_ACTIVITY).setActivity(FitnessActivities.RUNNING);
    activitySegments.add(secondRunningDp);

    // [START build_insert_session_request]
    // Create a session with metadata about the activity.
    Session session = new Session.Builder()
        .setName(SAMPLE_SESSION_NAME)
        .setDescription("Long run around Shoreline Park")
        .setIdentifier("UniqueIdentifierHere")
        .setActivity(FitnessActivities.RUNNING)
        .setStartTime(startTime, TimeUnit.MILLISECONDS)
        .setEndTime(endTime, TimeUnit.MILLISECONDS)
        .build();

    // Build a session insert request
    SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
        .setSession(session)
        .addDataSet(speedDataSet)
        .addDataSet(activitySegments)
        .build();
    // [END build_insert_session_request]
    // [END build_insert_session_request_with_activity_segments]

    return insertRequest;
  }

  /**
   * Return a {@link SessionReadRequest} for all speed data in the past week.
   */
  private SessionReadRequest readFitnessSession() {
    Log.e(getClass().toString(), "Reading History API results for session: " + SAMPLE_SESSION_NAME);
    // [START build_read_session_request]
    // Set a start and end time for our query, using a start time of 1 week before this moment.
    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    long endTime = cal.getTimeInMillis();
    cal.add(Calendar.WEEK_OF_YEAR, -1);
    long startTime = cal.getTimeInMillis();

    // Build a session read request
    SessionReadRequest readRequest = new SessionReadRequest.Builder()
        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        .read(DataType.TYPE_CALORIES_EXPENDED)
        .setSessionName(SAMPLE_SESSION_NAME)
        .build();
    // [END build_read_session_request]

    return readRequest;
  }

  private void dumpDataSet(DataSet dataSet) {
    Log.e(getClass().toString(), "Data returned for Data type: " + dataSet.getDataType().getName());
    for (DataPoint dp : dataSet.getDataPoints()) {
      DateFormat dateFormat = getTimeInstance();
      Log.e(getClass().toString(), "Data point:");
      Log.e(getClass().toString(), "\tType: " + dp.getDataType().getName());
      Log.e(getClass().toString(),
          "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
      Log.e(getClass().toString(),
          "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
      for (Field field : dp.getDataType().getFields()) {
        Log.e(getClass().toString(), "\tField: " + field.getName() +
            " Value: " + dp.getValue(field));
      }
    }
  }

  private void dumpSession(Session session) {
    DateFormat dateFormat = getTimeInstance();
    Log.e(getClass().toString(), "Data returned for Session: " + session.getName()
        + "\n\tDescription: " + session.getDescription()
        + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
        + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
  }


}
