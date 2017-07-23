package com.peter.climb;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;
import com.peter.Climb.Msgs.Gyms;
import java.util.ArrayList;
import java.util.List;

class FetchGymDataTask extends AsyncTask<Void, Integer, Gyms> {

  private final Context applicationContext;
  final private List<FetchGymDataListener> listeners = new ArrayList<>();

  interface FetchGymDataListener {

    void onGymsFound(Msgs.Gyms gyms);

    void onNoGymsFound();
  }

  FetchGymDataTask(Context applicationContenxt) {
    this.applicationContext = applicationContenxt;
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

  void addFetchGymDataListener(FetchGymDataListener listener) {
    listeners.add(listener);
  }

  protected void onPostExecute(Msgs.Gyms gyms) {
    for (FetchGymDataListener listener : listeners) {
      if (gyms != null && gyms.getGymsCount() > 0) {
        listener.onGymsFound(gyms);
      } else {
        listener.onNoGymsFound();
      }
    }
  }
}
