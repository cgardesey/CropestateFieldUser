package com.cropestate.fielduser.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.cropestate.fielduser.constants.keyConst;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.AnswerAdapter;
import com.cropestate.fielduser.adapter.EnrolmentActivityAdapter;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.pojo.Question;
import com.cropestate.fielduser.realm.RealmSubmittedQuiz;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.keyConst.INSTRUCTOR_API_BASE_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;
import static com.cropestate.fielduser.fragment.SubmittedQuizzesFragment.initSubmittedQuizzes;
import static com.cropestate.fielduser.fragment.UnsubmittedQuizzesFragment.initUnsubmittedQuizzes;

public class QuizActivity extends AppCompatActivity {

    TextView quiztitle, timertext, noquestionstext;

    ImageView backbtn;
    ViewPager viewpager;
    String quizid;
    ShimmerFrameLayout shimmer_view_container;
    Button retrybtn;
    public static ArrayList<Question> questionArrayList;
    String starttime, endtime;
    long diff;
    LinearLayout error_loading;
    public static Button submitquestionlayout;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    static public ProgressDialog mProgress;
    static int correct_ans_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        noquestionstext = findViewById(R.id.noquestionstext);
        viewpager = findViewById(R.id.viewpager);
        backbtn = findViewById(R.id.search);
        timertext = findViewById(R.id.timertext);
        retrybtn = findViewById(R.id.retrybtn);
        submitquestionlayout = findViewById(R.id.submitquestionlayout);
        error_loading = findViewById(R.id.error_loading);

        shimmer_view_container = findViewById(R.id.shimmer_view_container);

        shimmer_view_container.startShimmerAnimation();

        quiztitle = findViewById(R.id.quiztitle);
        SharedPreferences prefs = getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);
        Intent intent = getIntent();
        starttime = intent.getStringExtra("startime");
        endtime = intent.getStringExtra("endtime");
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = df.parse(starttime);
            date2 = df.parse(endtime);
            diff = date2.getTime() - date1.getTime();
            starttimer();

        } catch (ParseException e) {
            e.printStackTrace();
        }


        quiztitle.setText(intent.getStringExtra("title"));
        quizid = getIntent().getStringExtra("quizid");

        checkSubscription();
        retrybtn.setOnClickListener(view -> {
            shimmer_view_container.setVisibility(View.VISIBLE);
            shimmer_view_container.startShimmerAnimation();
            checkSubscription();
        });

        submitquestionlayout.setOnClickListener(v -> submitQuiz());
        submitquestionlayout.setVisibility(View.GONE);
        backbtn.setOnClickListener(v -> finish());
    }

    private void starttimer() {

        new CountDownTimer(diff, 1000) {

            public void onTick(long millisUntilFinished) {
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String text = formatter.format(new Date(millisUntilFinished));
                timertext.setText(text);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                submitQuiz();
            }

        }.start();
    }

    public void checkSubscription() {
        try {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "check-subscription",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("eligible")) {
                                    getInstructorQuestions(quizid);
                                } else {
                                    shimmer_view_container.stopShimmerAnimation();
                                    shimmer_view_container.setVisibility(View.GONE);
                                    error_loading.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_subscription_has_expired), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        shimmer_view_container.stopShimmerAnimation();
                        shimmer_view_container.setVisibility(View.GONE);
                        error_loading.setVisibility(View.VISIBLE);
                        myVolleyError(getApplicationContext(), error);
                    }
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("enrolmentid", getIntent().getStringExtra("ENROLMENTID"));
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

    public void getInstructorQuestions(String id) {
        String URL = null;
        questionArrayList = new ArrayList<Question>();

        URL = INSTRUCTOR_API_BASE_URL + "quiz/getquestions.php?quizid=" + id;
        try {
            Log.i("bbbb", URL);
            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.GET, URL, null, response -> {

                if (response == null) {
                    return;
                }

                shimmer_view_container.stopShimmerAnimation();
                shimmer_view_container.setVisibility(View.GONE);
                error_loading.setVisibility(View.GONE);
                //mShimmerViewContainer.setVisibility(View.GONE);

                try {
                    JSONArray jsonArray = response.getJSONArray("courseinfo");
                    int length = jsonArray.length();

                    if (length > 0) {
                        noquestionstext.setVisibility(View.GONE);
                    }
                    else {
                        noquestionstext.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < length; i++) {
                        try {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                            questionArrayList.add(new Question(jsonObject.getString("questionid"),
                                    jsonObject.getString("url"), jsonObject.getString("question"),
                                    jsonObject.getString("correctans"), jsonObject.getString("optiona"),
                                    jsonObject.getString("optionb"), jsonObject.getString("optionc"),
                                    jsonObject.getString("optiond"), jsonObject.getString("optione")));


                        } catch (JSONException e) {
                            e.printStackTrace();
                            shimmer_view_container.stopShimmerAnimation();
                            shimmer_view_container.setVisibility(View.GONE);
                            error_loading.setVisibility(View.VISIBLE);
                        }
                    }
                    submitquestionlayout.setVisibility(View.VISIBLE);
                    AnswerAdapter questionsAdapter = new AnswerAdapter(getSupportFragmentManager(), getApplicationContext(), questionArrayList);
                    viewpager.setAdapter(questionsAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.i("bbbb", response.toString());


            }, error -> {
                shimmer_view_container.stopShimmerAnimation();
                shimmer_view_container.setVisibility(View.GONE);
                error_loading.setVisibility(View.VISIBLE);
                Log.i("bbbb", error.toString());

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(APITOKEN, ""));

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

    public void submitQuiz() {
        try {
            mProgress.setTitle(getString(R.string.submitting_quiz));
            mProgress.show();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    keyConst.API_URL + "submitted-quizzes",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Toast.makeText(QuizActivity.this, getString(R.string.your_score_is) + " " + jsonObject.getJSONObject("submitted_quiz").getInt("percentagescore") + "%", Toast.LENGTH_LONG).show();
                                Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                    try {
                                        realm.createOrUpdateObjectFromJson(RealmSubmittedQuiz.class, jsonObject.getJSONObject("submitted_quiz"));
                                        new EnrolmentActivityAdapter.notifyInstructor(getApplicationContext(),"quiz", jsonObject.getString("confirmation_token"), getIntent().getStringExtra("COURSEPATH"), jsonObject.getJSONObject("submitted_quiz").getString("title")).execute();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                                initUnsubmittedQuizzes();
                                initSubmittedQuizzes();
                                finish();
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
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("title", getIntent().getStringExtra("title"));
                    params.put("percentagescore", getPercentageScore());
                    params.put("quizid", getIntent().getStringExtra("quizid"));
                    params.put("studentid", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MYUSERID, ""));

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

    private String getPercentageScore() {
        correct_ans_count = 0;
        for (Question question : questionArrayList) {
            correct_ans_count += question.isCorrectans() ? 1 : 0;
        }
        int correct_ans_count = QuizActivity.correct_ans_count;
        int size = questionArrayList.size();
        float percentage_score = ((float)correct_ans_count / (float)size) * 100.0f;
        return String.valueOf(percentage_score);
    }
}
