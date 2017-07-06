package com.peter.climb;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    private View decor_view;
    private ImageView map_image_view;
    private Button end_session_button;
    private AppState app_state;
    private TextView timer_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        app_state = ((MyApplication)getApplicationContext()).getState();

        decor_view = getWindow().getDecorView();

        end_session_button = (Button) findViewById(R.id.end_session_button);
        end_session_button.setOnClickListener(this);

        timer_view = (TextView) findViewById(R.id.time);

        map_image_view = (ImageView) findViewById(R.id.map_image_view);

        if (getIntent().getAction() == MainActivity.START_SESSION_ACTION) {
            app_state.session_in_progress = AppState.SessionState.IN_PROGRESS;
            start_session_timer();
        }

        loadMap();

    }

    private void start_session_timer() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        int minutes = 12;
                        int seconds = 34;
                        timer_view.setText(String.valueOf(minutes)+":"+String.valueOf(seconds));
                    }

                });
            }

        }, 0, 1000);
    }

    private void loadMap() {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                map_image_view,
                0,
                R.drawable.ic_error_black_24dp);
        String map_url = app_state.current_gym.getMapUrl();
        RequestorSingleton.getInstance(this).getImageLoader().get(map_url, listener);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            };

            decor_view.setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.end_session_button) {
            app_state.session_in_progress = AppState.SessionState.NOT_IN_PROGRESS;
            finish();
        }
    }
}
