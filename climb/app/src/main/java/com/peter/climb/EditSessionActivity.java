package com.peter.climb;

import static com.peter.climb.SessionDetailsActivity.DATASETS_KEY;
import static com.peter.climb.SessionDetailsActivity.SENDS_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import java.util.ArrayList;

public class EditSessionActivity extends AppCompatActivity {

  private Session session;
  private Bundle bundle;
  private ArrayList<DataSet> dataSets;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_session);

    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setTitle("Edit Session");
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    bundle = getIntent().getExtras();
    if (bundle != null) {
      session = bundle.getParcelable(SENDS_KEY);
      dataSets = bundle.getParcelableArrayList(DATASETS_KEY);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.edit_session_toolbar_menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      setResult(RESULT_CANCELED);
      finish();
      return true;
    } else if (item.getItemId() == R.id.save_session_item) {
      Intent intent = getIntent();
      intent.putExtra(SENDS_KEY, session);
      intent.putParcelableArrayListExtra(DATASETS_KEY, dataSets);
      setResult(RESULT_OK, intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
