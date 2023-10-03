package com.cropestate.fielduser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.EnrolmentActivityAdapter;
import com.cropestate.fielduser.other.InitApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class EnrolmentActivity extends AppCompatActivity {
    EnrolmentActivityAdapter enrolmentActivityAdapter;
    String coursepath;
    RecyclerView recyclerview_instructors;
    TextView title, noinstructorstext, nocoursestest;
    ImageView loadinggif, backbtn;
    ArrayList<JSONObject> instructorCourses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolment);

        Intent intent = getIntent();
        coursepath = intent.getStringExtra("coursepath");

        instructorCourses = new ArrayList<>();

        backbtn = findViewById(R.id.search);
        title = findViewById(R.id.title);
        noinstructorstext = findViewById(R.id.noinstructorstext);
        nocoursestest = findViewById(R.id.nocoursestest);
        loadinggif = findViewById(R.id.loadinggif);
        recyclerview_instructors = findViewById(R.id.recyclerview);
        title = findViewById(R.id.title);

        Glide.with(getApplicationContext()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(loadinggif);

        title.setText(coursepath);

        backbtn.setOnClickListener(v -> finish());

        populateInstructors();
        enrolmentActivityAdapter = new EnrolmentActivityAdapter(instructorCourses, EnrolmentActivity.this);

        recyclerview_instructors.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_instructors.setHasFixedSize(true);
        recyclerview_instructors.setNestedScrollingEnabled(false);
        recyclerview_instructors.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerview_instructors.getContext(),
                new LinearLayoutManager(this).getOrientation());
        recyclerview_instructors.addItemDecoration(dividerItemDecoration);

        recyclerview_instructors.setAdapter(enrolmentActivityAdapter);

    }

    private void populateInstructors() {
        try {
            loadinggif.setVisibility(View.VISIBLE);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "course-instructors",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            loadinggif.setVisibility(View.GONE);
//                            retry_layout.setVisibility(View.GONE);
                            if (response != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.has("course_exists")) {
                                        nocoursestest.setVisibility(View.VISIBLE);
                                    } else if (jsonObject.has("course_instructors")) {
                                        JSONArray jsonArray = jsonObject.getJSONArray("course_instructors");
                                        int length = jsonArray.length();
                                        if (length < 1) {
                                            noinstructorstext.setVisibility(View.VISIBLE);
                                        }
                                        instructorCourses.clear();
                                        for (int i = 0; i < length; i++) {
                                            try {
                                                instructorCourses.add(jsonArray.getJSONObject(i));

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadinggif.setVisibility(View.GONE);
//                            retry_layout.setVisibility(View.VISIBLE);
                            myVolleyError(EnrolmentActivity.this.getApplicationContext(), error);
                        }
                    }
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    String level = coursepath.split((" >> "))[0];
                    String level_en = "";
                    if (level.equals(getApplicationContext().getResources().getString(R.string.preschool))) {
                        level_en = "Pre-School";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.primary_school))) {
                        level_en = "Primary School";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.jhs))) {
                        level_en = "JHS";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.shs))) {
                        level_en = "SHS";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.preuniversity))) {
                        level_en = "Pre-University";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.university))) {
                        level_en = "University";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.professional))) {
                        level_en = "Professional";
                    } else if (level.equals(getApplicationContext().getResources().getString(R.string.vocational))) {
                        level_en = "Vocational";
                    }
                    
                    Map<String,String> params = new HashMap<>();
                    params.put("coursepath", coursepath.replaceFirst(level, level_en));
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
