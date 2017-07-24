package com.peter.climb;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.climb.MyApplication.SessionCardListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

class CardsAdapter extends RecyclerView.Adapter<ViewHolder> {

  private static final int NO_SESSIONS_CARD_TYPE = 1;
  private static final int SESSION_CARD_TYPE = 2;
  private static final int SELECT_GYM_INSTRUCTIONS_CARD_TYPE = 3;
  private int special_card_count = 0;
  private List<Session> sessions;
  private SessionReadResult sessionReadResult;
  private SessionCardListener sessionCardListener;
  private boolean show_instructions = false;
  private boolean show_no_sessions = false;

  CardsAdapter() {
  }

  void setSessions(SessionReadResult sessionReadResult) {
    this.sessionReadResult = sessionReadResult;
    this.sessions = sessionReadResult.getSessions();

    if (this.sessions.isEmpty()) {
      showNoSessions();
    } else {
      hideNoSessions();
    }

    notifyDataSetChanged();
  }

  void removeSession(Session session) {
    sessions.remove(session);
    if (!hasSessions()) {
      showNoSessions();
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LinearLayout card;
    switch (viewType) {
      case SESSION_CARD_TYPE:
        card = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.session_card, parent, false);
        return new SessionViewHolder(card);

      case SELECT_GYM_INSTRUCTIONS_CARD_TYPE:
        card = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.select_gym_instructions_card, parent, false);
        return new SelectGymInstructionsViewHolder(card);

      case NO_SESSIONS_CARD_TYPE:
      default:
        card = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.no_sessions_card, parent, false);

        return new NoSessionsViewHolder(card);
    }
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    int viewType = getItemViewType(position);

    switch (viewType) {
      case SESSION_CARD_TYPE:
        SessionViewHolder sessionViewHolder = (SessionViewHolder) holder;
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

        String title = numberOfSends + " Sends";
        String toolbarTitle = session.getDescription();

        sessionViewHolder.sessionTitleText.setText(title);
        sessionViewHolder.dateTimeText.setText(activeTimeString);
        sessionViewHolder.session = session;
        sessionViewHolder.sessionCardListener = this.sessionCardListener;
        sessionViewHolder.toolbar.setTitle(toolbarTitle);
        break;

      case NO_SESSIONS_CARD_TYPE:
      default:
        break;
    }
  }

  @Override
  public int getItemCount() {
    if (hasSessions()) {
      return sessions.size() + special_card_count;
    } else {
      return special_card_count;
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (hasSessions()) {
      return SESSION_CARD_TYPE;
    } else if (show_instructions && position == 0) {
      return SELECT_GYM_INSTRUCTIONS_CARD_TYPE;
    } else {
      return NO_SESSIONS_CARD_TYPE;
    }
  }

  private boolean hasSessions() {
    return sessions != null && sessions.size() > 0;
  }

  void setSessionCardListener(SessionCardListener sessionCardListener) {
    this.sessionCardListener = sessionCardListener;
  }

  void showSelectGymInstructions() {
    if (!show_instructions) {
      special_card_count++;
      show_instructions = true;
      notifyDataSetChanged();
    }
  }

  void hideSelectGymInstructions() {
    if (show_instructions) {
      special_card_count--;
      show_instructions = false;
      notifyDataSetChanged();
    }
  }

  void showNoSessions() {
    if (!show_no_sessions) {
      special_card_count++;
      show_no_sessions = true;
      notifyDataSetChanged();
    }
  }

  void hideNoSessions() {
    if (show_no_sessions) {
      special_card_count--;
      show_no_sessions = false;
      notifyDataSetChanged();
    }
  }
}

