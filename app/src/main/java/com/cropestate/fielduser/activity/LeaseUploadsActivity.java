package com.cropestate.fielduser.activity;

        import android.Manifest;
        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.Typeface;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.GridLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonArrayRequest;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.adapter.LeaseUploadsAdapter;
        import com.cropestate.fielduser.constants.keyConst;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.other.MyHttpEntity;
        import com.cropestate.fielduser.realm.RealmLeaseUpload;
        import com.cropestate.fielduser.util.RealmUtility;
        import com.google.android.material.floatingactionbutton.FloatingActionButton;
        import com.greysonparrelli.permiso.Permiso;
        import com.greysonparrelli.permiso.PermisoActivity;
        import com.yalantis.ucrop.util.FileUtils;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.entity.mime.MultipartEntityBuilder;
        import org.apache.http.entity.mime.content.FileBody;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.util.EntityUtils;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.File;
        import java.io.IOException;
        import java.io.UnsupportedEncodingException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.UUID;

        import io.realm.Realm;
        import io.realm.RealmResults;
        import vn.tungdx.mediapicker.MediaItem;
        import vn.tungdx.mediapicker.MediaOptions;
        import vn.tungdx.mediapicker.activities.MediaPickerActivity;

        import static com.arthenica.mobileffmpeg.FFmpeg.executeAsync;
        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.fileSize;
        import static com.cropestate.fielduser.constants.Const.isExternalStorageWritable;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class LeaseUploadsActivity extends PermisoActivity {
    protected static Typeface mTfLight;
    private static final int REQUEST_MEDIA = 1002;
    private ImageView backbtn, refresh;
    Button retrybtn;
    private String api_token, userid;
    TextView noleaseuploadstext;
    RecyclerView recyclerview;
    LeaseUploadsAdapter leaseUploadsAdapter;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    private List<MediaItem> mMediaSelectedList;
    FloatingActionButton gal;
    GridLayoutManager gridLayoutManager;
    ArrayList<RealmLeaseUpload> leaseUploadsArrayList = new ArrayList<>(), newLeaseUploads = new ArrayList<>();
    public static Activity leaseUploadsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lease_uploads);
        recyclerview = findViewById(R.id.recyclerview);
        leaseUploadsActivity = this;
        noleaseuploadstext = findViewById(R.id.noleaseuploadstext);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn = findViewById(R.id.backbtn1);
        gal = findViewById(R.id.gal);
        gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaSelectedList != null) {
                    mMediaSelectedList.clear();
                }
                MediaOptions.Builder builder = new MediaOptions.Builder();
                MediaOptions options = builder.canSelectMultiPhoto(true)
                        .canSelectMultiVideo(true).canSelectBothPhotoVideo()
                        .setMediaListSelected(mMediaSelectedList).build();

                if (options != null) {
                    MediaPickerActivity.Companion.open(LeaseUploadsActivity.this, REQUEST_MEDIA, options);
                }
            }
        });
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        leaseUploadsAdapter = new LeaseUploadsAdapter(leaseUploadsArrayList, 0);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerview.setLayoutManager(gridLayoutManager);
        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(leaseUploadsAdapter);

        populateLeases(getApplicationContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String leaseuploadsid = UUID.randomUUID().toString();
        String chatid = leaseuploadsid;
        switch (requestCode) {
            case REQUEST_MEDIA:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mMediaSelectedList = MediaPickerActivity.Companion
                                .getMediaItemSelected(data);
                        if (mMediaSelectedList != null) {

                            for (MediaItem mediaItem : mMediaSelectedList) {
                                String path = mediaItem.getPathOrigin(getApplicationContext());
                                File file = new File(path);
                                if (file.length() > 10000000L) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.file_size_of) + " " + fileSize(file.length()) + " " + getString(R.string.larger_than_limit), Toast.LENGTH_SHORT).show();
                                } else {
                                    RealmLeaseUpload realmLeaseUpload = new RealmLeaseUpload(
                                            leaseuploadsid,
                                            "",
                                            "",
                                            "",
                                            "URI" + file.getAbsolutePath(),
                                            null,
                                            null
                                    );
                                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                 @Override
                                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                     if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                         if (isExternalStorageWritable()) {
                                                                                             String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                                                                             String destinationPath = Environment.getExternalStorageDirectory() + "/CropEstate/Images/" + leaseuploadsid + ext ;
                                                                                             File destinationfile = new File(destinationPath);
                                                                                             File parentFile = destinationfile.getParentFile();
                                                                                             if (!parentFile.exists()) {
                                                                                                 parentFile.mkdirs();
                                                                                             }
                                                                                             try {
                                                                                                 FileUtils.copyFile(file.getAbsolutePath(), destinationPath);
                                                                                             } catch (IOException e) {
                                                                                                 e.printStackTrace();
                                                                                             }
                                                                                             saveTempLeaseUploadToRealm(realmLeaseUpload);
                                                                                             new SendLeaseUploadAsyncTask(getApplicationContext(), realmLeaseUpload).execute();
                                                                                         }
                                                                                     }
                                                                                 }
                                                                                 @Override
                                                                                 public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                                     Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                                 }
                                                                             },
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    );
                                }
                            }
//                            scrollview.setVisibility(View.VISIBLE);
//                            mMediaSelectedList.clear();
                        } else {
                            Log.e("fdsaasd", "Error to get media, NULL");
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            default:
                break;

        }
    }

    void populateLeases(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmLeaseUpload> results;
            results = realm.where(RealmLeaseUpload.class).equalTo("leasecode", getIntent().getStringExtra("LEASECODE")).findAll();
            newLeaseUploads.clear();
            for (RealmLeaseUpload realmLeaseUpload : results) {
                newLeaseUploads.add(realmLeaseUpload);
            }
            leaseUploadsArrayList.clear();
            leaseUploadsArrayList.addAll(newLeaseUploads);
            leaseUploadsAdapter.notifyDataSetChanged();
            if (leaseUploadsArrayList.size() > 0) {
                noleaseuploadstext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                noleaseuploadstext.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public void refresh() {
        try {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Refreshing leases");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "leases",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                RealmResults<RealmLeaseUpload> result = realm.where(RealmLeaseUpload.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmLeaseUpload.class, response);
                            });
                            populateLeases(getApplicationContext());
                            leaseUploadsAdapter.notifyDataSetChanged();
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

    public void saveTempLeaseUploadToRealm(RealmLeaseUpload realmLeaseUpload) {
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(realmLeaseUpload);
            leaseUploadsArrayList.add(realmLeaseUpload);
            leaseUploadsAdapter.notifyItemInserted(leaseUploadsArrayList.size() - 1);
            gridLayoutManager.scrollToPositionWithOffset(leaseUploadsArrayList.size() - 1, 0);
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.uploading_file), Toast.LENGTH_SHORT).show();
        });
    }

    private class SendLeaseUploadAsyncTask extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        RealmLeaseUpload realmLeaseUpload;
        private Context context;
        private Exception exception;
        // private ProgressDialog progressDialog;

        private SendLeaseUploadAsyncTask(Context context, RealmLeaseUpload realmLeaseUpload) {
            this.context = context;
            this.realmLeaseUpload = realmLeaseUpload;
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = keyConst.API_URL + "lease-uploads";
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                Uri uri = Uri.parse(realmLeaseUpload.getUrl().substring(3));
                File file = new File(uri.getPath());
                multipartEntityBuilder.addPart("file", new FileBody(file));

                multipartEntityBuilder.addTextBody("leaseuploadid", realmLeaseUpload.getLeaseuploadid());
                multipartEntityBuilder.addTextBody("leasecode", leaseUploadsActivity.getIntent().getStringExtra("LEASECODE"));
                multipartEntityBuilder.addTextBody("title", realmLeaseUpload.getTitle());
                multipartEntityBuilder.addTextBody("description", realmLeaseUpload.getDescription());
                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                } else {
                    responseString = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
                this.exception = e;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.contains("Error occurred! Http Status Code: ")) {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
                else if (result.contains("connect")){
                    Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                }
                else {
                    JSONObject responseJson = null;
                    try {
                        responseJson = new JSONObject(result);
                        JSONObject finalResponseJson = responseJson;
                        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmLeaseUpload.class, finalResponseJson);
                        });
                        populateLeases(context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }
    }
}
