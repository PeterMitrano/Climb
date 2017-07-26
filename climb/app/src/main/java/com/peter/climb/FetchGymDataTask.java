package com.peter.climb;

import static com.peter.climb.MyApplication.AppState.NO_GYM_ID;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;
import com.peter.Climb.Msgs.Gyms;
import com.peter.climb.MyApplication.AppState;

class FetchGymDataTask extends AsyncTask<Void, Integer, Gyms> {

  private final Context applicationContext;
  private FetchGymDataListener fetchGymDataListener;
  private final AppState appState;

  interface FetchGymDataListener {

    void onGymsFound(Msgs.Gyms gyms);

    void onNoGymsFound();
  }

  FetchGymDataTask(AppState appState, Context applicationContext,
      @Nullable FetchGymDataListener fetchGymDataListener) {
    this.appState = appState;
    this.applicationContext = applicationContext;
    this.fetchGymDataListener = fetchGymDataListener;
  }

  @Override
  protected Msgs.Gyms doInBackground(Void... params) {
    Cursor cursor = applicationContext.getContentResolver()
        .query(GymSuggestionProvider.CONTENT_URI, GymSuggestionProvider.PROJECTION, null, null,
            null);

    if (null == cursor) {
      return null;
    } else {
      try {
        // iterate over the rows
        Msgs.Gyms.Builder gyms_builder = Msgs.Gyms.newBuilder();
        while (cursor.moveToNext()) {
          // Gets the protobuf blob from the column.
          byte data[] = cursor.getBlob(GymSuggestionProvider.PROTOBUF_BLOB_COLUMN);
          Msgs.Gym gym = Msgs.Gym.parseFrom(data);
          gyms_builder.addGyms(gym);
        }
        cursor.close();
        return gyms_builder.build();

      } catch (InvalidProtocolBufferException ignored) {
        cursor.close();
        return null;
      }
    }
  }

  @Override
  protected void onPostExecute(Msgs.Gyms gyms) {
    if (gyms != null && gyms.getGymsCount() > 0) {
      appState.gyms = gyms;
      if (appState.currentGymId != NO_GYM_ID) {
        appState.currentGym = gyms.getGyms(appState.currentGymId);
      }
      if (fetchGymDataListener != null) {
        fetchGymDataListener.onGymsFound(gyms);
      }
    } else {
      appState.currentGymId = NO_GYM_ID;
      appState.gyms = null;
      if (fetchGymDataListener != null) {
        fetchGymDataListener.onNoGymsFound();
      }
    }
  }
}
