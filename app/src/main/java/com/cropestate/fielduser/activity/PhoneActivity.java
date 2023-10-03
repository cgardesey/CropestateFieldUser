package com.cropestate.fielduser.activity;

        import android.Manifest;
        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.app.role.RoleManager;
        import android.content.ActivityNotFoundException;
        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.preference.PreferenceManager;
        import android.telecom.TelecomManager;
        import android.util.Log;
        import android.view.View;
        import android.view.animation.Animation;
        import android.view.animation.AnimationUtils;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.Toast;

        import androidx.core.app.ActivityCompat;
        import androidx.core.content.FileProvider;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.StringRequest;
        import com.greysonparrelli.permiso.Permiso;
        import com.greysonparrelli.permiso.PermisoActivity;
        import com.gsm.gsm.CallManager;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.pojo.MyFile;
        import com.cropestate.fielduser.realm.RealmAudio;
        import com.cropestate.fielduser.realm.RealmClassSessionDoc;
        import com.cropestate.fielduser.realm.RealmRecordedVideo;
        import com.cropestate.fielduser.util.CustomJsonObjectRequest;
        import com.cropestate.fielduser.util.DownloadFileAsync;
        import com.cropestate.fielduser.util.FilenameUtils;
        import com.cropestate.fielduser.util.RealmUtility;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.File;
        import java.util.HashMap;
        import java.util.Map;

        import static android.Manifest.permission.CALL_PHONE;
        import static com.cropestate.fielduser.activity.ChatActivity.COURSEPATH;
        import static com.cropestate.fielduser.activity.ChatActivity.INSTRUCTORCOURSEID;
        import static com.cropestate.fielduser.activity.ChatActivity.chatFile;
        import static com.cropestate.fielduser.activity.ClassroomActivity.CALLMODE;

        import static com.cropestate.fielduser.activity.ClassroomActivity.CLASSCALL;
        import static com.cropestate.fielduser.activity.ClassroomActivity.CLASSPLAYBACK;
        import static com.cropestate.fielduser.activity.ClassroomActivity.classFile;
        import static com.cropestate.fielduser.activity.FileListActivity.myFiles;
        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.getMimeType;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;

        import io.realm.Realm;
        import io.realm.RealmResults;
        import io.realm.Sort;

public class PhoneActivity extends PermisoActivity {

    public static final String ROOMID = "ROOMID";
    public static final int REQUEST_CODE_SET_DEFAULT_DIALER = 100;
    static public Context context;
    static public ProgressDialog mProgress;
    private static String[] sRequiredPermissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.MANAGE_OWN_CALLS};
    LinearLayout quiz, livechat, liveclass, recorded, doc, classdocs, videos;
    ImageView backbtn;
    final File extStorageDir = Environment.getExternalStorageDirectory();
    AsyncTask<String, Integer, String> downloadFileAsync;

    public static JSONObject classSessionJsonObj, chatSessionJsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        context = getApplicationContext();
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        quiz = findViewById(R.id.quiz);
        classdocs = findViewById(R.id.classdocs);
        videos = findViewById(R.id.videos);
        recorded = findViewById(R.id.recorded);
        livechat = findViewById(R.id.livechat);
        liveclass = findViewById(R.id.liveclass);
        doc = findViewById(R.id.upcomingdoc);
        backbtn = findViewById(R.id.search);

        backbtn.setOnClickListener(view -> {
            clickview(view);
            finish();
        });

        quiz.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), QuizzesActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                    .putExtra("ENROLMENTID", getIntent().getStringExtra("ENROLMENTID")))
            ;
        });

        classdocs.setOnClickListener(view -> {
            clickview(view);

            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, getIntent().getStringExtra("ENROLMENTID"))
                    .putString(FileListActivity.INSTRUCTORCOURSEID, getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                    .putString(FileListActivity.COURSEPATH, getIntent().getStringExtra("COURSEPATH"))
                    .apply();

            populateDocs(this);

            startActivity(new Intent(getApplicationContext(), FileListActivity.class)
                    .putExtra("activitytitle", getIntent().getStringExtra("COURSEPATH"))
                    .putExtra("assignmenttitle", context.getString(R.string.classdocs))
            );
        });

        videos.setOnClickListener(view -> {
            clickview(view);

            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, getIntent().getStringExtra("ENROLMENTID"))
                    .putString(FileListActivity.INSTRUCTORCOURSEID, getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                    .putString(FileListActivity.COURSEPATH, getIntent().getStringExtra("COURSEPATH"))
                    .apply();

            populateVideos(this);

            startActivity(new Intent(getApplicationContext(), FileListActivity.class)
                    .putExtra("activitytitle", getIntent().getStringExtra("COURSEPATH"))
                    .putExtra("assignmenttitle", context.getString(R.string.classvideos))
            );
        });

        recorded.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getApplicationContext(), DistinctAudiosActivity.class).putExtra("INSTRUCTORCOURSEID", getIntent().getStringExtra("INSTRUCTORCOURSEID")));

        });

        liveclass.setOnClickListener(view -> {
            clickview(view);
            liveClass();
        });

        livechat.setOnClickListener(view -> {
            clickview(view);
            gotonextActivity_chat();
        });

        doc.setOnClickListener(view -> {
            clickview(view);
            checkForClassDocument();
        });

        liveclass.setVisibility(View.VISIBLE);

        if (getIntent().getBooleanExtra("ISUPCOMING", false)) {
            doc.setVisibility(View.VISIBLE);
        } else {
            doc.setVisibility(View.GONE);
        }
    }

    public static void populateDocs(Activity activity) {
        myFiles.clear();

        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmAudio> audios = realm.where(RealmAudio.class)
                    .isNotNull("url")
                    .notEqualTo("url", "")
                    .distinct("url")
                    .equalTo("instructorcourseid", PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, ""))
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmAudio audio : audios) {
                realm.copyToRealmOrUpdate(new RealmClassSessionDoc(audio.getUrl(), PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, "")));
            }

            RealmResults<RealmClassSessionDoc> classSessionDocs = realm.where(RealmClassSessionDoc.class)
                    .equalTo("instructorcourseid", PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, ""))
                    .findAll();

            for (RealmClassSessionDoc classSessionDoc : classSessionDocs) {
                myFiles.add(new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.COURSEPATH, "").replace(" >> ", "/") + "/Class-sessions/Documents/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, "") + "/" + FilenameUtils.getName(classSessionDoc.getUrl()),
                        classSessionDoc.getUrl(),
                        null));
            }
        });
    }

    public static void populateVideos(Activity activity) {
        myFiles.clear();

        Realm.init(activity);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmRecordedVideo> realmRecordedVideos = realm.where(RealmRecordedVideo.class)
                    .equalTo("instructorcourseid", PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, ""))
                    .equalTo("isactive", 1)
                    .sort("id", Sort.DESCENDING)
                    .findAll();

            for (RealmRecordedVideo realmRecordedVideo : realmRecordedVideos) {
                myFiles.add(new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.COURSEPATH, "").replace(" >> ", "/") + "/Videos/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(FileListActivity.INSTRUCTORCOURSEID, "") + "/" +  FilenameUtils.getName(realmRecordedVideo.getUrl()),
                        realmRecordedVideo.getUrl(),
                        realmRecordedVideo.getGiflink()
                        ));
            }
        });
    }


    @Override
    protected void onStop() {
        if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
            // This would not cancel downloading from httpClient
            //  we have do handle that manually in onCancelled event inside AsyncTask
            downloadFileAsync.cancel(true);
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkSetDefaultDialerResult(resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkDefaultDialer() throws JSONException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        boolean isAlreadyDefaultDialer;

        try {
            isAlreadyDefaultDialer = telecomManager.getDefaultDialerPackage().equals(getPackageName());
        } catch (NullPointerException e) {
            isAlreadyDefaultDialer = false;
        }

        if (isAlreadyDefaultDialer) {
            onDefaultDialerSetInit();
            return;
        }

        RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
        startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
    }

    private void checkForChatDoc() {
        try {
            String docurl = chatSessionJsonObj.getString("docurl");
            if (!docurl.replace("null", "").trim().equals("")) {
                chatFile = new File(extStorageDir + "/SchoolDirectStudent/Chat-sessions/Documents", chatSessionJsonObj.getString("chatsessionid") + ".pdf");
                if (chatFile.exists()) {
                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                 @Override
                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                     if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                                                         gotonextActivity_chat();
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                     Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                 }
                                                             },
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    );
                    return;
                } else {
                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                 @Override
                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                     if (
                                                                             resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                                                             resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                                                                             resultSet.isPermissionGranted(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                                                                     ) {
                                                                         File parentFile = chatFile.getParentFile();
                                                                         if (!parentFile.exists()) {
                                                                             parentFile.mkdirs();
                                                                         }
                                                                         mProgress.setTitle(getString(R.string.downloading_chat_document));
                                                                         mProgress.show();
                                                                         downloadFileAsync = new DownloadFileAsync(response -> {
                                                                             mProgress.dismiss();
                                                                             if (response != null) {
                                                                                 // unsuccessful
                                                                                 if (chatFile.exists()) {
                                                                                     chatFile.delete();
                                                                                 }
                                                                                 Toast.makeText(getApplicationContext(), getString(R.string.download_failed), Toast.LENGTH_LONG).show();
                                                                             } else {
                                                                                 // successful
                                                                                 gotonextActivity_chat();
                                                                                 return;
                                                                             }
                                                                         }, progress -> {
                                                                             //                        holder.pbar.setProgress(progress);
                                                                         }, () -> {
                                                                             mProgress.dismiss();
                                                                             if (chatFile.exists()) {
                                                                                 chatFile.delete();
                                                                             }
                                                                             Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
                                                                         }).execute(docurl, chatFile.getAbsolutePath());
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                     Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                 }
                                                             },
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    );
                }
            } else {
                chatFile = null;
                gotonextActivity_chat();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onDefaultDialerSetInit() {
        try {
            String docurl = classSessionJsonObj.getString("docurl");
            if (!docurl.replace("null", "").trim().equals("")) {
                classFile = new File(extStorageDir + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Class-sessions/Documents/" + classSessionJsonObj.getString("instructorcourseid"), FilenameUtils.getName(classSessionJsonObj.getString("docurl")));
                if (classFile.exists()) {
                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                 @Override
                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                     if (resultSet.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                                                         gotonextActivity();
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                     Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                 }
                                                             },
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    );
                    return;
                } else {
                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                 @Override
                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                     if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                         File parentFile = classFile.getParentFile();
                                                                         if (!parentFile.exists()) {
                                                                             parentFile.mkdirs();
                                                                         }
                                                                         mProgress.setTitle(getString(R.string.downloading_class_document));
                                                                         mProgress.show();
                                                                         downloadFileAsync = new DownloadFileAsync(response -> {
                                                                             mProgress.dismiss();
                                                                             if (response != null) {
                                                                                 // unsuccessful
                                                                                 if (classFile.exists()) {
                                                                                     classFile.delete();
                                                                                 }
                                                                                 Toast.makeText(getApplicationContext(), getString(R.string.download_failed), Toast.LENGTH_LONG).show();
                                                                             } else {
                                                                                 // successful
                                                                                 gotonextActivity();
                                                                                 return;
                                                                             }
                                                                         }, progress -> {
                                                                             //                        holder.pbar.setProgress(progress);
                                                                         }, () -> {
                                                                             mProgress.dismiss();
                                                                             if (classFile.exists()) {
                                                                                 classFile.delete();
                                                                             }
                                                                             Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
                                                                         }).execute(docurl, classFile.getAbsolutePath());
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
            } else {
                classFile = null;
                gotonextActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkSetDefaultDialerResult(int resultCode) {
        String message;

        switch (resultCode) {
            case RESULT_OK:
                message = "기본 전화 앱으로 설정하였습니다.";
                mProgress.setTitle(getString(R.string.joining_class));
//                mProgress.show();
//                getAvailableRoom(getApplicationContext());
                onDefaultDialerSetInit();
                break;
            case RESULT_CANCELED:
                message = "기본 전화 앱으로 설정하지 않았습니다.";
                break;
            default:
                message = "Unexpected result code " + resultCode;
        }
    }

    private boolean ifNeededRequestPermission() {
        int check = 0;

        /**
         * 하나라도 permission이 없으면 check는 음수가 됨
         */
        for (String permission : sRequiredPermissions) {
            check += ActivityCompat.checkSelfPermission(this, permission);
        }

        return (check < 0);
    }

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);

    }

    public void liveChat() {
        try {
            mProgress.setTitle(getString(R.string.checking_for_chat_session));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "live-chats",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("eligible")) {
                                    if (jsonObject.isNull("chatsession")) {
                                        Toast.makeText(PhoneActivity.this, getString(R.string.no_live_chat_at_the_moment), Toast.LENGTH_LONG).show();
                                    } else {
                                        chatSessionJsonObj = jsonObject.getJSONObject("chatsession");
                                        checkForChatDoc();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
//                                Toast.makeText(PhoneActivity.this, getString(R.string.no_live_chat_at_the_moment), Toast.LENGTH_LONG).show();
                            }

                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("enrolmentid", getIntent().getStringExtra("ENROLMENTID"));
                    params.put("instructorcourseid", getIntent().getStringExtra("INSTRUCTORCOURSEID"));
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

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    public void liveClass() {
        try {
            mProgress.setTitle(getString(R.string.checking_for_live_class));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "live-classes",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("eligible")) {
                                    if (!jsonObject.isNull("classsession")) {
                                        classSessionJsonObj = jsonObject.getJSONObject("classsession");
                                        if (classSessionJsonObj.getInt("islive") == 0 || !jsonObject.getBoolean("no_participants_exist")) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                             @Override
                                                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                                 if (
                                                                                                         resultSet.isPermissionGranted(Manifest.permission.MANAGE_OWN_CALLS) &&
                                                                                                                 resultSet.isPermissionGranted(Manifest.permission.CALL_PHONE)
                                                                                                 ) {
                                                                                                     try {
                                                                                                         checkDefaultDialer();
                                                                                                     } catch (JSONException e) {
                                                                                                         e.printStackTrace();
                                                                                                     }
                                                                                                 }
                                                                                             }

                                                                                             @Override
                                                                                             public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                                                 Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                                                             }
                                                                                         },
                                                        Manifest.permission.MANAGE_OWN_CALLS,
                                                        Manifest.permission.CALL_PHONE
                                                );
                                            } else {
                                                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                             @Override
                                                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                                 if (
                                                                                                         resultSet.isPermissionGranted(CALL_PHONE)
                                                                                                 ) {
                                                                                                     try {
                                                                                                         checkDefaultDialer();
                                                                                                     } catch (JSONException e) {
                                                                                                         e.printStackTrace();
                                                                                                     }
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
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.your_instructor_is_yet_to_start_class), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(PhoneActivity.this, getString(R.string.no_live_class_at_the_moment), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                }


                            } catch (JSONException e) {
//                                Toast.makeText(PhoneActivity.this, getString(R.string.no_live_class_at_the_moment), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(getApplicationContext(), error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("enrolmentid", getIntent().getStringExtra("ENROLMENTID"));
                    params.put("instructorcourseid", getIntent().getStringExtra("INSTRUCTORCOURSEID"));
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

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    private void gotonextActivity_chat() {
        PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putString(INSTRUCTORCOURSEID, getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                .putString(COURSEPATH, getIntent().getStringExtra("COURSEPATH"))
                .apply();
        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
    }

    private void gotonextActivity() {
        try {
            ClassroomActivity.CLASSINSESSION = true;
            PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(ROOMID, classSessionJsonObj.getString("roomid"))
                    .apply();
            if (classSessionJsonObj.getInt("islive") == 1) {
                CALLMODE = CLASSCALL;
            } else {
                CALLMODE = CLASSPLAYBACK;
            }
            CallManager.get().placeCall(classSessionJsonObj.getString("dialcode"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkForClassDocument() {
        try {
            mProgress.setTitle(getString(R.string.checking_for_upcoming_class_document));
            mProgress.show();
            CustomJsonObjectRequest jsonObjectRequest = new CustomJsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "class-sessions/" + getIntent().getStringExtra("INSTRUCTORCOURSEID"),
                    null,
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            if (response.has("classsessionid")) {
                                try {
                                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                                 @Override
                                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                                     if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                                                         try {
                                                                                             classFile = new File(extStorageDir + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Class-sessions/Documents/" + getIntent().getStringExtra("INSTRUCTORCOURSEID"), FilenameUtils.getName(response.getString("docurl")));
                                                                                             File file_dir = new File(extStorageDir + "/SchoolDirectStudent/Class-sessions/", "Documents");
                                                                                             if (!file_dir.exists()) {
                                                                                                 file_dir.mkdirs();
                                                                                             }
                                                                                             classFile = new File(extStorageDir + "/SchoolDirectStudent/" + getIntent().getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Class-sessions/Documents/" + getIntent().getStringExtra("INSTRUCTORCOURSEID"), FilenameUtils.getName(response.getString("docurl")));
                                                                                             if (classFile.exists()) {
                                                                                                 viewDoc();
                                                                                                 return;
                                                                                             } else {
                                                                                                 downloadClassDoc(response.getString("docurl"));
                                                                                             }
                                                                                         } catch (JSONException e) {
                                                                                             e.printStackTrace();
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(PhoneActivity.this, getString(R.string.sorry_no_document_found), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(PhoneActivity.this, getString(R.string.sorry_no_document_found), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
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
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadClassDoc(String docUrl) {
        downloadFileAsync = new DownloadFileAsync(response -> {
            mProgress.dismiss();
            if (response != null) {
                // unsuccessful
                if (classFile.exists()) {
                    classFile.delete();
                }
                Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
            } else {
                // successful
                viewDoc();
                return;
            }
        }, progress -> {
            //                        holder.pbar.setProgress(progress);
        }, () -> {
            mProgress.dismiss();
            if (classFile.exists()) {
                classFile.delete();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
        }).execute(docUrl, classFile.getAbsolutePath());
    }

    private void viewDoc() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = getMimeType(classFile.getAbsolutePath());
        Uri docURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", classFile);
        intent.setDataAndType(docURI, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_suitable_app_for_viewing_this_file), Toast.LENGTH_LONG).show();
        }
    }
}
