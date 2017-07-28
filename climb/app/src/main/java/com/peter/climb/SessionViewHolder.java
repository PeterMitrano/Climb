package com.peter.climb;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Session;
import com.peter.climb.CardsAdapter.CardListener;
import java.util.ArrayList;

class SessionViewHolder extends RecyclerView.ViewHolder implements
    OnMenuItemClickListener, OnClickListener {

  private final LinearLayout layout;
  final Toolbar toolbar;
  final CardView card;
  final TextView sessionTitleText;
  final TextView dateTimeText;

  private Session session;
  CardListener cardListener;
  private ArrayList<DataSet> datasets;

  SessionViewHolder(View itemView) {
    super(itemView);

    layout = (LinearLayout) itemView.findViewById(R.id.card_layout);
    card = (CardView) itemView.findViewById(R.id.card_view);
    toolbar = (Toolbar) itemView.findViewById(R.id.card_toolbar);
    sessionTitleText = (TextView) itemView.findViewById(R.id.session_title_text);
    dateTimeText = (TextView) itemView.findViewById(R.id.date_time_text);

    toolbar.inflateMenu(R.menu.card_menu);
    toolbar.setOnMenuItemClickListener(this);
    toolbar.setOnClickListener(this);
    card.setOnClickListener(this);
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    if (item.getItemId() == R.id.card_delete) {
      // confirm to delete this session
      AlertDialog.Builder builder = new AlertDialog.Builder(layout.getContext());
      builder.setMessage(R.string.delete_session_message).setTitle(R.string.delete_session_title)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              cardListener.onDeleteSession(session, getAdapterPosition());
            }
          })
          .setNegativeButton(android.R.string.cancel, null);
      builder.create().show();
    }
    return false;
  }

  @Override
  public void onClick(View v) {
    cardListener.onShowSessionDetails(session, datasets, getAdapterPosition());
  }

  void setSession(Session session, ArrayList<DataSet> datasets) {
    this.session = session;
    this.datasets = datasets;
  }

}

