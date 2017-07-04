package com.peter.climb;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.peter.Climb.Msgs;

import java.util.ArrayList;

public class FindGymActivity extends ListActivity {

    private AppState app_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gym);

        app_state = ((MyApplication)getApplicationContext()).getState();

        ArrayList<String> listValues = new ArrayList<>();
        for (Msgs.Gym gym : app_state.gyms.getGymsList()) {
            listValues.add(gym.getName());
        }

        // initiate the listadapter
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
                R.layout.gym_search_result_layout, R.id.list_text, listValues);

        // assign the list adapter
        setListAdapter(myAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent data = new Intent();
        data.putExtra(MainActivity.FIND_GYM_KEY, position);
        setResult(MainActivity.FIND_GYM_CODE, data);
        finish();
    }
}
