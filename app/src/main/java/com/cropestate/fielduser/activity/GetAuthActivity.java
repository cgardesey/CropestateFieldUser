package com.cropestate.fielduser.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.gsm.gsm.CallManager;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmDialcode;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import pub.devrel.easypermissions.AppSettingsDialog;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.MANAGE_OWN_CALLS;
import static com.cropestate.fielduser.activity.ClassroomActivity.CALLMODE;

import static com.cropestate.fielduser.activity.ClassroomActivity.VERIFICATIONCALL;
import static com.cropestate.fielduser.activity.HomeActivity.persistAll;
import static com.cropestate.fielduser.activity.PhoneActivity.REQUEST_CODE_SET_DEFAULT_DIALER;
import static com.cropestate.fielduser.activity.PhoneActivity.ROOMID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.keyConst.APP_HASH;
import static com.cropestate.fielduser.constants.keyConst.CALL_API_BASE_URL;
import static com.cropestate.fielduser.constants.Const.changeDefaultDialer;
import static com.cropestate.fielduser.constants.Const.myVolleyError;


/**
 * Created by Nana on 11/26/2017.
 */

public class GetAuthActivity extends PermisoActivity {

    public static String APITOKEN = "APITOKEN";
    public static String MYUSERID = "MYUSERID";

    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    public static Context getAuthActivityContext;
    Button changenumberbtn;
    LinearLayout retry_area;
    public static Button resendbtn;
    private int RESOLVE_HINT = 2;
    public static TextView welcomemsg, multiplesimtext;
    static int authTry = 0;
    ProgressDialog dialog;
    public static Button completeverificationbtn;
    private String api_token, userid, room_number;
    public static String code = "";
    private String deviceModel, deviceManufacturer, android_id, os;
    int height, width;
    public static CountDownTimer countDownTimer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_auth);

        getAuthActivityContext = getApplicationContext();
        retry_area = findViewById(R.id.retry_area);
        changenumberbtn = findViewById(R.id.changenumberbtn);
        multiplesimtext = findViewById(R.id.multiplesimtext);
        resendbtn = findViewById(R.id.resendbtn);
        welcomemsg = findViewById(R.id.welcomemsg);
        completeverificationbtn = findViewById(R.id.completeverificationbtn);
        deviceModel = Build.MODEL;
        deviceManufacturer = Build.MANUFACTURER;
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        os = Build.VERSION.RELEASE;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        completeverificationbtn.setOnClickListener(view -> verifyPhone());

        changenumberbtn.setOnClickListener(view -> finish());
        resendbtn.setOnClickListener(view -> getCallAuth());

        getCallAuth();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void shouldShowRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (
                    !shouldShowRequestPermissionRationale(CALL_PHONE) &&
                            !shouldShowRequestPermissionRationale(MANAGE_OWN_CALLS)
            ) {
                new AppSettingsDialog.Builder(this).build().show();
            }
        } else {
            if (
                    !shouldShowRequestPermissionRationale(CALL_PHONE)
            ) {
                new AppSettingsDialog.Builder(this).build().show();
            }
        }
    }

    private void placeCall(String code) {
        PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putString(ROOMID, "123456")
                .apply();
        starttimer();
        CALLMODE = VERIFICATIONCALL;
        Realm.init(getAuthActivityContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(new RealmDialcode(code));
        });
        CallManager.get().placeCall(code);
    }

    public void getRoom() {
        String URL = null;
        URL = CALL_API_BASE_URL + "api/v1/room/";
        try {

            JSONObject jsonBody = new JSONObject();

            Log.i("bbbb", URL);
            JsonArrayRequest jsonOblect = new JsonArrayRequest(Request.Method.GET, URL, null, response -> {
                dialog.dismiss();
                if (response == null) {
                    return;
                }
                Log.i("bbbb", response.toString());
                try {
                    int length = response.length();

                    JSONObject jsonObject = (JSONObject) response.get(0);

                    room_number = jsonObject.getString("room_number");
                    code = jsonObject.getString("code");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                     @Override
                                                                     public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                         if (
                                                                                 resultSet.isPermissionGranted(CALL_PHONE) &&
                                                                                         resultSet.isPermissionGranted(MANAGE_OWN_CALLS)
                                                                         ) {
                                                                             try {
                                                                                 checkDefaultDialer();
                                                                             } catch (JSONException e) {
                                                                                 e.printStackTrace();
                                                                             }
                                                                         } else {
                                                                             dialog.dismiss();
                                                                             shouldShowRationale();
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
                                                                             try {
                                                                                 checkDefaultDialer();
                                                                             } catch (JSONException e) {
                                                                                 e.printStackTrace();
                                                                             }
                                                                         } else {
                                                                             dialog.dismiss();
                                                                             shouldShowRationale();
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

                } catch (JSONException e) {

                }
            }, error -> {

                myVolleyError(getApplicationContext(), error);
                welcomemsg.setText(getResources().getString(R.string.error_contacting_server));
                dialog.dismiss();
                stoptimer();
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    return headers;
                }
            };
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void starttimer() {
        authTry = authTry + 1;
        countDownTimer.start();
    }

    public static void stoptimer() {
        authTry = 0;
        resendbtn.setEnabled(true);
        resendbtn.setText("Retry");
        countDownTimer.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkSetDefaultDialerResult(resultCode);
        }
    }

    private void verifyPhone() {
        ProgressDialog mProgress = new ProgressDialog(GetAuthActivity.this);
        mProgress.setTitle(getString(R.string.completing_verification));
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.show();
        try {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "confirm-registration",
                    response -> {
                        try {
                            mProgress.dismiss();
                            JSONObject responseJson = new JSONObject(response);

                            if (responseJson.getInt("connected") == 1) {
                                if (responseJson.has("users")) {
                                    Realm.init(getAuthActivityContext);
                                    Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                        try {
                                            persistAll(realm, responseJson);
                                            /*JSONArray timetableJsonArray = responseJson.getJSONArray("timetables");
                                            realm.createOrUpdateAllFromJson(RealmTimetable.class, timetableJsonArray);*/

                                            PreferenceManager
                                                    .getDefaultSharedPreferences(getApplicationContext())
                                                    .edit()
                                                    .putString(MYUSERID, responseJson.getString("userid"))
                                                    .putString(APITOKEN, responseJson.getString("api_token"))
                                                    .apply();

                                            mProgress.dismiss();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                            GetPhoneNumberActivity.getInstance().finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    completeverificationbtn.setVisibility(View.VISIBLE);
                                    retry_area.setVisibility(View.GONE);

                                } else {
                                    PreferenceManager
                                            .getDefaultSharedPreferences(getApplicationContext())
                                            .edit()
                                            .putString(MYUSERID, responseJson.getString("userid"))
                                            .putString(APITOKEN, responseJson.getString("api_token"))
                                            .apply();

                                    mProgress.dismiss();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    finish();
                                    GetPhoneNumberActivity.getInstance().finish();
                                }
                            } else {
                                welcomemsg.setText(getResources().getString(R.string.verification_failed));
                                completeverificationbtn.setVisibility(View.GONE);
                                retry_area.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        mProgress.dismiss();
                        myVolleyError(getApplicationContext(), error);
                        stoptimer();
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("phonenumber", GetPhoneNumberActivity.phonenumber);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            mProgress.dismiss();
            Log.e("My error", e.toString());
        }
    }

    public void getCallAuth() {

        dialog = ProgressDialog.show(GetAuthActivity.this, "", getResources().getString(R.string.shortfreecall), true);
        countDownTimer = new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                resendbtn.setText("Retry in : " + millisUntilFinished / 1000 + " s");
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                resendbtn.setEnabled(true);
                resendbtn.setText("Retry");

                welcomemsg.setText(getString(R.string.pending_verification));
                completeverificationbtn.setVisibility(View.VISIBLE);
                retry_area.setVisibility(View.GONE);
            }

        };
        resendbtn.setEnabled(false);
        welcomemsg.setText(getResources().getString(R.string.almost_done));

        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;

            JSONObject request = new JSONObject();
            request.put("phonenumber", GetPhoneNumberActivity.phonenumber);
            request.put("apphash", APP_HASH);
            request.put("role", "student");
            request.put("osversion", System.getProperty("os.version"));
            request.put("sdkversion", android.os.Build.VERSION.SDK_INT);
            request.put("device", android.os.Build.DEVICE);
            request.put("devicemodel", android.os.Build.MODEL);
            request.put("deviceproduct", android.os.Build.PRODUCT);
            request.put("manufacturer", android.os.Build.MANUFACTURER);
            request.put("androidid", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            request.put("versionrelease", Build.VERSION.RELEASE);
            request.put("deviceheight", String.valueOf(heightPixels));
            request.put("devicewidth", String.valueOf(widthPixels));

            JsonObjectRequest jsonOblect = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "create-user",
                    request,
                    response -> {
                        dialog.dismiss();
                        if (response != null) {
                            if (response.has("registered_as_instructor")) {
                                welcomemsg.setText(getResources().getString(R.string.number_cannot_be_used));
                                resendbtn.setVisibility(View.GONE);
                            } else {
                                getRoom();
                            }
                        }
                    }, error -> {
                error.printStackTrace();
                Log.d("Cyrilll", error.toString());
                dialog.dismiss();
                myVolleyError(getAuthActivityContext, error);
                stoptimer();
            });
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);

        } catch (Exception e) {
            e.printStackTrace();
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
            placeCall(code);
            return;
        }
        changeDefaultDialer(this, "");
    }

    private void checkSetDefaultDialerResult(int resultCode) {
        String message;

        switch (resultCode) {
            case RESULT_OK:
                message = "기본 전화 앱으로 설정하였습니다.";
//                mProgress.setTitle("Joining class...");
//                mProgress.show();
//                getAvailableRoom(getApplicationContext());
                placeCall(code);
                break;
            case RESULT_CANCELED:
                message = "기본 전화 앱으로 설정하지 않았습니다.";
                break;
            default:
                message = "Unexpected result code " + resultCode;
        }
    }
}
