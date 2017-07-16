package com.peter.climb;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class GymSuggestionProvider extends ContentProvider {

  public static final String[] PROJECTION = new String[]{BaseColumns._ID,
      SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA,
      SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA};

  private static final String AUTHORITY = "com.peter.climb.GymSuggestionProvider";
  public static final Uri CONTENT_URI =
      Uri.parse("content://" + AUTHORITY + "/gyms");

  public static final int PROTOBUF_BLOB_COLUMN = 4;
  private static final int SEARCH_DISTANCE_THRESHOLD = 15;

  public GymSuggestionProvider() {
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
      @Nullable String[] selectionArgs, @Nullable String sortOrder) {
    String query = uri.getLastPathSegment().toLowerCase();

    String url = "http://192.168.0.11:3000/gyms";
    RequestFuture<String> future = RequestFuture.newFuture();
    StringRequest gymDataRequest = new StringRequest(url, future, future);
    RequestorSingleton.getInstance(getContext()).addToRequestQueue(gymDataRequest);

    // This will wait synchronously
    try {
      String response = future.get(2000, TimeUnit.MILLISECONDS);

      // decode the HTTP response
      try {
        byte[] data = Base64.decode(response, Base64.DEFAULT);
        Msgs.Gyms gyms = Msgs.Gyms.parseFrom(data);

        if (selectionArgs != null && query.equals("search_suggest_query")) {
          String search_text = selectionArgs[0];
          return searchResultsCursor(gyms, search_text);
        } else {
          return allResultsCursor(gyms);
        }
      } catch (InvalidProtocolBufferException e) {
        // could not parse message.
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }

    return null;
  }

  private Cursor allResultsCursor(Msgs.Gyms gyms) {
    return searchResultsCursor(gyms, "");
  }

  private Cursor searchResultsCursor(Msgs.Gyms gyms, String search_text) {
    MatrixCursor matrixCursor = new MatrixCursor(PROJECTION);
    int i = 0;
    for (Msgs.Gym gym : gyms.getGymsList()) {
      if (search_text.isEmpty()) {
        matrixCursor.addRow(new Object[]{i, gym.getName(), i, gym});
      } else {
        int distance = LevenshteinDistance.getDefaultInstance()
            .apply(search_text, gym.getName());
        Log.e(this.getClass().toString(), "DIST: " + distance);
        if (distance < SEARCH_DISTANCE_THRESHOLD) {
          matrixCursor.addRow(new Object[]{i, gym.getName(), i, gym});
        }
      }
      i++;
    }

    return matrixCursor;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }
}
