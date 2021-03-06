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
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class GymsContentProvider extends ContentProvider {

  public static final String[] PROJECTION = new String[]{BaseColumns._ID,
      SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA,
      SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA};
  public static final int PROTOBUF_BLOB_COLUMN = 3;
  private static final String AUTHORITY = "com.peter.climb.GymsContentProvider";
  public static final Uri CONTENT_URI =
      Uri.parse("content://" + AUTHORITY + "/gyms");
  private static final int MAX_RESULTS = 5;

  public GymsContentProvider() {
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

    String url = "http://gym-server-dev.us-east-1.elasticbeanstalk.com/gyms";
//    String url = "http://192.168.1.14:8081/gyms";
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
          return searchResultsCursor(gyms, search_text, MAX_RESULTS);
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

  private Cursor allResultsCursor(Msgs.Gyms gyms) {
    return searchResultsCursor(gyms, "", -1);
  }

  private Cursor searchResultsCursor(Msgs.Gyms gyms, String searchText, int maxResults) {
    MatrixCursor matrixCursor = new MatrixCursor(PROJECTION);
    TreeMap<Integer, Object[]> sorted_rows = new TreeMap<>();

    int i = 0;
    for (Msgs.Gym gym : gyms.getGymsList()) {
      if (searchText.isEmpty()) {
        sorted_rows.put(i, new Object[]{i, gym.getName(), gym.getUuid(), gym.toByteArray()});
      } else {
        int distance = LevenshteinDistance.getDefaultInstance()
            .apply(searchText, gym.getName());
        sorted_rows.put(distance, new Object[]{i, gym.getName(), gym.getUuid(), gym.toByteArray()});
      }
      i++;
    }

    i = 0;
    for (Object row[] : sorted_rows.values()) {
      matrixCursor.addRow(row);
      i++;
      if (i == maxResults) {
        break;
      }
    }

    return matrixCursor;
  }
}
