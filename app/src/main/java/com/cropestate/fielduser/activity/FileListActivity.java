package com.cropestate.fielduser.activity;

        import android.Manifest;
        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.RequiresApi;
        import androidx.appcompat.app.AlertDialog;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonArrayRequest;
        import com.android.volley.toolbox.StringRequest;
        import com.greysonparrelli.permiso.Permiso;
        import com.greysonparrelli.permiso.PermisoActivity;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.adapter.FileListAdapter;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.pojo.MyFile;
        import com.cropestate.fielduser.realm.RealmAudio;
        import com.cropestate.fielduser.realm.RealmRecordedVideo;
        import com.cropestate.fielduser.util.DownloadFileAsync;
        import com.cropestate.fielduser.util.RealmUtility;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.File;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

        import io.realm.Realm;
        import io.realm.RealmResults;

        import static android.Manifest.permission.CALL_PHONE;
        import static android.Manifest.permission.MANAGE_OWN_CALLS;
        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.activity.HomeActivity.getDefaultDialerPackage;
        import static com.cropestate.fielduser.activity.PhoneActivity.populateDocs;
        import static com.cropestate.fielduser.activity.PhoneActivity.populateVideos;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;

        import static com.cropestate.fielduser.constants.Const.changeDefaultDialer;
        import static com.cropestate.fielduser.constants.Const.fileSize;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;
        import static com.cropestate.fielduser.constants.Const.toTitleCase;

public class FileListActivity extends PermisoActivity {
    public static String ENROLMENTID = "ENROLMENTID";
    public static String INSTRUCTORCOURSEID = "INSTRUCTORCOURSEID";
    public static String COURSEPATH = "COURSEPATH";
    private HashMap<String, AsyncTask<String, Integer, String>> downFileAsyncMap = new HashMap<>();
    public static ArrayList<MyFile> myFiles = new ArrayList<>();
    FileListAdapter fileListAdapter;
    RecyclerView recyclerview_files;
    ImageView backbtn, refresh_videos, refresh_docs;
    TextView activitytitle, assignmenttitle, nofile;
    LinearLayout refresh_layout;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        recyclerview_files = findViewById(R.id.recyclerview_files);
        backbtn = findViewById(R.id.search);
        refresh_layout = findViewById(R.id.refresh_layout);

        backbtn.setOnClickListener(v -> finish());
        activitytitle = findViewById(R.id.activitytitle);
        assignmenttitle = findViewById(R.id.assignmenttitle);
        nofile = findViewById(R.id.nodocument);

        refresh_videos = findViewById(R.id.refresh);
        refresh_docs = findViewById(R.id.refresh_docs);

        refresh_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (refresh_videos.getVisibility() == View.VISIBLE) {
                    refreshVideos();
                }
                else if (refresh_docs.getVisibility() == View.VISIBLE) {
                    refreshDocs();
                }
            }
        });

        activitytitle.setText(getIntent().getStringExtra("activitytitle"));
        assignmenttitle.setText(getIntent().getStringExtra("assignmenttitle"));

        if (getIntent().getStringExtra("assignmenttitle") != null && getIntent().getStringExtra("assignmenttitle").equals(getString(R.string.classvideos))) {
            refresh_videos.setVisibility(View.VISIBLE);
            refresh_docs.setVisibility(View.GONE);
        }
        else if (getIntent().getStringExtra("assignmenttitle") != null && getIntent().getStringExtra("assignmenttitle").equals(getString(R.string.classdocs))) {
            refresh_videos.setVisibility(View.GONE);
            refresh_docs.setVisibility(View.VISIBLE);
        }
        else {
            refresh_docs.setVisibility(View.GONE);
            refresh_videos.setVisibility(View.GONE);
        }

        if (getIntent().getBooleanExtra("LAUNCED_FROM_NOTIFICATION", false)) {
//            makeAppDefaultCallingApp();
        }

        fileListAdapter = new FileListAdapter((myFiles, position, holder) -> Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                                                          @Override
                                                                                                                          public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                                                              if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                                                                  MyFile myFile = myFiles.get(position);
                                                                                                                                  File file = new File(myFile.getPath());
                                                                                                                                  File parentFile = file.getParentFile();
                                                                                                                                  if (!parentFile.exists()) {
                                                                                                                                      parentFile.mkdirs();
                                                                                                                                  }

                                                                                                                                  holder.pbar.setVisibility(View.VISIBLE);
                                                                                                                                  holder.download.setVisibility(View.GONE);
                                                                                                                                  holder.pbar.animate();
                                                                                                                                  Toast.makeText(FileListActivity.this, getString(R.string.downloading), Toast.LENGTH_SHORT).show();



                                                                                                                                  StringRequest stringRequest = new StringRequest(
                                                                                                                                          Request.Method.POST,
                                                                                                                                          API_URL + "check-subscription",
                                                                                                                                          response -> {
                                                                                                                                              if (response != null) {
                                                                                                                                                  try {
                                                                                                                                                      JSONObject jsonObject = new JSONObject(response);
                                                                                                                                                      if (jsonObject.getBoolean("eligible")) {
                                                                                                                                                          downFileAsyncMap.put(myFile.getUrl(), new DownloadFileAsync(new DownloadFileAsync.OnTaskCompletedInterface() {
                                                                                                                                                              @Override
                                                                                                                                                              public void onTaskCompleted(String response) {

                                                                                                                                                                  if (response != null) {
                                                                                                                                                                      holder.pbar.setVisibility(View.GONE);
                                                                                                                                                                      holder.download.setVisibility(View.VISIBLE);
                                                                                                                                                                      // unsuccessful
                                                                                                                                                                      if (response.contains("java.io.FileNotFoundException")) {
                                                                                                                                                                          new AlertDialog.Builder(FileListActivity.this)
                                                                                                                                                                                  .setTitle(toTitleCase(getApplicationContext().getString(R.string.download_failed)))
                                                                                                                                                                                  .setMessage(FileListActivity.this.getString(R.string.file_no_longer_available_for_download))

                                                                                                                                                                                  // Specifying a listener allows you to take an action before dismissing the dialog.
                                                                                                                                                                                  // The dialog is automatically dismissed when a dialog button is clicked.
                                                                                                                                                                                  .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                                                                                                                                                  })
                                                                                                                                                                                  .show();

                                                                                                                                                                      } else {
                                                                                                                                                                          Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                                                                                                                                                      }
                                                                                                                                                                  } else {
                                                                                                                                                                      holder.downloadStatusWrapper.setVisibility(View.GONE);
                                                                                                                                                                      holder.removelayout.setVisibility(View.VISIBLE);
                                                                                                                                                                      holder.size.setText(fileSize(file.length()));
                                                                                                                                                                  }
                                                                                                                                                              }
                                                                                                                                                          }, progress -> holder.pbar.setProgress(progress), new DownloadFileAsync.OnTaskCancelledInterface() {
                                                                                                                                                              @Override
                                                                                                                                                              public void onTaskCancelled() {
                                                                                                                                                                  if (file.exists()) {
                                                                                                                                                                      file.delete();
                                                                                                                                                                  }
                                                                                                                                                                  Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                                                                                                                                                              }
                                                                                                                                                          }).execute(myFile.getUrl(), file.getAbsolutePath()));
                                                                                                                                                      } else {
                                                                                                                                                          holder.pbar.setVisibility(View.GONE);
                                                                                                                                                          holder.download.setVisibility(View.VISIBLE);
                                                                                                                                                          Toast.makeText(getApplicationContext(), getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                                                                                                                                      }
                                                                                                                                                  } catch (JSONException e) {
                                                                                                                                                      e.printStackTrace();
                                                                                                                                                  }
                                                                                                                                              }
                                                                                                                                          },
                                                                                                                                          error -> {
                                                                                                                                              error.printStackTrace();
                                                                                                                                              holder.pbar.setVisibility(View.GONE);
                                                                                                                                              holder.download.setVisibility(View.VISIBLE);
                                                                                                                                              myVolleyError(getApplicationContext(), error);
                                                                                                                                          }
                                                                                                                                  )
                                                                                                                                  {
                                                                                                                                      @Override
                                                                                                                                      public Map<String, String> getParams() throws AuthFailureError {
                                                                                                                                          Map<String,String> params = new HashMap<>();
                                                                                                                                          params.put("enrolmentid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(ENROLMENTID, ""));
                                                                                                                                          return params;
                                                                                                                                      }

                                                                                                                                      @Override
                                                                                                                                      public Map getHeaders() throws AuthFailureError {
                                                                                                                                          HashMap headers = new HashMap();
                                                                                                                                          headers.put("accept", "application/json");
                                                                                                                                          headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                                                                                                                                          return headers;
                                                                                                                                      }
                                                                                                                                  };
                                                                                                                                  stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                                                                                                          0,
                                                                                                                                          DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                                                                                                          DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                                                                                                  InitApplication.getInstance().addToRequestQueue(stringRequest);
                                                                                                                              }
                                                                                                                          }
                                                                                                                          @Override
                                                                                                                          public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                                                                              Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                                                                          }
                                                                                                                      },
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), myFiles);

        recyclerview_files.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_files.setHasFixedSize(true);
        recyclerview_files.setNestedScrollingEnabled(false);
        recyclerview_files.setItemAnimator(new DefaultItemAnimator());
        recyclerview_files.setAdapter(fileListAdapter);

        ShowNoFileAvailableMsg();
    }

    @Override
    protected void onStop() {
        for (Map.Entry me : downFileAsyncMap.entrySet()) {
            AsyncTask<String, Integer, String> downloadFileAsync = (AsyncTask<String, Integer, String>) me.getValue();
            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                // This would not cancel downloading from httpClient
                //  we have do handle that manually in onCancelled event inside AsyncTask
                downloadFileAsync.cancel(true);
            }
        }
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("assignmenttitle") != null && intent.getStringExtra("assignmenttitle").equals(getString(R.string.classvideos))) {
            refresh_videos.setVisibility(View.VISIBLE);
            refresh_docs.setVisibility(View.GONE);
        }
        else if (intent.getStringExtra("assignmenttitle") != null && intent.getStringExtra("assignmenttitle").equals(getString(R.string.classdocs))) {
            refresh_videos.setVisibility(View.GONE);
            refresh_docs.setVisibility(View.VISIBLE);
        }

        if (intent.getBooleanExtra("LAUNCED_FROM_NOTIFICATION", false)) {
//            makeAppDefaultCallingApp();
        }
    }

    private void makeAppDefaultCallingApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                         @Override
                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                             if (
                                                                     resultSet.isPermissionGranted(CALL_PHONE) &&
                                                                             resultSet.isPermissionGranted(MANAGE_OWN_CALLS)
                                                             ) {
                                                                 setSchoolDirectStudentADefaultCallingApp();
                                                             }
                                                         }

                                                         @Override
                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                             Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                         }
                                                     },
                    CALL_PHONE,
                    MANAGE_OWN_CALLS
            );
        } else {
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                         @Override
                                                         public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                             if (
                                                                     resultSet.isPermissionGranted(CALL_PHONE)
                                                             ) {
                                                                 setSchoolDirectStudentADefaultCallingApp();
                                                             }
                                                         }

                                                         @Override
                                                         public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                             Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                         }
                                                     },
                    CALL_PHONE
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSchoolDirectStudentADefaultCallingApp() {
        if (!getDefaultDialerPackage(getApplicationContext()).equals(getPackageName())) {
            changeDefaultDialer(FileListActivity.this, getPackageName());
        }
    }

    public void refreshVideos() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.checking_for_new_videos));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "recorded-videos",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmRecordedVideo> result = realm.where(RealmRecordedVideo.class).findAll();
                                    result.deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmRecordedVideo.class, response);
                                }
                            });
                            populateVideos(this);
                            fileListAdapter.notifyDataSetChanged();
                            ShowNoFileAvailableMsg();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        dialog.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    +
                            0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshDocs() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.refreshing));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "audios",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                RealmResults<RealmAudio> result = realm.where(RealmAudio.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmAudio.class, response);
                            });
                            populateDocs(this);
                            fileListAdapter.notifyDataSetChanged();
                            ShowNoFileAvailableMsg();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        dialog.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    +
                            0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ShowNoFileAvailableMsg() {
        if (myFiles.size() > 0) {
            nofile.setVisibility(View.GONE);
            recyclerview_files.setVisibility(View.VISIBLE);
        }
        else {
            nofile.setVisibility(View.VISIBLE);
            recyclerview_files.setVisibility(View.GONE);
        }
    }
}
