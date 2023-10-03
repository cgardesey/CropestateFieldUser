package com.cropestate.fielduser.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.AttendanceAdapter;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmAttendance;
import com.cropestate.fielduser.realm.RealmAudio;
import com.cropestate.fielduser.realm.RealmCourse;
import com.cropestate.fielduser.realm.RealmInstructor;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.util.RealmUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class AttendanceActivity extends AppCompatActivity {
    protected static Typeface mTfLight;
    private ImageView backbtn, refresh;
    Button backbtn1, retrybtn;
    private String api_token, userid;
    TextView noattendancetext;
    RecyclerView recyclerview;
    AttendanceAdapter attendanceAdapter;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    ArrayList<RealmAttendance> attendanceArrayList = new ArrayList<>(), newAttendances = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        recyclerview = findViewById(R.id.recyclerview);
        noattendancetext = findViewById(R.id.noleasestext);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn1 = findViewById(R.id.backbtn1);
        backbtn1.setOnClickListener(new View.OnClickListener() {
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

        attendanceAdapter = new AttendanceAdapter(attendanceArrayList, 0);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(attendanceAdapter);

        populateAttendance(getApplicationContext());
    }

    void populateAttendance(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmAttendance> results;
            results = realm.where(RealmAttendance.class).equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, "")).findAll();
            newAttendances.clear();
            for (RealmAttendance realmAttendance : results) {

                RealmAudio realmAudio = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmAudio.class).equalTo("sessionid", realmAttendance.getAudioid()).findFirst();
                realmAttendance.setAudiotitle(realmAudio.getTitle());
                realmAttendance.setUpdated_at(realmAudio.getUrl());

                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmAudio.getInstructorcourseid()).findFirst();
                RealmInstructor realmInstructor = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();

                realmAttendance.setInstructorname(realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname());
                realmAttendance.setCoursepath(realmCourse.getCoursepath());
                newAttendances.add(realmAttendance);
            }
            attendanceArrayList.clear();
            attendanceArrayList.addAll(newAttendances);
            attendanceAdapter.notifyDataSetChanged();
            if (attendanceArrayList.size() > 0) {
                noattendancetext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                noattendancetext.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public void refresh() {
        try {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Refreshing attendance");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "attendances",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                RealmResults<RealmAttendance> result = realm.where(RealmAttendance.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmAttendance.class, response);
                            });
                            populateAttendance(getApplicationContext());
                            attendanceAdapter.notifyDataSetChanged();
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
}
