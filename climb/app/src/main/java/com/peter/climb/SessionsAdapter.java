package com.peter.climb;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.climb.SessionsAdapter.SessionViewHolder;
import java.util.List;
import java.util.concurrent.TimeUnit;

class SessionsAdapter extends RecyclerView.Adapter<SessionViewHolder> {

  private List<Session> sessions;
  private SessionReadResult sessionReadResult;

  SessionsAdapter() {
  }

  void setSessions(SessionReadResult sessionReadResult) {
    this.sessionReadResult = sessionReadResult;
    this.sessions = sessionReadResult.getSessions();
    notifyDataSetChanged();
  }

  static class SessionViewHolder extends RecyclerView.ViewHolder {

    LinearLayout layout;
    CardView card;
    TextView sessionTitleText;
    TextView dateTimeText;

    SessionViewHolder(View itemView) {
      super(itemView);

      layout = (LinearLayout) itemView.findViewById(R.id.card_layout);
      card = (CardView) itemView.findViewById(R.id.card_view);
      sessionTitleText = (TextView) itemView.findViewById(R.id.session_title_text);
      dateTimeText = (TextView) itemView.findViewById(R.id.date_time_text);
    }
  }

  @Override
  public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LinearLayout card = (LinearLayout) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.session_card, parent, false);

    SessionViewHolder card_holder = new SessionViewHolder(card);
    return card_holder;
  }

  @Override
  public void onBindViewHolder(SessionViewHolder holder, int position) {
    if (position >= sessions.size()) {
      return;
    }

    Session session = sessions.get(position);
    List<DataSet> dataSets = sessionReadResult.getDataSet(session);
    int numberOfSends = 0;
    for (DataSet dataSet : dataSets) {
      numberOfSends += dataSet.getDataPoints().size();
    }

    holder.sessionTitleText.setText(numberOfSends + " Sends");

    long milliseconds =
        session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
    int hours = (int) milliseconds / (1000 * 60 * 60);
    int minutes = (int) milliseconds / (1000 * 60);
    String activeTimeString = hours + " h " + minutes + " min";
    holder.dateTimeText.setText(activeTimeString);
  }

  @Override
  public int getItemCount() {
    if (sessions != null && sessionReadResult != null) {
      return sessions.size();
    } else {
      return 0;
    }
  }
}

