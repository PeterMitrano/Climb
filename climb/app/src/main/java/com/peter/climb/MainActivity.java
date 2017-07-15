package com.peter.climb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  public static final String PREFS_NAME = "MyPrefsFile";
  public static final String START_SESSION_ACTION = "start_session_action";
  private static final String GYM_ID_PREF_KEY = "gym_id_pref_key";
  public static final int SESSION_NOTIFICATION_ID = 1;
  private AppState appState;
  private GoogleApiClient mClient = null;

  private ImageView largeIconImageView;
  private TextView noGymSelectedTitle;
  private TextView noGymSelectedSubtitle;
  private ImageView noGymSelectedImage;
  private FloatingActionButton startSessionButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    appState = ((MyApplication) getApplicationContext()).getState();

    largeIconImageView = (ImageView) findViewById(R.id.large_icon_image_view);
    noGymSelectedTitle = (TextView) findViewById(R.id.no_gym_selected_title);
    noGymSelectedSubtitle = (TextView) findViewById(R.id.no_gym_selected_subtitle);
    noGymSelectedImage = (ImageView) findViewById(R.id.no_gym_selected_image);
    startSessionButton = (FloatingActionButton) findViewById(R.id.start_session_button);
    startSessionButton.setEnabled(false);
    startSessionButton.setOnClickListener(this);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

  }

  @Override
  protected void onResume() {
    super.onResume();

    // kickoff HTTP request to server for all the gym data
    fetchGymData();

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
    // TODO: use real server
    String url = "http://www.google.com";
    StringRequest gymDataRequest = new StringRequest(url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            try {
              // TODO: actually use this
              Msgs.Gyms gyms = Msgs.Gyms.parseFrom(ByteString.copyFromUtf8(response));
              onGymDataSuccess(gyms);
            } catch (InvalidProtocolBufferException e) {
              // could not parse message.
              // mock of what the server would return
              Msgs.Gyms gyms = fakeGymData();
              onGymDataSuccess(gyms);
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(this.getClass().toString(), "error: " + error.getMessage());
          }
        });
    RequestorSingleton.getInstance(this).addToRequestQueue(gymDataRequest);
  }

  private void onGymDataSuccess(Msgs.Gyms gyms) {
    appState.gyms = gyms;

    // load up the gym
    Intent intent = getIntent();
    if (intent.getAction().equals(Intent.ACTION_VIEW)) {
      Uri uri = getIntent().getData();
      try {
        int gymId = Integer.parseInt(uri.getLastPathSegment().toLowerCase());
        appState.setCurrentGym(gymId);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    } else {
      // try to look up the currently selected gym from preferences
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      int gymId = settings.getInt(GYM_ID_PREF_KEY, -1);
      if (gymId == -1) {
        // TODO: proper error handling
        appState.setCurrentGym(0);
      } else {
        appState.setCurrentGym(gymId);
      }

      // TODO: remove this.
      appState.setCurrentGym(0);
      startSessionButton.setEnabled(true);
    }

    // set the Icon for the current gym (if there is one)
    displayCurrentGym();
  }

  private void displayCurrentGym() {
    if (appState.getCurrentGymId() == -1) {
      // they have no gym selected, so tell them how to add one
      noGymSelectedTitle.setVisibility(View.VISIBLE);
      noGymSelectedSubtitle.setVisibility(View.VISIBLE);
      noGymSelectedImage.setVisibility(View.VISIBLE);
      largeIconImageView.setVisibility(View.GONE);
    } else {
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
      startActivity(startSessionIntent);
    }
  }

  private Msgs.Gyms fakeGymData() {
    return Msgs.Gyms.newBuilder().addGyms(
        Msgs.Gym.newBuilder().setName("Ascend PGH").addFloors(
            Msgs.Floor.newBuilder().addWalls(
                Msgs.Wall.newBuilder().setPolygon(
                    Msgs.Polygon.newBuilder().addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(0)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(10).setY(0)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(15).setY(5)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(10).setY(10)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(14)
                    ).setColor("#FFC107")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Lappnor Project").setPosition(
                        Msgs.Point2D.newBuilder().setX(2).setY(2)
                    ).setGrade(17).setColor("#FFFF00")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("La Dura Dura").setPosition(
                        Msgs.Point2D.newBuilder().setX(13).setY(25)
                    ).setGrade(16).setColor("#FFFFFF")
                ).setName("The Dawn Wall")
            ).addWalls(
                Msgs.Wall.newBuilder().setPolygon(
                    Msgs.Polygon.newBuilder().addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(30)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(5).setY(30)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(6).setY(35)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(4).setY(60)
                    ).addPoints(
                        Msgs.Point2D.newBuilder().setX(0).setY(67)
                    ).setColor("#9C27B0")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Pikachu").setPosition(
                        Msgs.Point2D.newBuilder().setX(5).setY(32)
                    ).setGrade(7).setColor("#ff0000")
                ).addRoutes(
                    Msgs.Route.newBuilder().setName("Magikarp").setPosition(
                        Msgs.Point2D.newBuilder().setX(4).setY(38)
                    ).setGrade(10).setColor("#00ff00")
                ).setName("Slab")
            ).setWidth(25).setHeight(70)
        ).setLargeIconUrl(
            "https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/Ascend-Mobile-Logo.png"
        )
    ).build();
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
}
