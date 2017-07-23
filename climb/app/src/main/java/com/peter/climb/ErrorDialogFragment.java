package com.peter.climb;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import com.google.android.gms.common.GoogleApiAvailability;

/* A fragment to display an error dialog */
public class ErrorDialogFragment extends DialogFragment {

  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Get the error code and retrieve the appropriate dialog
    int errorCode = this.getArguments().getInt(MainActivity.DIALOG_ERROR);
    return GoogleApiAvailability.getInstance().getErrorDialog(
        this.getActivity(), errorCode, MainActivity.REQUEST_RESOLVE_ERROR);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    ((MainActivity) getActivity()).onDialogDismissed();
  }
}
