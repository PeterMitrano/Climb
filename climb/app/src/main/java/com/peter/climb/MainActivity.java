package com.peter.climb;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.Climb.Msgs;
import com.peter.Climb.Msgs.Gyms;
import com.peter.climb.FetchGymDataTask.FetchGymDataListener;
import com.peter.climb.MyApplication.AppState;
import com.peter.climb.MyApplication.DeleteSessionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener,
    DeleteSessionListener, OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
    FetchGymDataListener {

  public static final int REQUEST_RESOLVE_ERROR = 1001;
  public static final int SESSION_NOTIFICATION_ID = 1002;
  public static final int START_SESSION_REQUEST_CODE = 1004;
  public static final String DIALOG_ERROR = "GOOGLE_API_ERROR";
  public static final String PREFS_NAME = "MyPrefsFile";
  public static final String START_SESSION_ACTION = "start_session_action";

  private boolean mResolvingError = false;
  private static final String GYM_ID_PREF_KEY = "gym_id_pref_key";

  private FloatingActionButton startSessionButton;
  private ImageView appBarImage;
  private MenuItem changeAccountsItem;
  private RecyclerView cardsRecycler;

  private AppState appState;
  private CardsAdapter cardsAdapter;
  private int searchedGymId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // calling this function ensures that gym data is fetched if need be
    appState = ((MyApplication) getApplicationContext())
        .fetchGymDataAndAppState(getApplicationContext(), this);

    appBarImage = (ImageView) findViewById(R.id.app_bar_image);
    cardsRecycler = (RecyclerView) findViewById(R.id.sessions_recycler);
    startSessionButton = (FloatingActionButton) findViewById(R.id.start_session_button);
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    changeAccountsItem = navigationView.getMenu().findItem(R.id.change_accounts);

    cardsAdapter = new CardsAdapter();
    cardsAdapter.setOnDeleteSessionListener(this);
    cardsRecycler.setAdapter(cardsAdapter);

    startSessionButton.setEnabled(false);
    startSessionButton.setOnClickListener(this);

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
      buildFitnessClient();
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
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    Window window = getWindow();
    window.setFormat(PixelFormat.RGBA_8888);
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
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    switch (item.getItemId()) {
      case R.id.app_settings: {
        break;
      }
      case R.id.change_accounts: {
        // they probably want to change accounts if they clicked here
        if (appState.mClient.isConnected()) {
          startSessionButton.setEnabled(false);
          appState.mClient.clearDefaultAccountAndReconnect();
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_RESOLVE_ERROR) {
      mResolvingError = false;
      if (resultCode == RESULT_OK) {
        // Make sure the app is not already connected or attempting to connect
        if (!appState.mClient.isConnecting() &&
            !appState.mClient.isConnected()) {
          appState.mClient.connect();
        }
      } else {
        Snackbar snack = Snackbar
            .make(cardsRecycler, R.string.no_fit_permission_msg, Snackbar.LENGTH_INDEFINITE);
        snack.setAction(R.string.ask_again, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            appState.mClient.reconnect();
            mResolvingError = false;
          }
        });
        snack.show();
      }
    } else if (requestCode == START_SESSION_REQUEST_CODE) {
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
    if (v.getId() == R.id.start_session_button) {

      // if not, start a new session
      appState.startSession();

      Intent startSessionIntent = new Intent(this, MapActivity.class);
      startSessionIntent.setAction(START_SESSION_ACTION);
      startActivityForResult(startSessionIntent, START_SESSION_REQUEST_CODE);
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    String message = "Sign in Successful";
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    // also request sessions to display
    updateSessionsRecycler();

    if (appState.hasCurrentGym()) {
      startSessionButton.setEnabled(true);
      changeAccountsItem.setTitle(R.string.change_accounts);
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    // If your connection to the sensor gets lost at some point,
    // you'll be able to determine the reason and react to it here.
    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
      Log.e(getClass().toString(), "Connection lost.  Cause: Network Lost.");
    } else if (i
        == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
      Log.e(getClass().toString(), "Connection lost.  Reason: Service Disconnected");
    }
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult result) {
    if (!mResolvingError && result.hasResolution()) {
      try {
        mResolvingError = true;
        result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
      } catch (SendIntentException e) {
        // There was an error with the resolution intent. Try again.
        appState.mClient.connect();
      }
    } else {
      Log.e(getClass().toString(), result.getErrorCode() + ", " + result.getErrorMessage());
      showErrorDialog(result.getErrorCode());
    }
  }

  @Override
  public void onDeleteSession(final Session session, final int index) {
    long startTime = session.getStartTime(TimeUnit.MILLISECONDS);
    long endTime = session.getEndTime(TimeUnit.MILLISECONDS);
    appState.deleteSession(session, startTime, endTime, new ResultCallback<Status>() {
      @Override
      public void onResult(@NonNull Status status) {
        // remove the item
        cardsAdapter.removeSession(session);
        cardsAdapter.notifyItemRemoved(index);
      }
    });
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
        startSessionButton.setEnabled(true);
      }

    } else if (prefGymId != AppState.NO_GYM_ID) {
      appState.setCurrentGym(prefGymId);
      displayCurrentGym();
      if (appState.mClient.isConnected()) {
        startSessionButton.setEnabled(true);
      }

    } else {
      displayNoCurrentGym();
    }
  }

  @Override
  public void onNoGymsFound() {
    Snackbar.make(cardsRecycler, "No gyms found.", Snackbar.LENGTH_LONG).show();
  }

  private void buildFitnessClient() {
    if (appState.mClient == null) {
      appState.mClient = new GoogleApiClient.Builder(this)
          .addApi(Fitness.SESSIONS_API)
          .addApi(Fitness.CONFIG_API)
          .addApi(Fitness.HISTORY_API)
          .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
          .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
          .addScope(new Scope(Scopes.PROFILE))
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .build();

      appState.mClient.connect();
    }
  }

  private void updateSessionsRecycler() {
    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    long endTime = cal.getTimeInMillis();
    cal.add(Calendar.MONTH, -1);
    long startTime = cal.getTimeInMillis();
    appState.getSessionHistory(startTime, endTime, new ResultCallback<SessionReadResult>() {
      @Override
      public void onResult(@NonNull SessionReadResult sessionReadResult) {
        if (sessionReadResult.getStatus().isSuccess()) {
          cardsAdapter.setSessions(sessionReadResult);
        } else {
          // indicate that there are no sessions
          cardsAdapter.showNoSessions();
        }
      }
    });
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

  /* Creates a dialog for an error message */
  private void showErrorDialog(int errorCode) {
    // Create a fragment for the error dialog
    ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
    // Pass the error that should be displayed
    Bundle args = new Bundle();
    args.putInt(DIALOG_ERROR, errorCode);
    dialogFragment.setArguments(args);
    dialogFragment.show(getSupportFragmentManager(), getClass().toString());
  }

  /* Called from ErrorDialogFragment when the dialog is dismissed. */
  public void onDialogDismissed() {
    mResolvingError = false;
  }
}
