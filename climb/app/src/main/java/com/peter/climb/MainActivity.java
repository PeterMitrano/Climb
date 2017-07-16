package com.peter.climb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    SwipeRefreshLayout.OnRefreshListener {

  public static final String PREFS_NAME = "MyPrefsFile";
  public static final String START_SESSION_ACTION = "start_session_action";
  private static final String GYM_ID_PREF_KEY = "gym_id_pref_key";
  public static final int SESSION_NOTIFICATION_ID = 1;
  private static final int START_SESSION_REQUEST_CODE = 1;
  private AppState appState;
  private GoogleApiClient mClient = null;

  private ImageView largeIconImageView;
  private TextView noGymSelectedTitle;
  private TextView noGymSelectedSubtitle;
  private ImageView noGymSelectedImage;
  private FloatingActionButton startSessionButton;
  private SwipeRefreshLayout swipeRefreshLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    appState = ((MyApplication) getApplicationContext()).getState();

    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
    largeIconImageView = (ImageView) findViewById(R.id.large_icon_image_view);
    noGymSelectedTitle = (TextView) findViewById(R.id.no_gym_selected_title);
    noGymSelectedSubtitle = (TextView) findViewById(R.id.no_gym_selected_subtitle);
    noGymSelectedImage = (ImageView) findViewById(R.id.no_gym_selected_image);
    startSessionButton = (FloatingActionButton) findViewById(R.id.start_session_button);

    swipeRefreshLayout.setOnRefreshListener(this);
    startSessionButton.setEnabled(false);
    startSessionButton.setOnClickListener(this);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    // kickoff HTTP request to server for all the gym data
    fetchGymData();
  }

  @Override
  protected void onResume() {
    super.onResume();
//        buildFitnessClient();
  }

  /**
   * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
   * to connect to Fitness APIs. The scopes included should match the scopes your app needs
   * (see documentation for details). Authentication will occasionally fail intentionally,
   * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
   * can address. Examples of this include the user never having signed in before, or having
   * multiple accounts on the device and needing to specify which account to use, etc.
   */
  private void buildFitnessClient() {
    if (mClient == null) {
      GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
          .requestEmail()
          .requestScopes(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE),
              new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
          .build();

      mClient = new GoogleApiClient.Builder(this)
          .addApi(Fitness.SENSORS_API)
          .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
          .addConnectionCallbacks(this)
          .enableAutoManage(this, 0, this)
          .build();
    }
  }

  private void fetchGymData() {
    // this provider request makes an network request, so it must be run async
    Cursor cursor = getContentResolver()
        .query(GymSuggestionProvider.CONTENT_URI, GymSuggestionProvider.PROJECTION, null, null,
            null);

    Msgs.Gyms.Builder gyms_builder = Msgs.Gyms.newBuilder();
    if (null == cursor) {
      Log.e(this.getClass().toString(), "null curser");
    } else {
      Log.e(this.getClass().toString(), "got results!");
      try {
        // iterate over the rows
        while (cursor.moveToNext()) {
          // Gets the protobuf blob from the column.
          byte data[] = cursor.getBlob(GymSuggestionProvider.PROTOBUF_BLOB_COLUMN);
          Msgs.Gym gym = Msgs.Gym.parseFrom(data);
          gyms_builder.addGyms(gym);
          cursor.close();
        }
      } catch (InvalidProtocolBufferException ignored) {
        Log.e(this.getClass().toString(), ignored.getLocalizedMessage());
      }
    }

    if (gyms_builder.getGymsCount() > 0) {
      // on success
      onGymDataSuccess(gyms_builder.build());
      swipeRefreshLayout.setRefreshing(false);
    } else {
      // on fail
      Snackbar.make(swipeRefreshLayout, "No gyms found.", Snackbar.LENGTH_LONG);
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  private void onGymDataSuccess(Msgs.Gyms gyms) {
    appState.gyms = gyms;

    // First check if this request came from a search for a specific gym
    Intent intent = getIntent();
    boolean has_current_gym = false;
    if (intent.getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
      try {
        int gymId = Integer.parseInt(uri.getLastPathSegment().toLowerCase());
        appState.setCurrentGym(gymId);
        has_current_gym = true;
      } catch (NumberFormatException e) {
        e.printStackTrace();
        // failure case 1, invalid search result
        Snackbar.make(swipeRefreshLayout, "Invalid search result", Snackbar.LENGTH_SHORT).show();
      }
    } else {
      // try to look up the currently selected gym from preferences
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      int gymId = settings.getInt(GYM_ID_PREF_KEY, -1);
      if (gymId != -1) {
        appState.setCurrentGym(gymId);
        has_current_gym = true;
      }
    }

    if (has_current_gym) {
      // set the Icon for the current gym
      displayCurrentGym();

      // allow the user to start the session
      startSessionButton.setEnabled(true);
    } else {
      displayNoCurrentGym();
    }
  }

  private void displayNoCurrentGym() {
    // they have no gym selected, so tell them how to add one
    noGymSelectedTitle.setVisibility(View.VISIBLE);
    noGymSelectedSubtitle.setVisibility(View.VISIBLE);
    noGymSelectedImage.setVisibility(View.VISIBLE);
    largeIconImageView.setVisibility(View.GONE);
  }

  private void displayCurrentGym() {
    // get the gym
    Msgs.Gym gym = appState.getCurrentGym();

    // set the logo
    String url = gym.getLargeIconUrl();
    ImageLoader.ImageListener listener = ImageLoader.getImageListener(
        largeIconImageView,
        0,
        R.drawable.ic_error_black_24dp);
    RequestorSingleton.getInstance(
        getApplicationContext()).getImageLoader().get(url, listener);

    noGymSelectedTitle.setVisibility(View.GONE);
    noGymSelectedSubtitle.setVisibility(View.GONE);
    noGymSelectedImage.setVisibility(View.GONE);
    largeIconImageView.setVisibility(View.VISIBLE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.toolbar_menu, menu);

    // Get the SearchView and set the searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.gym_search_view).getActionView();

    // Assumes current activity is the searchable activity
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setFocusable(true);
    searchView.setIconified(false);

    // Override the onQueryTextListener just to return true on Submit,
    // so it doesn't reload the activity
    searchView.setOnQueryTextListener(
        new SearchView.OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String query) {
            return true;
          }

          @Override
          public boolean onQueryTextChange(String newText) {
            return false;
          }
        }
    );

    return true;
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt(GYM_ID_PREF_KEY, appState.getCurrentGymId());
    editor.apply();
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.start_session_button) {
      NotificationCompat.Builder mBuilder =
          new NotificationCompat.Builder(this)
              .setSmallIcon(R.drawable.ic_terrain_black_24dp)
              .setContentTitle("Climb")
              .setContentText("Session In Progress");

      // Creates an explicit intent for an Activity in your app
      Intent resultIntent = new Intent(this, MapActivity.class);

      // The stack builder object will contain an artificial back stack for the
      // started Activity.
      // This ensures that navigating backward from the Activity leads out of
      // your application to the Home screen.
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
      // Adds the back stack for the Intent (but not the Intent itself)
      stackBuilder.addParentStack(MapActivity.class);
      // Adds the Intent that starts the Activity to the top of the stack
      stackBuilder.addNextIntent(resultIntent);
      PendingIntent resultPendingIntent =
          stackBuilder.getPendingIntent(
              0,
              PendingIntent.FLAG_UPDATE_CURRENT
          );
      mBuilder.setContentIntent(resultPendingIntent);
      NotificationManager mNotificationManager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      // mId allows you to update the notification later on.
      Notification notification = mBuilder.build();
      notification.flags = Notification.FLAG_ONGOING_EVENT;
      mNotificationManager.notify(SESSION_NOTIFICATION_ID, notification);

      Intent startSessionIntent = new Intent(this, MapActivity.class);
      startSessionIntent.setAction(START_SESSION_ACTION);
      startActivityForResult(startSessionIntent, START_SESSION_REQUEST_CODE);
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    Log.e(this.getClass().toString(), "Connected!!!");
    // Now you can make calls to the Fitness APIs.
  }

  @Override
  public void onConnectionSuspended(int i) {
    // If your connection to the sensor gets lost at some point,
    // you'll be able to determine the reason and react to it here.
    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
      Log.e(this.getClass().toString(), "Connection lost.  Cause: Network Lost.");
    } else if (i
        == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
      Log.e(this.getClass().toString(),
          "Connection lost.  Reason: Service Disconnected");
    }
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e(this.getClass().toString(), "Google Play services connection failed. Cause: "
        + connectionResult.toString());
  }

  @Override
  public void onRefresh() {
    fetchGymData();
  }
}
