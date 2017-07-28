package com.peter.climb.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.peter.climb.CardsAdapter.CardListener;
import com.peter.climb.R;

public class NotSignedInViewHolder extends RecyclerView.ViewHolder {

  public CardListener cardListener;

  public NotSignedInViewHolder(View itemView) {
    super(itemView);

    Button signInButtton = (Button) itemView.findViewById(R.id.sign_in_button);
    signInButtton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
          cardListener.signIn();
        }
      }
    });
  }
}

