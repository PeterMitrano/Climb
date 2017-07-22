package com.peter.climb;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.climb.MyApplication.DeleteSessionListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

class SessionsAdapter extends RecyclerView.Adapter<SessionViewHolder> {

  private List<Session> sessions;
  private SessionReadResult sessionReadResult;
  private DeleteSessionListener deleteSessionListener;

  SessionsAdapter() {
  }

  void setSessions(SessionReadResult sessionReadResult) {
    this.sessionReadResult = sessionReadResult;
    this.sessions = sessionReadResult.getSessions();
    notifyDataSetChanged();
  }

  void removeSession(Session session) {
    sessions.remove(session);
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
    Session session = sessions.get(position);
    List<DataSet> dataSets = sessionReadResult.getDataSet(session);
    int numberOfSends = 0;

    for (DataSet dataSet : dataSets) {
      numberOfSends += dataSet.getDataPoints().size();
    }

    long milliseconds =
        session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
    int hours = (int) milliseconds / (1000 * 60 * 60);
    int minutes = (int) milliseconds / (1000 * 60);
    String activeTimeString = hours + " h " + minutes + " min";

    holder.sessionTitleText.setText(numberOfSends + " Sends");
    holder.dateTimeText.setText(activeTimeString);
    holder.session = session;
    holder.deleteSessionListener = this.deleteSessionListener;
  }

  @Override
  public int getItemCount() {
    if (sessions != null && sessionReadResult != null) {
      return sessions.size();
    } else {
      return 0;
    }
  }

  void setOnDeleteSessionListener(DeleteSessionListener deleteSessionListener) {
    this.deleteSessionListener = deleteSessionListener;
  }
}

