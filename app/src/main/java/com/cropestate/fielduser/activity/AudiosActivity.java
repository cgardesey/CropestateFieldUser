package com.cropestate.fielduser.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.cropestate.fielduser.constants.keyConst;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.gsm.gsm.CallManager;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.AudioAdapter;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmAttendance;
import com.cropestate.fielduser.realm.RealmAudio;
import com.cropestate.fielduser.realm.RealmCourse;
import com.cropestate.fielduser.realm.RealmEnrolment;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.util.DownloadFileAsync;
import com.cropestate.fielduser.util.FilenameUtils;
import com.cropestate.fielduser.util.RealmUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.Manifest.permission.CALL_PHONE;
import static com.cropestate.fielduser.activity.ClassroomActivity.CALLMODE;

import static com.cropestate.fielduser.activity.ClassroomActivity.classFile;
import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.PhoneActivity.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.cropestate.fielduser.activity.PhoneActivity.ROOMID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class AudiosActivity extends PermisoActivity {

    static public final int
        ALL = 1,
        PLAYED = 2,
        UNPLAYED = 3;

    static int LISTTYPE = ALL;
    RecyclerView recyclerview;
    ImageView backbtn, refresh;
    ArrayList<RealmAudio> audios = new ArrayList<>(), realmAudios = new ArrayList<>(), plyedAudios = new ArrayList<>(), unplayedAudios = new ArrayList<>();
    TextView noaudiotext;
    public static RealmAudio clickedRealmAudio;
    static public ProgressDialog mProgress;
    JSONObject roomJsonObject;
    AudioAdapter audioAdapter;
    ProgressDialog dialog;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distinct_audio);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        refresh = findViewById(R.id.refresh);
        noaudiotext = findViewById(R.id.noaudiotext);
        title = findViewById(R.id.title);
        title.setText(getIntent().getStringExtra("title"));
        refresh.setOnClickListener(v -> refresh());
        realmAudios = new ArrayList<>();
        recyclerview = findViewById(R.id.recyclerview);
        populateAllAudio();
        audios.addAll(realmAudios);
        audioAdapter = new AudioAdapter((realmAudios, position, holder) -> {
            RealmAudio realmAudio = realmAudios.get(position);
            clickedRealmAudio = realmAudio;
            Realm.init(getApplicationContext());
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                String enrolmentid = realm.where(RealmEnrolment.class).equalTo("instructorcourseid", clickedRealmAudio.getInstructorcourseid()).findFirst().getEnrolmentid();
                playback(enrolmentid);
            });
        }, AudiosActivity.this, realmAudios);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(audioAdapter);
        backbtn = findViewById(R.id.search);
        backbtn.setOnClickListener(view -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkSetDefaultDialerResult(resultCode);
        }
    }

    public void populateAllAudio() {
        LISTTYPE = ALL;
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmAudio> results = realm.where(RealmAudio.class)
                    .equalTo("instructorcourseid", getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                    .equalTo("title", getIntent().getStringExtra("title"))
                    .findAll()
                    .sort("id", Sort.DESCENDING);
            if (results.size() > 0) {
                noaudiotext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                noaudiotext.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
            realmAudios.clear();
            for (RealmAudio realmAudio : results) {
                RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmAudio.getInstructorcourseid()).findFirst();
                RealmCourse realmCourse = realm.where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                realmAudio.setCoursepath(realmCourse.getCoursepath());
                realmAudios.add(realmAudio);
            }
        });
    }

    public void populatePlayedAudio() {
        LISTTYPE = PLAYED;
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmAudio> results = realm.where(RealmAudio.class).equalTo("attended", true).findAll();
            plyedAudios.clear();
            for (RealmAudio realmAudio : results) {
                boolean attended = realm.where(RealmAttendance.class).equalTo("audioid", realmAudio.getAudioid()).findFirst() != null;
                realmAudio.setAttended(attended);
                plyedAudios.add(realmAudio);
            }
        });
    }

    public void populateUnplayedAudio() {
        LISTTYPE = UNPLAYED;
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmAudio> results = realm.where(RealmAudio.class).equalTo("attended", false).findAll();
            unplayedAudios.clear();
            for (RealmAudio realmAudio : results) {
                boolean attended = realm.where(RealmAttendance.class).equalTo("audioid", realmAudio.getAudioid()).findFirst() != null;
                realmAudio.setAttended(attended);
                unplayedAudios.add(realmAudio);
            }
        });
    }

    public void refresh() {
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
                            audios.clear();
                            switch (LISTTYPE) {
                                case ALL:
                                    populateAllAudio();
                                    audios.addAll(realmAudios);
                                    break;
                                case PLAYED:
                                    populatePlayedAudio();
                                    audios.addAll(plyedAudios);
                                    break;
                                case UNPLAYED:
                                    populateUnplayedAudio();
                                    audios.addAll(unplayedAudios);
                                    break;
                            }
                            audioAdapter.notifyDataSetChanged();
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
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playback(String enrolmentid) {
        try {
            mProgress.setTitle(getString(R.string.checking_for_available_room));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    keyConst.API_URL + "room",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("eligible")) {
                                    roomJsonObject = new JSONArray(jsonObject.getString("contents")).getJSONObject(0);
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
                                }
                                else {
                                    Toast.makeText(AudiosActivity.this, getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                }


                            } catch (JSONException e) {
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
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("enrolmentid", enrolmentid);
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

    private void onDefaultDialerSetInit() {

        if (clickedRealmAudio.getUrl() != null && !clickedRealmAudio.getUrl().trim().equals("")) {
            classFile = new File(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + clickedRealmAudio.getCoursepath().replace(" >> ", "/") + "/Class-sessions/Documents/" + getIntent().getStringExtra("INSTRUCTORCOURSEID"), FilenameUtils.getName(AudiosActivity.clickedRealmAudio.getUrl()));
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
                                                                     new DownloadFileAsync(response -> {
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
                                                                         Toast.makeText(getApplicationContext(), getString(R.string.download_cancelled), Toast.LENGTH_LONG).show();
                                                                     }).execute(AudiosActivity.clickedRealmAudio.getUrl(), classFile.getAbsolutePath());
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

    private void gotonextActivity() {
        mProgress.dismiss();
        try {
            PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putString(ROOMID, clickedRealmAudio.getSessionid())
                    .apply();
            CALLMODE = ClassroomActivity.CLASSPLAYBACK;
            CallManager.get().placeCall(this.roomJsonObject.getString("code"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
