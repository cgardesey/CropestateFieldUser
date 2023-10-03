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
import com.cropestate.fielduser.adapter.LeaseAdapter;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmLease;
import com.cropestate.fielduser.realm.RealmLeaseHolder;
import com.cropestate.fielduser.util.RealmUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class LeasesActivity extends AppCompatActivity {
    protected static Typeface mTfLight;
    private ImageView backbtn, refresh;
    Button retrybtn;
    private String api_token, userid;
    TextView noleasetext;
    RecyclerView recyclerview;
    LeaseAdapter leaseAdapter;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    ArrayList<RealmLease> leaseArrayList = new ArrayList<>(), newLeases = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leases);
        recyclerview = findViewById(R.id.recyclerview);
        noleasetext = findViewById(R.id.noleasestext);
        backbtn = findViewById(R.id.search);
        refresh = findViewById(R.id.refresh);
        retrybtn = findViewById(R.id.retrybtn);
        backbtn = findViewById(R.id.backbtn1);
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

        leaseAdapter = new LeaseAdapter(leaseArrayList, 0);
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerview.setHasFixedSize(true);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(leaseAdapter);

        populateLease(getApplicationContext());
    }

    void populateLease(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmLease> results;
            results = realm.where(RealmLease.class).findAll();
            newLeases.clear();
            for (RealmLease realmLease : results) {

                RealmLeaseHolder realmLeaseHolder = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmLeaseHolder.class).equalTo("EMAIL_ID", realmLease.getEMAIL_ID()).findFirst();
                if (realmLeaseHolder != null) {
                    realmLease.setNAME(realmLeaseHolder.getNAME());
                }
                else {
                    realmLease.setNAME("Anonymous");
                }

                newLeases.add(realmLease);
            }
            leaseArrayList.clear();
            leaseArrayList.addAll(newLeases);
            leaseAdapter.notifyDataSetChanged();
            if (leaseArrayList.size() > 0) {
                noleasetext.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                noleasetext.setVisibility(View.VISIBLE);
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
                                RealmResults<RealmLease> result = realm.where(RealmLease.class).findAll();
                                result.deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmLease.class, response);
                            });
                            populateLease(getApplicationContext());
                            leaseAdapter.notifyDataSetChanged();
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
