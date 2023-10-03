package com.cropestate.fielduser.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.SearchAdapter;
import com.cropestate.fielduser.other.InitApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class SearchCourseActivity extends AppCompatActivity {
    static ArrayList<String> courses = new ArrayList<>();
    static ArrayList<String> newCourses = new ArrayList<>();
    SearchAdapter searchAdapter;
    RecyclerView recyclerview_courses;
    ImageView loadinggif, search;
    EditText searchtext;
    LinearLayout searchlayout;
    Context mContext;
    static int offset = 0;
    private String tag = "SEARCH_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_course);

        mContext = getApplicationContext();
        loadinggif = findViewById(R.id.loadinggif);
        recyclerview_courses = findViewById(R.id.recyclerview_courses);
        searchtext = findViewById(R.id.searchtext);
        search = findViewById(R.id.search);
        searchlayout = findViewById(R.id.searchlayout);

        Glide.with(getApplicationContext()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(loadinggif);

        searchAdapter = new SearchAdapter(courses);

        recyclerview_courses.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_courses.setHasFixedSize(true);
        recyclerview_courses.setNestedScrollingEnabled(false);
        recyclerview_courses.setItemAnimator(new DefaultItemAnimator());
        recyclerview_courses.setAdapter(searchAdapter);

        recyclerview_courses.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    offset += 10;
                    courses.clear();
                    searchAdapter.notifyDataSetChanged();
                    populateCourses(searchtext.getText().toString());
                }
            }
        });

        searchtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                courses.clear();
                searchAdapter.notifyDataSetChanged();
                offset = 0;
                if (!searchtext.getText().toString().equals("")) {
                    populateCourses(searchtext.getText().toString());
                }
            }
        });
    }

    private void populateCourses(String search) {
        try {
            loadinggif.setVisibility(View.VISIBLE);
            InitApplication.getInstance().mRequestQueue.cancelAll(tag);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "filtered-courses",
                    response -> {
                        loadinggif.setVisibility(View.GONE);
                        courses.clear();
                        searchAdapter.notifyDataSetChanged();
                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                if (jsonArray.length() > 0) {
                                    newCourses.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            String level_en = jsonArray.getString(i).split((" >> "))[0];
                                            String level = "";
                                            if (level_en.equals("Pre-School")) {
                                                level = mContext.getResources().getString(R.string.preschool);
                                            } else if (level_en.equals("Primary School")) {
                                                level = mContext.getResources().getString(R.string.primary_school);
                                            } else if (level_en.equals("JHS")) {
                                                level = mContext.getResources().getString(R.string.jhs);
                                            } else if (level_en.equals("SHS")) {
                                                level = mContext.getResources().getString(R.string.shs);
                                            } else if (level_en.equals("Pre-University")) {
                                                level = mContext.getResources().getString(R.string.preuniversity);
                                            } else if (level_en.equals("University")) {
                                                level = mContext.getResources().getString(R.string.university);
                                            } else if (level_en.equals("Professional")) {
                                                level = mContext.getResources().getString(R.string.professional);
                                            } else if (level_en.equals("Vocational")) {
                                                level = mContext.getResources().getString(R.string.vocational);
                                            }
                                            newCourses.add(jsonArray.getString(i).replaceFirst(level_en, level));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    courses.addAll(newCourses);
                                    searchAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        loadinggif.setVisibility(View.VISIBLE);
                        myVolleyError(mContext, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("search", search);
                    params.put("offset", String.valueOf(offset));
                    params.put("length", "10");

                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            stringRequest.setTag(tag);
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }
}
