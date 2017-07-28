package com.peter.climb;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.peter.climb.CardsAdapter.CardListener;

class NoGymsFoundViewHolder extends RecyclerView.ViewHolder {

  private Button refreshButton;
  CardListener cardListener;

  NoGymsFoundViewHolder(View itemView) {
    super(itemView);

    refreshButton = (Button) itemView.findViewById(R.id.refresh_button);
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

