package com.cropestate.fielduser.activity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.constants.keyConst;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.util.RealmUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import static com.cropestate.fielduser.activity.HomeActivity.persistAll;
import static com.cropestate.fielduser.constants.Const.myVolleyError;


public class SigninActivity extends Activity {

    public static final String AGENTID = "agentid";

    public static String APITOKEN = "APITOKEN";
    public static String MYUSERID = "MYUSERID";

    static final String USER_NOT_FOUND = "0";
    static final String SUCCESS = "1";
    static final String INCORRECT_PASSWORD = "2";
    private EditText emailField, passwordField;
    ImageView passwordIcon;
    private Button button;
    private ProgressDialog mProgress;
    boolean passwordShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Processing...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        emailField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        passwordIcon = findViewById(R.id.passwordIcon);

        button = findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
//        boolean signedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(userType, false);
        if (false) {
            onSignInInit();
        }
        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordShow = !passwordShow;
                if (passwordShow) {
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hide_password);
                    passwordIcon.setImageBitmap(bitmap);
                }
                else {
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.show_password);
                    passwordIcon.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void onSignInInit() {
        startActivity(new Intent(SigninActivity.this, HomeActivity.class));
        finish();
    }

    public void login() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        boolean canLogin = true;

        if (!isValidEmail(email)){
            emailField.setError("Invalid email!");
            canLogin = false;
        }
        else {
            emailField.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            canLogin = false;
        } else {
            passwordField.setError(null);
        }

        if (canLogin) {
            signin(email, password);
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void signin(String email, String password) {
        try {
            mProgress.setTitle("Signing in...");
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    keyConst.API_URL + "login",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.has("user_not_found")) {
                                    Toast.makeText(getApplicationContext(), "User not found!", Toast.LENGTH_SHORT).show();
                                }
                                else if (jsonObject.has("email_not_verified")) {
                                    Toast.makeText(getApplicationContext(), "Email not verified!", Toast.LENGTH_SHORT).show();
                                }
                                else if (jsonObject.has("incorrect_password")) {
                                    Toast.makeText(getApplicationContext(), "Incorrect password!", Toast.LENGTH_SHORT).show();
                                }
                                else if (jsonObject.has("userid")) {
                                    Toast.makeText(getApplicationContext(), "Successfully signed in!", Toast.LENGTH_SHORT).show();
                                    Realm.init(getApplicationContext());
                                    Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                        try {
                                            persistAll(realm, jsonObject);

                                            PreferenceManager
                                                    .getDefaultSharedPreferences(getApplicationContext())
                                                    .edit()
                                                    .putString(MYUSERID, jsonObject.getString("userid"))
                                                    .putString(APITOKEN, jsonObject.getString("api_token"))
                                                    .apply();

                                            mProgress.dismiss();
                                            startActivity(new Intent(getApplicationContext(), LeasesActivity.class));
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
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
                    params.put("email", email);
                    params.put("password", password);
                    return params;
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
}