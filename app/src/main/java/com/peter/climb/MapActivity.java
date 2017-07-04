package com.peter.climb;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    private View decor_view;
    private ImageView map_image_view;
    private Button end_session_button;
    private AppState app_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        app_state = ((MyApplication)getApplicationContext()).getState();

        decor_view = getWindow().getDecorView();

        end_session_button = (Button) findViewById(R.id.end_session_button);
        end_session_button.setOnClickListener(this);

        map_image_view = (ImageView) findViewById(R.id.map_image_view);
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
            finish();
        }
    }
}
