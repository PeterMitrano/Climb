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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.peter.Climb.Msgs;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String GYM_ID_PREF_KEY = "gym_id_pref_key";
    public static final String PREFS_NAME = "MyPrefsFile";
    private AppState app_state;

    private ImageView large_icon_image_view;
    private TextView no_gym_selected_title;
    private TextView no_gym_selected_subtitle;
    private ImageView no_gym_selected_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app_state = ((MyApplication) getApplicationContext()).getState();

        large_icon_image_view = (ImageView) findViewById(R.id.large_icon_image_view);
        no_gym_selected_title = (TextView) findViewById(R.id.no_gym_selected_title);
        no_gym_selected_subtitle = (TextView) findViewById(R.id.no_gym_selected_subtitle);
        no_gym_selected_image = (ImageView) findViewById(R.id.no_gym_selected_image);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // kickoff HTTP request to server for all the gym data
        if (app_state.gyms == null) {
            fetchGymData();
        }
    }

    private void fetchGymData() {
        // TODO: use real server
        String url = "http://www.google.com";
        StringRequest gym_data_request = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // TODO: actually use this
                            Msgs.Gyms gyms = Msgs.Gyms.parseFrom(ByteString.copyFromUtf8(response));
                        } catch (InvalidProtocolBufferException e) {
                            // could not parse message. 100 % Sad Panda
                        }

                        // mock of what the server would return
                        Msgs.Gyms gyms = fakeGymData();
                        onGymDataSuccess(gyms);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: dafuk do I do here
                    }
                });
        RequestorSingleton.getInstance(this).addToRequestQueue(gym_data_request);
    }

    private void onGymDataSuccess(Msgs.Gyms gyms) {
        app_state.gyms = gyms;

        // load up the gym
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            try {
                int gym_id = Integer.parseInt(uri.getLastPathSegment().toLowerCase());
                app_state.current_gym = app_state.gyms.getGyms(gym_id);
                app_state.current_gym_id = gym_id;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            // try to look up the currently selected gym from preferences
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            app_state.current_gym_id = settings.getInt(GYM_ID_PREF_KEY, -1);
        }

        // set the Icon for the current gym (if there is one)
        displayCurrentGym();
    }

    private void displayCurrentGym() {
        if (app_state.current_gym_id == -1) {
            // they have no gym selected, so tell them how to add one
            no_gym_selected_title.setVisibility(View.VISIBLE);
            no_gym_selected_subtitle.setVisibility(View.VISIBLE);
            no_gym_selected_image.setVisibility(View.VISIBLE);
            large_icon_image_view.setVisibility(View.GONE);
        } else {
            for (Msgs.Gym gym : app_state.gyms.getGymsList()) {
                if (gym.getId() == app_state.current_gym_id) {
                    // set the logo
                    String url = gym.getLargeIconUrl();
                    ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                            large_icon_image_view,
                            0,
                            R.drawable.ic_error_black_24dp);
                    RequestorSingleton.getInstance(
                            getApplicationContext()).getImageLoader().get(url, listener);

                    // mark this as the current gym
                    app_state.current_gym = gym;
                }
            }

            no_gym_selected_title.setVisibility(View.GONE);
            no_gym_selected_subtitle.setVisibility(View.GONE);
            no_gym_selected_image.setVisibility(View.GONE);
            large_icon_image_view.setVisibility(View.VISIBLE);

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
        editor.putInt(GYM_ID_PREF_KEY, app_state.current_gym_id);
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
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_menu_camera)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

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
//        private int notification_id = 1;
//        mNotificationManager.notify(notification_id, notification);

        Intent start_session_intent = new Intent(this, MapActivity.class);
        startActivity(start_session_intent);
    }

    private Msgs.Gyms fakeGymData() {
        return Msgs.Gyms.newBuilder().addGyms(
                Msgs.Gym.newBuilder().setName("Ascend PGH").addWalls(
                        Msgs.Wall.newBuilder().setPolygon(
                                Msgs.Polygon.newBuilder().addPoints(
                                        Msgs.Point2D.newBuilder().setX(0).setY(0)
                                ).addPoints(
                                        Msgs.Point2D.newBuilder().setX(10).setY(0)
                                ).addPoints(
                                        Msgs.Point2D.newBuilder().setX(10).setY(10)
                                ).addPoints(
                                        Msgs.Point2D.newBuilder().setX(0).setY(10)
                                )
                        ).addRoutes(
                                Msgs.Route.newBuilder().setName("Lappnor Project").setPosition(
                                        Msgs.Point2D.newBuilder().setX(0).setY(0)
                                ).setGrade(17)
                        ).addRoutes(
                                Msgs.Route.newBuilder().setName("La Dura Dura").setPosition(
                                        Msgs.Point2D.newBuilder().setX(1).setY(0)
                                ).setGrade(16)
                        ).setName("The Dawn Wall")
                ).setSmallIconUrl(
                        "https://www.ascendpgh.com/sites/default/files/logo.png"
                ).setLargeIconUrl(
                        "https://www.ascendpgh.com/sites/all/themes/ascend_foundation/images/header-images/02-Header-Visiting-Ascend.jpg"
                ).setMapUrl(
                        "https://www.guthrie.org/sites/default/files/TCH_AreaMap.gif"
                ).setId(0)
        ).build();
    }
}
