package com.cropestate.fielduser.activity;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.Build;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewTreeObserver;
        import android.view.animation.DecelerateInterpolator;
        import android.widget.ImageView;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.viewpager.widget.ViewPager;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonArrayRequest;
        import com.google.android.material.tabs.TabLayout;
        import com.takusemba.spotlight.SimpleTarget;
        import com.takusemba.spotlight.Spotlight;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.pagerAdapter.QuizPagerAdapter;
        import com.cropestate.fielduser.realm.RealmQuiz;
        import com.cropestate.fielduser.util.RealmUtility;

        import java.util.HashMap;
        import java.util.Map;

        import io.realm.Realm;
        import io.realm.RealmResults;

        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;
        import static com.cropestate.fielduser.fragment.UnsubmittedQuizzesFragment.populateUnsubmittedQuizzes;

public class QuizzesActivity extends AppCompatActivity {

    static final int
            ALL = 1,
            PENDING = 2,
            PAST = 3;
    static int LISTTYPE = ALL;

    ImageView backbtn, refresh;
    ProgressDialog dialog;
    public static String instructorcourseid, enrolmentid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);

        if (getIntent().getStringExtra("ENROLMENTID") != null) {
            enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        }
        if (getIntent().getStringExtra("INSTRUCTORCOURSEID") != null) {
            instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);

        refresh.setOnClickListener(v -> refresh());

        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.unsubmitted)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.submitted)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = findViewById(R.id.pager);
        final QuizPagerAdapter quizPagerAdapter = new QuizPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    refresh.setVisibility(View.VISIBLE);
                }
                else {
                    refresh.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(quizPagerAdapter);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.unsubmitted));
        tabLayout.getTabAt(1).setText(getString(R.string.submitted));

        backbtn.setOnClickListener(v -> finish());

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("QUIZZES_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = refresh.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        refresh.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        refresh.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder(QuizzesActivity.this).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(getString(R.string.check_for_new_quizzes_tip) + getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with(QuizzesActivity.this)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightEndedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putBoolean("QUIZZES_ACTIVITY_TIPS_DISMISSED", true)
                                    .apply())
                            .start();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("ENROLMENTID") != null) {
            enrolmentid = getIntent().getStringExtra("ENROLMENTID");
        }

        if (intent.getStringExtra("INSTRUCTORCOURSEID") != null) {
            instructorcourseid = getIntent().getStringExtra("INSTRUCTORCOURSEID");
        }
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.checking_for_new_quizzes));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "quizzes",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<RealmQuiz> result = realm.where(RealmQuiz.class).findAll();
                                    result.deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmQuiz.class, response);
                                }
                            });
                            populateUnsubmittedQuizzes();
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
