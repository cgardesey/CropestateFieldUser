package com.cropestate.fielduser.activity;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.View;
        import android.view.animation.Animation;
        import android.view.animation.AnimationUtils;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonArrayRequest;
        import com.greysonparrelli.permiso.PermisoActivity;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.adapter.AudioAdapter;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.realm.RealmAttendance;
        import com.cropestate.fielduser.realm.RealmAudio;
        import com.cropestate.fielduser.realm.RealmCourse;
        import com.cropestate.fielduser.realm.RealmInstructorCourse;
        import com.cropestate.fielduser.util.RealmUtility;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

        import io.realm.Realm;
        import io.realm.RealmResults;
        import io.realm.Sort;

        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class DistinctAudiosActivity extends PermisoActivity {

    public static final int
            ALL = 1,
            PLAYED = 2,
            UNPLAYED = 3;

    static int LISTTYPE = ALL;
    RecyclerView recyclerview;
    ImageView backbtn, refresh;
    ArrayList<RealmAudio> audios = new ArrayList<>(), realmAudios = new ArrayList<>(), plyedAudios = new ArrayList<>(), unplayedAudios = new ArrayList<>();
    TextView noaudiotext;
    static public ProgressDialog mProgress;
    AudioAdapter audioAdapter;
    ProgressDialog dialog;

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
        refresh.setOnClickListener(v -> refresh());
        realmAudios = new ArrayList<>();
        recyclerview = findViewById(R.id.recyclerview);
        populateAllAudio();
        audios.addAll(realmAudios);
        audioAdapter = new AudioAdapter((realmAudios, position, holder) -> {
            RealmAudio realmAudio = realmAudios.get(position);
            startActivity(new Intent(DistinctAudiosActivity.this, AudiosActivity.class)
                    .putExtra("title", realmAudio.getTitle())
                    .putExtra("COURSEPATH", realmAudio.getCoursepath())
                    .putExtra("INSTRUCTORCOURSEID", getIntent().getStringExtra("INSTRUCTORCOURSEID"))
            );
        }, DistinctAudiosActivity.this, realmAudios);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(audioAdapter);
        backbtn = findViewById(R.id.search);
        backbtn.setOnClickListener(view -> {
            clickview(view);
            finish();
        });
    }

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);
    }

    public void populateAllAudio() {
        LISTTYPE = ALL;
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmAudio> results = realm.where(RealmAudio.class)
                    .distinct("title")
                    .equalTo("instructorcourseid", getIntent().getStringExtra("INSTRUCTORCOURSEID"))
                    .sort("id", Sort.DESCENDING)
                    .findAll();
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

            RealmResults<RealmAudio> results = realm.where(RealmAudio.class).distinct("title").findAll();
            plyedAudios.clear();
            for (RealmAudio realmAudio : results) {
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
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmAudio> result = realm.where(RealmAudio.class).findAll();
                                    result.deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmAudio.class, response);
                                }
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

}
