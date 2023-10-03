package com.cropestate.fielduser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cropestate.fielduser.realm.RealmLease;
import com.cropestate.fielduser.realm.RealmLeaseUpload;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.greysonparrelli.permiso.PermisoActivity;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.fragment.EnrolmentsFragment;
import com.cropestate.fielduser.fragment.SearchCourseFragment;
import com.cropestate.fielduser.fragment.SettingsFragment;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmEnrolment;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.realm.RealmStudent;
import com.cropestate.fielduser.realm.RealmUser;
import com.cropestate.fielduser.receiver.NetworkReceiver;
import com.cropestate.fielduser.util.FCMAsyncTask;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.cropestate.fielduser.activity.ChatActivity.sendUnsentChats;
import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.PhoneActivity.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;

public class HomeActivity extends PermisoActivity implements SettingsFragment.Callbacks {

    private String TAG;
    public static String ACCESSTOKEN = "ACCESSTOKEN";
    public static String JUSTENROLLED = "JUSTENROLLED";
    public static int RC_ACCOUNT = 435;
    NetworkReceiver networkReceiver;
    static BottomNavigationView navigation;
    FloatingActionButton close;
    public static Context context;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        public Fragment fragment;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                fragment = new SearchCourseFragment();
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.navigation_learn) {
                fragment = new EnrolmentsFragment();
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                fragment = new SettingsFragment();
                loadFragment(fragment);
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if (InitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_home);
        close = findViewById(R.id.close);
        close.setOnClickListener(v -> {
//            changeDefaultDialer(HomeActivity.this, getPackagesOfDialerApps(getApplicationContext()).get(0));
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
//            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
        });
        new FCMAsyncTask(getApplicationContext()).execute();

        Realm.init(
                getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmEnrolment> enrolments = realm.where(RealmEnrolment.class).equalTo("enrolled", 1).findAll();
            for (RealmEnrolment enrolment : enrolments) {
                String instructorcourseid = realm.where(RealmInstructorCourse.class).equalTo("instructorcourseid", enrolment.getInstructorcourseid()).findFirst().getInstructorcourseid();
                FirebaseMessaging.getInstance().subscribeToTopic(instructorcourseid)
                        .addOnCompleteListener(task -> {
                            String msg = "subscribed";
                            if (!task.isSuccessful()) {
                                msg = "unsubscribed";
                            }
                            Log.d("engineer:sub_status:", msg);
                        });
            }
        });


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    Log.d("engineer", token);
                    retriev_current_registration_token(getApplicationContext(), token);
                });

        loadFragment(new SearchCourseFragment());
        //navigation.setSelectedItemId(R.id.navigation_home);

        networkReceiver = new NetworkReceiver();

        navigation = findViewById(R.id.navigation);
        //BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        boolean signedIn = !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "").equals("");
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            int size = realm.where(RealmEnrolment.class)
                    .equalTo("enrolled", 1)
                    .equalTo("approved", true)
                    .findAll().size();

            navigation.getMenu().getItem(0).setEnabled(true);
            navigation.getMenu().getItem(1).setEnabled(true);
            navigation.getMenu().getItem(2).setEnabled(true);

            if (signedIn && size > 0) {
                navigation.getMenu().getItem(1).setChecked(true);
                Fragment fragment = new EnrolmentsFragment();
                loadFragment(fragment);

            } else {
                navigation.getMenu().getItem(0).setChecked(true);
                Fragment fragment = new SearchCourseFragment();
                loadFragment(fragment);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmStudent realmStudent = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst();
            boolean accountInfoIncomplete =
                    realmStudent.getTitle() == null ||
                    realmStudent.getFirstname() == null ||
                    realmStudent.getLastname() == null ||
                    realmStudent.getGender() == null ||
                    realmStudent.getEmailaddress() == null;
            if (accountInfoIncomplete) {
                int requestCode = 435;
                startActivityForResult(new Intent(getApplicationContext(), AccountActivity.class), requestCode);
            }
            else {
                boolean just_enrolled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(JUSTENROLLED, false);
                if (just_enrolled) {
                    PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putBoolean(JUSTENROLLED, false)
                            .apply();
                    navigation.getMenu().getItem(1).setChecked(true);
                    Fragment fragment = new EnrolmentsFragment();
                    loadFragment(fragment);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ACCOUNT) {
            Realm.init(getApplicationContext());
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                boolean accountInfoNotSet = realm.where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "")).findFirst().getFirstname() == null;
                if (accountInfoNotSet) {
                    finish();
                }
            });
        }
        else if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
//            finish();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.homeframe, fragment);
        transaction.commit();
    }


    @Override
    public void onChangeNightMOde() {
        if (InitApplication.getInstance().isNightModeEnabled()) {
            InitApplication.getInstance().setIsNightModeEnabled(false);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);

        } else {
            InitApplication.getInstance().setIsNightModeEnabled(true);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }

    }

    public static void dialog(Context context, boolean value) {

        if (value) {
            //   tv_check_connection.setVisibility(View.VISIBLE);

        } else {
            Snackbar snackbar = Snackbar
                    .make(navigation, context.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.ok).toUpperCase(), view -> {

                    });

            snackbar.show();
        }
    }

    public static void fetchAllMyData(Context context) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "all-data",
                    null,
                    responseJson -> {
                        if (responseJson != null) {
                            Realm.init(context);
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                try {
                                    persistAll(realm, responseJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                            sendUnsentChats();
                        }
                    },
                    error -> {

                    }
            ){
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
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

    public static void persistAll(Realm realm, JSONObject responseJson) throws JSONException {
        realm.createOrUpdateAllFromJson(RealmUser.class, responseJson.getJSONArray("users"));
        realm.createOrUpdateAllFromJson(RealmLease.class, responseJson.getJSONArray("leases"));
        JSONArray lease_holders_json = responseJson.getJSONArray("lease_holders");
        realm.createOrUpdateAllFromJson(RealmLeaseUpload.class, responseJson.getJSONArray("lease_uploads"));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getDefaultDialerPackage(Context context) {
        TelecomManager manger= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            manger = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
        }
        String name=manger.getDefaultDialerPackage();
        return name;
    }

    public static void retriev_current_registration_token(Context context, String confirmation_token) {
        JSONObject request = new JSONObject();

        try {
            request.put("confirmation_token", confirmation_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                API_URL + "update-confirmation-token/" + PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""),
                request,
                response -> {
                },
                error -> {
                }
        ) {
            /** Passing some request headers* */

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
