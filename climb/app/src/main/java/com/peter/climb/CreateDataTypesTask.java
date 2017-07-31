package com.peter.climb;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.peter.climb.MyApplication.AppState;
import java.util.concurrent.TimeUnit;

class CreateDataTypesTask extends AsyncTask<Void, Void, DataTypeResult[]> {

  private final CreateDataTypesListener createDataTypesListener;
  private final String packageName;

  interface CreateDataTypesListener {

    void onDataTypesCreated();

    void onDataTypesNotCreated();
  }

  private final AppState appState;

  CreateDataTypesTask(AppState appState, String packageName,
      @NonNull CreateDataTypesListener createDataTypesListener) {
    this.createDataTypesListener = createDataTypesListener;
    this.appState = appState;
    this.packageName = packageName;
  }

  @Override
  protected DataTypeResult[] doInBackground(Void... params) {
    appState.gradeField = Field.zzm("Grade", Field.FORMAT_STRING);
    appState.nameField = Field.zzm("Name", Field.FORMAT_STRING);
    appState.wallField = Field.zzm("Wall", Field.FORMAT_STRING);
    appState.colorField = Field.zzm("Color", Field.FORMAT_STRING);
    appState.gymNameField = Field.zzm("Gym", Field.FORMAT_STRING);
    appState.imageUrlField = Field.zzm("Image URL", Field.FORMAT_STRING);
    appState.uuidField = Field.zzm("UUID", Field.FORMAT_STRING);

    // Build a request to create a new data type
    DataTypeCreateRequest metadataTypeRequest = new DataTypeCreateRequest.Builder()
        // The prefix of your data type name must match your app's package name
        .setName(packageName + ".metadata_data_type_000")
        .addField(appState.gymNameField)
        .addField(appState.imageUrlField)
        .addField(appState.uuidField)
        .build();

    // Build a request to create a new data type
    DataTypeCreateRequest routeTypeRequest = new DataTypeCreateRequest.Builder()
        // The prefix of your data type name must match your app's package name
        .setName(packageName + ".route_data_type_000")
        .addField(appState.gradeField)
        .addField(appState.nameField)
        .addField(appState.wallField)
        .addField(appState.colorField)
        .build();

    DataTypeResult metadataTypeResult = Fitness.ConfigApi
        .createCustomDataType(appState.mClient, metadataTypeRequest).await(1, TimeUnit.MINUTES);
    DataTypeResult routeTypeResult = Fitness.ConfigApi
        .createCustomDataType(appState.mClient, routeTypeRequest).await(1, TimeUnit.MINUTES);

    return new DataTypeResult[]{metadataTypeResult, routeTypeResult};
  }

  @Override
  protected void onPostExecute(DataTypeResult[] dataTypeResults) {
    appState.metadataType = dataTypeResults[0].getDataType();
    appState.routeDataType = dataTypeResults[1].getDataType();

    if (dataTypeResults[0].getStatus().isSuccess() && dataTypeResults[1].getStatus().isSuccess()) {
      createDataTypesListener.onDataTypesCreated();
    } else {
      createDataTypesListener.onDataTypesNotCreated();
    }
  }
}
