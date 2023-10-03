package com.cropestate.fielduser.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.tabs.TabLayout;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.pagerAdapter.AssignmentPagerAdapter;
import com.cropestate.fielduser.realm.RealmAssignment;
import com.cropestate.fielduser.realm.RealmCourse;
import com.cropestate.fielduser.util.RealmUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;
import static com.cropestate.fielduser.fragment.AssignedFragment.initAssignedFragment;

public class AssignmentActivity extends AppCompatActivity {
    ArrayList<RealmCourse> courses = new ArrayList<>();
    ImageView backbtn;
    public static ImageView refreshassignmentsimg;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        backbtn = findViewById(R.id.search);
        refreshassignmentsimg = findViewById(R.id.refresh);

        refreshassignmentsimg.setOnClickListener(v -> refresh());

        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.unsubmitted)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.submitted)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = findViewById(R.id.pager);
        final AssignmentPagerAdapter assignmentPagerAdapter = new AssignmentPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    refreshassignmentsimg.setVisibility(View.VISIBLE);
                }
                else {
                    refreshassignmentsimg.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(assignmentPagerAdapter);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.unsubmitted));
        tabLayout.getTabAt(1).setText(getString(R.string.submitted));

        backbtn.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.checking_for_new_assignment));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    API_URL + "assignments",
                    null,
                    response -> {
                        if (response != null) {
                            dialog.dismiss();
                            Realm.init(getApplicationContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> realm.createOrUpdateAllFromJson(RealmAssignment.class, response));
                            initAssignedFragment(getApplicationContext());
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
