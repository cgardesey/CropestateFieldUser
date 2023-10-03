package com.cropestate.fielduser.activity;

        import android.content.Context;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.StringRequest;
        import com.bumptech.glide.Glide;
        import com.bumptech.glide.request.RequestOptions;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.adapter.ListAdapter;
        import com.cropestate.fielduser.other.InitApplication;

        import org.json.JSONArray;
        import org.json.JSONException;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class MyListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView loadinggif;
    ImageView backbtn;
    Button retrybtn;
    LinearLayout retry_layout;
    TextView titleTextView, text;
    ArrayList<String> newList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    ListAdapter listAdapter;
    String title = "";
    Context mContext;
    ArrayList<String> courses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        mContext = getApplicationContext();
        loadinggif = findViewById(R.id.loadinggif);
        retry_layout = findViewById(R.id.retry_layout);
        retrybtn = findViewById(R.id.retrybtn);
        Glide.with(getApplicationContext()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(loadinggif);
        text = findViewById(R.id.text);
        recyclerView = findViewById(R.id.recyclerView);
        titleTextView = findViewById(R.id.title);
        backbtn = findViewById(R.id.search);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        title = getIntent().getStringExtra("title");
        titleTextView.setText(title);

        courses.clear();
        populateNames(title.concat(" ").concat(">>").concat(" "));
        backbtn.setOnClickListener(v -> finish());
        retrybtn.setOnClickListener(v -> populateNames(title.concat(" ").concat(">>").concat(" ")));
    }


    private void populateNames(String search) {

        try {
            loadinggif.setVisibility(View.VISIBLE);
            StringRequest jsonArrayRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "sub-courses",
                    response -> {
                        loadinggif.setVisibility(View.GONE);
                        retry_layout.setVisibility(View.GONE);
                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                int length = jsonArray.length();
                                if (length == 0) {
                                    startActivity(new Intent(mContext, EnrolmentActivity.class).putExtra("coursepath", title));
                                    finish();
                                }
                                courses.clear();
                                for (int i = 0; i < length; i++) {
                                    try {
                                        courses.add(jsonArray.getString(i));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        listAdapter = new ListAdapter((names, position, holder) -> {
                            String textViewText = names.get(position);
                            startActivity(new Intent(mContext, MyListActivity.class).putExtra("title", title.concat(" ").concat(">>").concat(" ") + textViewText));
                        }, MyListActivity.this, courses, title);

                        recyclerView.setAdapter(listAdapter);
                    },
                    error -> {
                        loadinggif.setVisibility(View.GONE);
                        retry_layout.setVisibility(View.VISIBLE);
                        myVolleyError(mContext, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {

                    String level = search.split((" >> "))[0];
                    String level_en = "";
                    if (level.equals(mContext.getResources().getString(R.string.preschool))) {
                        level_en = "Pre-School";
                    } else if (level.equals(mContext.getResources().getString(R.string.primary_school))) {
                        level_en = "Primary School";
                    } else if (level.equals(mContext.getResources().getString(R.string.jhs))) {
                        level_en = "JHS";
                    } else if (level.equals(mContext.getResources().getString(R.string.shs))) {
                        level_en = "SHS";
                    } else if (level.equals(mContext.getResources().getString(R.string.preuniversity))) {
                        level_en = "Pre-University";
                    } else if (level.equals(mContext.getResources().getString(R.string.university))) {
                        level_en = "University";
                    } else if (level.equals(mContext.getResources().getString(R.string.professional))) {
                        level_en = "Professional";
                    } else if (level.equals(mContext.getResources().getString(R.string.vocational))) {
                        level_en = "Vocational";
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("search", search.replaceFirst(level, level_en));
                    return params;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }
}
