package com.peter.climb;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {
  private String[] mDataset;

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  static class ViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    TextView mTextView;
    ViewHolder(TextView v) {
      super(v);
      mTextView = v;
    }
  }

  SessionsAdapter(String[] myDataset) {
    mDataset = myDataset;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public SessionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
      int viewType) {
    // create a new view
    TextView v = (TextView) LayoutInflater.from(parent.getContext())
        .inflate(R.layout.session_card, parent, false);

    // set the view's size, margins, paddings and layout parameters
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    holder.mTextView.setText(mDataset[position]);
  }

  @Override
  public int getItemCount() {
    return mDataset.length;
  }
}

