package com.peter.climb;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.peter.climb.MyApplication.AppState;
import com.peter.climb.MyApplication.SessionCardListener;
import java.util.ArrayList;
import java.util.List;

class CardsAdapter extends RecyclerView.Adapter<ViewHolder> {

  private static final int NO_SESSIONS_CARD_TYPE = 1;
  private static final int SESSION_CARD_TYPE = 2;
  private static final int SELECT_GYM_INSTRUCTIONS_CARD_TYPE = 3;
  private final AppState appState;
  private int specialCardCount = 0;
  private List<Session> sessions;
  private SessionReadResult sessionReadResult;
  private SessionCardListener sessionCardListener;
  private boolean showInstructions = false;
  private boolean showNoSessions = false;

  CardsAdapter(AppState appState) {
    sessions = new ArrayList<>();
    this.appState = appState;
  }

  void clearSessions() {
    sessions.clear();
    sessionReadResult = null;
  }

  void setSessions(SessionReadResult sessionReadResult) {
    // Digest the read result
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
        Session session = sessions.get(0);
        List<DataSet> dataSets = sessionReadResult.getDataSet(session);
        ArrayList<DataSet> dataSetsArrayList = new ArrayList<>(dataSets);
        int numberOfSends = 0;

        for (DataSet dataSet : dataSets) {
          if (dataSet.getDataType().equals(appState.routeDataType)) {
            numberOfSends += dataSet.getDataPoints().size();
          }
        }

        String activeTimeString = Utils.activeTimeStringHM(session);

        String title = numberOfSends + " Sends";
        String toolbarTitle = session.getDescription();

        sessionViewHolder.sessionTitleText.setText(title);
        sessionViewHolder.dateTimeText.setText(activeTimeString);
        sessionViewHolder.sessionCardListener = this.sessionCardListener;
        sessionViewHolder.toolbar.setTitle(toolbarTitle);
        sessionViewHolder.setSession(session, dataSetsArrayList);
        break;

      case NO_SESSIONS_CARD_TYPE:
      default:
        break;
    }
  }

  @Override
  public int getItemCount() {
    if (hasSessions()) {
      return sessions.size() + specialCardCount;
    } else {
      return specialCardCount;
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (showInstructions && position == 0) {
      return SELECT_GYM_INSTRUCTIONS_CARD_TYPE;
    } else if (hasSessions()) {
      return SESSION_CARD_TYPE;
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
    if (!showInstructions) {
      specialCardCount++;
      showInstructions = true;
      notifyDataSetChanged();
    }
  }

  void hideSelectGymInstructions() {
    if (showInstructions) {
      specialCardCount--;
      showInstructions = false;
      notifyDataSetChanged();
    }
  }

  void showNoSessions() {
    if (!showNoSessions) {
      specialCardCount++;
      showNoSessions = true;
      notifyDataSetChanged();
    }
  }

  void hideNoSessions() {
    if (showNoSessions) {
      specialCardCount--;
      showNoSessions = false;
      notifyDataSetChanged();
    }
  }
}
