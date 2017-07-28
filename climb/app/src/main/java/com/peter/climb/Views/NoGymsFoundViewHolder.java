package com.peter.climb.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.peter.climb.CardsAdapter.CardListener;
import com.peter.climb.R;

public class NoGymsFoundViewHolder extends RecyclerView.ViewHolder {

  public CardListener cardListener;

  public NoGymsFoundViewHolder(View itemView) {
    super(itemView);

    Button refreshButton = (Button) itemView.findViewById(R.id.refresh_button);
    refreshButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (v.getId() == R.id.refresh_button) {
          cardListener.onRefreshGyms();
        }
      }
    });
  }
}

