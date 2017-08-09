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
import com.peter.climb.Views.GenericViewHolder;
import com.peter.climb.Views.NoGymsFoundViewHolder;
import com.peter.climb.Views.NotSignedInViewHolder;
import com.peter.climb.Views.SessionViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CardsAdapter extends RecyclerView.Adapter<ViewHolder> {

  private static final Integer NO_SESSIONS_CARD_TYPE = 1;
  private static final Integer SESSION_CARD_TYPE = 2;
  private static final Integer SELECT_GYM_INSTRUCTIONS_CARD_TYPE = 3;
  private static final Integer NO_GYMS_FOUND_CARD_TYPE = 4;
  private static final Integer NOT_SIGNED_IN_CARD_TYPE = 5;
  private final AppState appState;
  private ArrayList<Object> dataset;
  private SessionReadResult sessionReadResult;
  private CardListener cardListener;

  CardsAdapter(AppState appState) {
    dataset = new ArrayList<>();
    this.appState = appState;
  }

  void clearSessions() {
    clearSessionFromDataSet();
    sessionReadResult = null;
    notifyDataSetChanged();
  }

  private void clearSessionFromDataSet() {
    for (int i = 0; i < dataset.size(); ) {
      Object datum = dataset.get(i);
      if (datum instanceof Session) {
        dataset.remove(datum);
      } else {
        i++;
      }
    }
  }

  void setSessions(SessionReadResult sessionReadResult) {
    // Digest the read result
    this.sessionReadResult = sessionReadResult;
    clearSessionFromDataSet();
    List<Session> sessions = sessionReadResult.getSessions();

    Collections.sort(sessions, new SessionSorter());
    for (Session session : sessions) {
      dataset.add(session);
    }

    if (sessions.isEmpty()) {
      showNoSessions();
    } else {
      hideNoSessions();
    }

    notifyDataSetChanged();
  }

  void removeSession(Session session) {
    dataset.remove(session);
    if (!hasSessions()) {
      showNoSessions();
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LinearLayout card;
    if (viewType == SESSION_CARD_TYPE) {
      card = (LinearLayout) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.session_card, parent, false);
      return new SessionViewHolder(card);
    } else if (viewType == NOT_SIGNED_IN_CARD_TYPE) {
      card = (LinearLayout) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.not_signed_in_card, parent, false);
      return new NotSignedInViewHolder(card);
    } else if (viewType == NO_GYMS_FOUND_CARD_TYPE) {
      card = (LinearLayout) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.no_gyms_found_card, parent, false);
      return new NoGymsFoundViewHolder(card);
    } else if (viewType == SELECT_GYM_INSTRUCTIONS_CARD_TYPE) {
      card = (LinearLayout) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.select_gym_instructions_card, parent, false);
      return new GenericViewHolder(card);
    } else if (viewType == NO_SESSIONS_CARD_TYPE) {
      card = (LinearLayout) LayoutInflater.from(parent.getContext())
          .inflate(R.layout.no_sessions_card, parent, false);
      return new GenericViewHolder(card);
    } else {
      card = new LinearLayout(parent.getContext());
      return new GenericViewHolder(card);
    }
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    int viewType = getItemViewType(position);

    if (viewType == SESSION_CARD_TYPE) {
      SessionViewHolder sessionViewHolder = (SessionViewHolder) holder;
      Session session = (Session) dataset.get(position);
      List<DataSet> dataSets = sessionReadResult.getDataSet(session, appState.routeDataType);
      ArrayList<DataSet> dataSetsArrayList = new ArrayList<>(dataSets);
      DataSet metadata = sessionReadResult.getDataSet(session, appState.metadataType).get(0);
      int numberOfSends = Utils.sendCount(dataSetsArrayList, appState.routeDataType);

      String activeTimeString = Utils.activeTimeStringHM(session);

      String title = numberOfSends + " Sends";
      String toolbarTitle = session.getDescription();

      sessionViewHolder.sessionTitleText.setText(title);
      sessionViewHolder.dateTimeText.setText(activeTimeString);
      sessionViewHolder.cardListener = this.cardListener;
      sessionViewHolder.toolbar.setTitle(toolbarTitle);
      sessionViewHolder.setSession(session, dataSetsArrayList, metadata);
    } else if (viewType == NO_GYMS_FOUND_CARD_TYPE) {
      NoGymsFoundViewHolder noGymsFoundViewHolder = (NoGymsFoundViewHolder) holder;
      noGymsFoundViewHolder.cardListener = this.cardListener;
    } else if (viewType == NOT_SIGNED_IN_CARD_TYPE) {
      NotSignedInViewHolder notSignedInViewHolder = (NotSignedInViewHolder) holder;
      notSignedInViewHolder.cardListener = this.cardListener;
    }
  }

  @Override
  public int getItemViewType(int position) {
    Object datum = dataset.get(position);
    if (datum instanceof Session) {
      return SESSION_CARD_TYPE;
    } else {
      return (int) datum;
    }
  }

  @Override
  public int getItemCount() {
    return dataset.size();
  }

  private boolean hasSessions() {
    for (Object datum : dataset) {
      if (datum instanceof Session) {
        return true;
      }
    }

    return false;
  }

  void setCardListener(CardListener cardListener) {
    this.cardListener = cardListener;
  }

  void showSelectGymInstructions() {
    if (!dataset.contains(SELECT_GYM_INSTRUCTIONS_CARD_TYPE)) {
      dataset.add(0, SELECT_GYM_INSTRUCTIONS_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void hideSelectGymInstructions() {
    if (dataset.contains(SELECT_GYM_INSTRUCTIONS_CARD_TYPE)) {
      dataset.remove(SELECT_GYM_INSTRUCTIONS_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void showNoSessions() {
    if (!dataset.contains(NO_SESSIONS_CARD_TYPE)) {
      int i = 0;
      for (; i < dataset.size(); i++) {
        Object datum = dataset.get(i);
        if (datum instanceof Session) {
          break;
        }
      }
      dataset.add(i, NO_SESSIONS_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void hideNoSessions() {
    if (dataset.contains(NO_SESSIONS_CARD_TYPE)) {
      dataset.remove(NO_SESSIONS_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void hideNoGymsFound() {
    if (dataset.contains(NO_GYMS_FOUND_CARD_TYPE)) {
      dataset.remove(NO_GYMS_FOUND_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void showNoGymsFound() {
    if (!dataset.contains(NO_GYMS_FOUND_CARD_TYPE)) {
      dataset.add(0, NO_GYMS_FOUND_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void showNotSignedIn() {
    if (!dataset.contains(NOT_SIGNED_IN_CARD_TYPE)) {
      dataset.add(0, NOT_SIGNED_IN_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  void hideNotSignedIn() {
    if (dataset.contains(NOT_SIGNED_IN_CARD_TYPE)) {
      dataset.remove(NOT_SIGNED_IN_CARD_TYPE);
      notifyDataSetChanged();
    }
  }

  public interface CardListener {

    void onDeleteSession(Session session, int index);

    void onShowSessionDetails(Session session, ArrayList<DataSet> dataSets, DataSet metadata,
        int index);

    void onRefreshGyms();

    void signIn();
  }

  private class SessionSorter implements java.util.Comparator<Session> {

    @Override
    public int compare(Session s1, Session s2) {
      if (s1 == null || s2 == null) {
        throw new NullPointerException();
      }
      long dt = s2.getStartTime(TimeUnit.MILLISECONDS) - s1.getStartTime(TimeUnit.MILLISECONDS);
      if (dt > 0L) {
        return 1;
      } else if (dt == 0L) {
        return 0;
      } else {
        return -1;
      }
    }
  }
}

