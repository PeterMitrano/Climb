package com.peter.climb;

import static com.peter.climb.SessionDetailsActivity.DATASETS_KEY;
import static com.peter.climb.SessionDetailsActivity.METADATA_KEY;
import static com.peter.climb.SessionDetailsActivity.SENDS_KEY;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SearchView;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.Climb.Msgs;
import com.peter.Climb.Msgs.Gyms;
import com.peter.climb.CardsAdapter.CardListener;
import com.peter.climb.MyApplication.AppState;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActivityWrapper implements OnNavigationItemSelectedListener,
    CardListener, OnClickListener {

  public static final int SESSION_NOTIFICATION_ID = 1002;
  private static final int START_SESSION_REQUEST_CODE = 1004;
  private static final String PREFS_NAME = "ClimbPreferences";
  static final String START_SESSION_ACTION = "start_session_action";

  private static final String GYM_ID_PREF_KEY = "gym_id_pref_key";

  private FloatingActionButton floatingActionButton;
  private ImageView appBarImage;
  private RecyclerView cardsRecycler;

  private CardsAdapter cardsAdapter;
  private int searchedGymId;

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    Window window = getWindow();
    window.setFormat(PixelFormat.RGBA_8888);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_toolbar_menu, menu);

    // Get the SearchView and set the searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.gym_search_view).getActionView();

    // Assumes current activity is the searchable activity
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setFocusable(true);
    searchView.setIconified(false);

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
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    switch (item.getItemId()) {
      case R.id.app_settings: {
        break;
      }
      case R.id.change_accounts: {
        if (appState.mClient.isConnected()) {
          floatingActionButton.setImageResource(R.drawable.ic_eye_black_24dp);
          if (appState.hasCurrentGym()) {
            floatingActionButton.setEnabled(true);
          }
          else {
            floatingActionButton.setEnabled(false);
          }
          cardsAdapter.clearSessions();
          cardsAdapter.hideNoSessions();
          cardsAdapter.showNotSignedIn();
          appState.reconnect(true);
        } else {
          appState.mClient.connect();
        }
        break;
      }
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  void onPermissionsDenied() {
    mResolvingError = false;
    cardsAdapter.hideNoSessions();
    cardsAdapter.showNotSignedIn();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    appBarImage = (ImageView) findViewById(R.id.app_bar_image);
    cardsRecycler = (RecyclerView) findViewById(R.id.sessions_recycler);
    floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

    cardsAdapter = new CardsAdapter(appState);
    cardsAdapter.setCardListener(this);
    cardsRecycler.setAdapter(cardsAdapter);

    floatingActionButton.setImageResource(R.drawable.ic_eye_black_24dp);
    floatingActionButton.setEnabled(false);
    floatingActionButton.setOnClickListener(this);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    Intent intent = getIntent();
    String action = intent.getAction();
    searchedGymId = AppState.NO_GYM_ID;
    if (action.equals(Intent.ACTION_MAIN)) {
      // connect to google fit API
      if (!appState.mClient.isConnected()) {
        appState.mClient.connect();
      }
    } else if (action.equals(Intent.ACTION_SEARCH)) {
      // check if this request came from a search for a specific gym
      Uri uri = getIntent().getData();
      try {
        searchedGymId = Integer.parseInt(uri.getLastPathSegment().toLowerCase());

        // save this search as the current setting
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(GYM_ID_PREF_KEY, searchedGymId);
        editor.apply();
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Snackbar.make(cardsRecycler, "Invalid search result", Snackbar.LENGTH_SHORT).show();
      }

      // update the recycler
      updateSessionsRecycler();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == START_SESSION_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        updateSessionsRecycler();
      }
    }
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
  public void onClick(View v) {
    if (v.getId() == R.id.floating_action_button) {
      unregisterGoogleFitListener();

      // if not, start a new session
      appState.startSession();

      Intent startSessionIntent = new Intent(this, MapActivity.class);

      if (appState.mClient.isConnected()) {
        startSessionIntent.setAction(START_SESSION_ACTION);
      }
      startActivityForResult(startSessionIntent, START_SESSION_REQUEST_CODE);
    }
  }

  @Override
  public void onGoogleFitConnected() {
    // also request sessions to display
    cardsAdapter.hideNotSignedIn();
    updateSessionsRecycler();

    floatingActionButton.setImageResource(R.drawable.ic_timer_black_24dp);
    if (appState.hasCurrentGym()) {
      floatingActionButton.setEnabled(true);
    }
    else {
      floatingActionButton.setEnabled(false);
    }
  }

  @Override
  public void onGoogleFitFailed() {
    // these are really unlikely to happen, but it renders google fit api useless
    floatingActionButton.setImageResource(R.drawable.ic_eye_black_24dp);
    if (appState.hasCurrentGym()) {
      floatingActionButton.setEnabled(true);
    }
    else {
      floatingActionButton.setEnabled(false);
    }

    cardsAdapter.clearSessions();
    Snackbar snack = Snackbar
        .make(cardsRecycler, "Failed to connect to google Fit.", Snackbar.LENGTH_INDEFINITE);
    snack.setAction("Try Again", new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        appState.reconnect();
      }
    });
    snack.show();
  }

  @Override
  public void onDeleteSession(final Session session, final int index) {
    long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
    long endTime = session.getEndTime(TimeUnit.MILLISECONDS);
    appState.deleteSession(session, startTime, endTime, new ResultCallback<Status>() {
      @Override
      public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
          // remove the item
          cardsAdapter.removeSession(session);
          cardsAdapter.notifyItemRemoved(index);
        } else {
          Snackbar.make(cardsRecycler, "Failed to delete session", Snackbar.LENGTH_SHORT).show();
        }
      }
    });
  }

  @Override
  public void onShowSessionDetails(Session session, ArrayList<DataSet> dataSets, DataSet metadata,
      int index) {
    Intent intent = new Intent(this, SessionDetailsActivity.class);
    intent.putExtra(SENDS_KEY, session);
    intent.putExtra(METADATA_KEY, metadata);
    intent.putParcelableArrayListExtra(DATASETS_KEY, dataSets);
    startActivity(intent);
  }

  @Override
  public void onRefreshGyms() {
    appState.refreshGyms(this);
  }

  @Override
  public void signIn() {
    appState.reconnect();
  }

  @Override
  public void onGymsFound(Gyms gyms) {
    // Two possible sources of current gym are search and settings
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    int prefGymId = settings.getInt(GYM_ID_PREF_KEY, AppState.NO_GYM_ID);

    // First check if this request came from a search for a specific gym
    if (searchedGymId != AppState.NO_GYM_ID) {
      appState.setCurrentGym(searchedGymId);
      displayCurrentGym();
      if (appState.mClient.isConnected()) {
        floatingActionButton.setImageResource(R.drawable.ic_timer_black_24dp);
        floatingActionButton.setEnabled(true);
      }
      else {
        floatingActionButton.setImageResource(R.drawable.ic_eye_black_24dp);
        floatingActionButton.setEnabled(true);
      }
    } else if (prefGymId != AppState.NO_GYM_ID) {
      appState.setCurrentGym(prefGymId);
      displayCurrentGym();
      if (appState.mClient.isConnected()) {
        floatingActionButton.setImageResource(R.drawable.ic_timer_black_24dp);
        floatingActionButton.setEnabled(true);
      }
      else {
        floatingActionButton.setImageResource(R.drawable.ic_eye_black_24dp);
        floatingActionButton.setEnabled(true);
      }
    } else {
      displayNoCurrentGym();
    }

    cardsAdapter.hideNoGymsFound();
  }

  @Override
  public void onNoGymsFound() {
    Snackbar.make(cardsRecycler, "No gyms found.", Snackbar.LENGTH_LONG).show();
    cardsAdapter.showNoGymsFound();
  }

  private void updateSessionsRecycler() {
    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    long endTime = cal.getTimeInMillis();
    cal.add(Calendar.MONTH, -1);
    long startTime = cal.getTimeInMillis();

    if (appState.mClient.isConnected() && appState.hasDataTypes()) {
      appState.getSessionHistory(startTime, endTime, new ResultCallback<SessionReadResult>() {
        @Override
        public void onResult(@NonNull SessionReadResult sessionReadResult) {
          if (sessionReadResult.getStatus().isSuccess()) {
            cardsAdapter.setSessions(sessionReadResult);
          } else {
            Log.e(getClass().toString(), "session read failed");
            cardsAdapter.showNoSessions();
          }
        }
      });
    } else {
      Log.e(getClass().toString(), "not connected or missing datatypes");
      if (appState.mClient.isConnected()) {
        cardsAdapter.showNoSessions();
      }
      else {
        cardsAdapter.showNotSignedIn();
      }
    }
  }

  private void displayNoCurrentGym() {
    // they have no gym selected
    cardsAdapter.showSelectGymInstructions();
    cardsAdapter.notifyDataSetChanged();
  }

  private void displayCurrentGym() {
    // remove the instructions card
    cardsAdapter.hideSelectGymInstructions();
    cardsAdapter.notifyDataSetChanged();

    // get the gym
    Msgs.Gym gym = appState.getCurrentGym();

    // set the logo
    String url = gym.getLargeIconUrl();
    ImageLoader.ImageListener listener = ImageLoader.getImageListener(
        appBarImage,
        0,
        R.drawable.ic_error_black_24dp);
    RequestorSingleton.getInstance(
        getApplicationContext()).getImageLoader().get(url, listener);
  }
}
