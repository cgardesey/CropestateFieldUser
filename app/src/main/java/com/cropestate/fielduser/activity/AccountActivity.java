package com.cropestate.fielduser.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.constants.Const;
import com.cropestate.fielduser.fragment.AccountFragment1;
import com.cropestate.fielduser.other.MyHttpEntity;
import com.cropestate.fielduser.pagerAdapter.AccountPageAdapter;
import com.cropestate.fielduser.realm.RealmStudent;
import com.cropestate.fielduser.util.RealmUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.isNetworkAvailable;
import static com.cropestate.fielduser.fragment.AccountFragment1.emailaddress;
import static com.cropestate.fielduser.fragment.AccountFragment1.firstName;
import static com.cropestate.fielduser.fragment.AccountFragment1.gender;
import static com.cropestate.fielduser.fragment.AccountFragment1.lastName;
import static com.cropestate.fielduser.fragment.AccountFragment1.otherNames;
import static com.cropestate.fielduser.fragment.AccountFragment1.profile_pic_file;
import static com.cropestate.fielduser.fragment.AccountFragment1.title;

@SuppressWarnings("HardCodedStringLiteral")
public class AccountActivity extends PermisoActivity {

    public static RealmStudent realmStudent = new RealmStudent();
    static Context context;


    boolean close = false;
    ViewPager mViewPager;
    AccountPageAdapter accountPageAdapter;
    FloatingActionButton fab;

    RelativeLayout rootview;
    ProgressBar progressBar;
    String tag1 = "android:switcher:" + R.id.pageques + ":" + 0;
//    String tag2 = "android:switcher:" + R.id.pageques + ":" + 1;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        Permiso.getInstance().setActivity(this);

        setContentView(R.layout.activity_account);
        getSupportActionBar().hide();

        mProgress = new ProgressDialog(this);
        mProgress.setTitle(getString(R.string.updating_profile));
        mProgress.setMessage(getString(R.string.please_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        fab = findViewById(R.id.done);
        fab.setOnClickListener(v -> sendData());

        rootview = findViewById(R.id.root);

        progressBar = findViewById(R.id.pbar_pic);
        Realm.init(getApplicationContext());
        realmStudent = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmStudent.class).equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(this).getString(MYUSERID, "")).findFirst();
        accountPageAdapter = new AccountPageAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pageques);
        mViewPager.setAdapter(accountPageAdapter);
//        mViewPager.setOffscreenPageLimit(1);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void sendData() {

        final AccountFragment1 tabFrag1 = (AccountFragment1) getSupportFragmentManager().findFragmentByTag(tag1);
//        final AccountFragment2 tabFrag2 = (AccountFragment2) getSupportFragmentManager().findFragmentByTag(tag2);

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            if (tabFrag1 != null) {

                if (tabFrag1.validate()) {
                    if (isNetworkAvailable(AccountActivity.this)) {
                        new updateStudentaAsync(getApplicationContext()).execute();
                    } else {
                        Toast.makeText(AccountActivity.this, getString(R.string.internet_connection_is_needed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountActivity.this, getString(R.string.pls_correct_the_errors), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showTwoButtonSnackbar() {

        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final Snackbar snackbar = Snackbar.make(rootview, "Exit?", Snackbar.LENGTH_INDEFINITE);

        // Get the Snackbar layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        // Inflate our courseListMaterialDialog viewBitmap bitmap = ((RoundedDrawable)profilePic.getDrawable()).getSourceBitmap();
        View snackView = getLayoutInflater().inflate(R.layout.snackbar, null);


        TextView textViewOne = snackView.findViewById(R.id.first_text_view);
        textViewOne.setText(this.getResources().getString(R.string.yes));
        textViewOne.setOnClickListener(v -> {
            snackbar.dismiss();
            close = true;
            AccountActivity.this.onBackPressed();

            //  finish();
        });

        final TextView textViewTwo = snackView.findViewById(R.id.second_text_view);

        textViewTwo.setText(this.getResources().getString(R.string.no));
        textViewTwo.setOnClickListener(v -> {
            Log.d("Deny", "showTwoButtonSnackbar() : deny clicked");
            snackbar.dismiss();


        });

        // Add our courseListMaterialDialog view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);

        // Show the Snackbar
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (close) {
            super.onBackPressed();
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
        showTwoButtonSnackbar();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private class updateStudentaAsync extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;

        private updateStudentaAsync(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String infoid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, "");

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = API_URL + "students/" + infoid;
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                if (profile_pic_file != null) {
                    multipartEntityBuilder.addPart("picture", new FileBody(profile_pic_file));
                }
                multipartEntityBuilder.addTextBody("title", title.getSelectedItem().toString());
                String firstname = firstName.getText().toString();
                multipartEntityBuilder.addTextBody("firstname", firstname);
                multipartEntityBuilder.addTextBody("lastname", lastName.getText().toString());
                multipartEntityBuilder.addTextBody("othername", otherNames.getText().toString());
                multipartEntityBuilder.addTextBody("gender", gender.getSelectedItem().toString());
                multipartEntityBuilder.addTextBody("emailaddress", emailaddress.getText().toString());
//                multipartEntityBuilder.addTextBody("highestedulevel", eduLevel.getSelectedItem().toString());
//                multipartEntityBuilder.addTextBody("highesteduinstitutionname", nameofInst.getText().toString().trim());

                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                responseString = e.getMessage();
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
            } catch (IOException e) {
                responseString = e.getMessage();
                Log.e("gardes", e.toString());
//                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.contains("connect")){
                    Toast.makeText(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmStudent.class, jsonObject);
                            Const.showToast(getApplicationContext(), context.getString(R.string.successfully_updated));
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        });
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            /*progressbar.setProgress(progress[0]);
            statustext.setText(progress[0].toString() + "%  complete");*/
        }
    }
}
