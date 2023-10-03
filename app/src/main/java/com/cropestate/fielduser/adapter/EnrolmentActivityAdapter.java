package com.cropestate.fielduser.adapter;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.ViewTreeObserver;
        import android.view.animation.DecelerateInterpolator;
        import android.widget.Button;
        import android.widget.Filter;
        import android.widget.Filterable;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.RatingBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.appcompat.app.AlertDialog;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonObjectRequest;
        import com.android.volley.toolbox.StringRequest;
        import com.bumptech.glide.Glide;
        import com.bumptech.glide.request.RequestOptions;
        import com.makeramen.roundedimageview.RoundedImageView;
        import com.takusemba.spotlight.SimpleTarget;
        import com.takusemba.spotlight.Spotlight;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.activity.HomeActivity;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.realm.RealmAudio;
        import com.cropestate.fielduser.realm.RealmCourse;
        import com.cropestate.fielduser.realm.RealmEnrolment;
        import com.cropestate.fielduser.realm.RealmInstructor;
        import com.cropestate.fielduser.realm.RealmInstructorCourse;
        import com.cropestate.fielduser.realm.RealmStudent;
        import com.cropestate.fielduser.realm.RealmTimetable;
        import com.cropestate.fielduser.realm.RealmUser;
        import com.cropestate.fielduser.util.RealmUtility;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;

        import io.realm.Realm;
        import okhttp3.MediaType;
        import okhttp3.OkHttpClient;
        import okhttp3.RequestBody;

        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
        import static com.cropestate.fielduser.activity.HomeActivity.JUSTENROLLED;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.keyConst.FCM_MESSAGE_URL;
        import static com.cropestate.fielduser.constants.keyConst.SERVER_URL;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;

public class EnrolmentActivityAdapter extends RecyclerView.Adapter<EnrolmentActivityAdapter.ViewHolder> implements Filterable {
    ArrayList<JSONObject> courseInstructors;
    private Activity mContext;

    ProgressDialog mProgress;

    String coursepath, confirmation_token;

    public EnrolmentActivityAdapter(ArrayList<JSONObject> instructoeCourses, Activity mContext) {
        this.courseInstructors = instructoeCourses;
        this.mContext = mContext;

        mProgress = new ProgressDialog(mContext);
        mProgress.setMessage(mContext.getString(R.string.pls_wait));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_instructor, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final JSONObject courseInstructorJson = courseInstructors.get(position);
        try {
            holder.edubackground.setText(courseInstructorJson.getJSONObject("instructor").getString("edubackground").replace("null", ""));
            holder.about.setText(courseInstructorJson.getJSONObject("instructor").getString("about").replace("null", ""));
            holder.coursedescription.setText(courseInstructorJson.getJSONObject("course").getString("description").replace("null", ""));
            holder.rating.setText(String.valueOf(courseInstructorJson.getDouble("rating")));
            if (courseInstructorJson.getDouble("rating") == 0.0) {
                holder.rating.setVisibility(View.GONE);
            } else {
                holder.rating.setVisibility(View.VISIBLE);
            }
            int total_ratings = courseInstructorJson.getInt("total_ratings");
            String rating_text = "(";
            if (total_ratings == 0) {
                rating_text += mContext.getString(R.string.no_ratings_available) + ")";
            } else if (total_ratings == 1) {
                rating_text += total_ratings + " " + mContext.getString(R.string.rating) + ")";
            } else {
                rating_text += total_ratings + " " + mContext.getString(R.string.rating) + "s)";
            }
            holder.totalrating.setText(rating_text);
            holder.ratingbar.setRating((float) courseInstructorJson.getDouble("rating"));
            String instructorName = (courseInstructorJson.getJSONObject("instructor").getString("title") + " " + courseInstructorJson.getJSONObject("instructor").getString("firstname") + " " + courseInstructorJson.getJSONObject("instructor").getString("othername") + " " + courseInstructorJson.getJSONObject("instructor").getString("lastname")).replace("null", "");

            holder.instructorname.setText(instructorName);
            Glide.with(mContext).load(courseInstructorJson.getJSONObject("instructor").get("profilepicurl")).apply(new RequestOptions().centerCrop()).into(holder.profilepic);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.enrol.setOnClickListener(v -> {
            try {
                if (courseInstructorJson.getString("institutionid").replace("null", "").trim().equals("")) {
                    JSONObject request = new JSONObject();
                    request.put("studentid", PreferenceManager.getDefaultSharedPreferences(mContext).getString(MYUSERID, ""));
                    request.put("instructorcourseid", courseInstructorJson.getString("instructorcourseid"));
                    mProgress.setTitle(mContext.getString(R.string.enrolling));
                    enrol(request);
                } else {
                    new AlertDialog.Builder(mContext)
                            .setTitle(mContext.getString(R.string.enrolment_request))
                            .setMessage(mContext.getString(R.string.course_belongs_to_an_institution))

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(mContext.getString(R.string.yes), (dialog, which) -> {
                                try {
                                    JSONObject request = new JSONObject();
                                    request.put("studentid", PreferenceManager.getDefaultSharedPreferences(mContext).getString(MYUSERID, ""));
                                    request.put("instructorcourseid", courseInstructorJson.getString("instructorcourseid"));
                                    request.put("request_enrolment", true);
                                    mProgress.setTitle(mContext.getString(R.string.requesting_enrolment));
                                    enrol(request);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            })
                            .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("ENROLMENT_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.downbtn.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.downbtn.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.downbtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder(mContext).setPoint(holder.downbtn)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.enrol_tip)  + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with(mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightEndedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                                    .edit()
                                    .putBoolean("ENROLMENT_ACTIVITY_TIPS_DISMISSED", true)
                                    .apply())
                            .start();

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return courseInstructors.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<JSONObject> courseInstructors) {
        this.courseInstructors = courseInstructors;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView instructorname, edubackground, about, coursedescription;
        RatingBar ratingbar;
        TextView totalrating, rating;
        LinearLayout details;
        ImageView downbtn;
        RoundedImageView profilepic;
        Button enrol;

        public ViewHolder(View view) {
            super(view);
            instructorname = view.findViewById(R.id.instructorname);
            enrol = view.findViewById(R.id.enrol);
            ratingbar = view.findViewById(R.id.ratingbar);
            rating = view.findViewById(R.id.rating);
            totalrating = view.findViewById(R.id.totalrating);
            coursedescription = view.findViewById(R.id.coursedescription);
            about = view.findViewById(R.id.about);
            edubackground = view.findViewById(R.id.edubackground);
            details = view.findViewById(R.id.details);
            downbtn = view.findViewById(R.id.upbtn);
            profilepic = view.findViewById(R.id.profilepic);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            if (details.getVisibility() == View.VISIBLE) {
                details.setVisibility(View.GONE);
                downbtn.animate().rotation(360).start();
            } else {
                details.setVisibility(View.VISIBLE);
                downbtn.animate().rotation(-180).start();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }

    }

    public void enrol(JSONObject request) {
        try {
            mProgress.show();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "enrolments",
                    request,
                    response -> {
                        if (response != null) {
                            Realm.init(mContext);
                            if (response.has("already_enrolled")) {
                                mProgress.dismiss();
                                Toast.makeText(mContext, mContext.getString(R.string.already_enrolled), Toast.LENGTH_LONG).show();
                            }
                            else if (response.has("request_sent")) {
                                new AlertDialog.Builder(mContext)
                                        .setTitle(mContext.getString(R.string.success))
                                        .setMessage(mContext.getResources().getString(R.string.enrolment_request_successfully_sent))

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
                                            Intent i = new Intent(mContext, HomeActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            mContext.startActivity(i);
                                        })
                                        .show();
                            }
                            else if (response.has("pending_approval")) {
                                mProgress.dismiss();
                                new AlertDialog.Builder(mContext)
//                                                .setTitle("Message")
                                        .setMessage(mContext.getString(R.string.still_pending_approval))

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(mContext.getString(R.string.ok), null)
                                        .show();
                            }
                            else {
                                Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                    try {
                                        realm.createOrUpdateObjectFromJson(RealmInstructorCourse.class, response.getJSONObject("instructorCourse"));
                                        realm.createOrUpdateObjectFromJson(RealmEnrolment.class, response.getJSONObject("enrolment"));
                                        RealmCourse course = realm.createOrUpdateObjectFromJson(RealmCourse.class, response.getJSONObject("course"));
                                        coursepath = course.getCoursepath();
                                        realm.createOrUpdateObjectFromJson(RealmUser.class, response.getJSONObject("user"));
                                        RealmInstructor realmInstructor = realm.createOrUpdateObjectFromJson(RealmInstructor.class, response.getJSONObject("instructor"));
                                        confirmation_token = realmInstructor.getConfirmation_token();
                                        realm.createOrUpdateAllFromJson(RealmTimetable.class, response.getJSONArray("timetables"));
                                        realm.createOrUpdateAllFromJson(RealmAudio.class, response.getJSONArray("audios"));
                                        mProgress.dismiss();

                                        PreferenceManager
                                                .getDefaultSharedPreferences(mContext)
                                                .edit()
                                                .putBoolean(JUSTENROLLED, true)
                                                .apply();
                                        Toast.makeText(mContext, mContext.getString(R.string.successfully_enrolled), Toast.LENGTH_LONG).show();
                                        new notifyInstructor(mContext,"enrolment", confirmation_token, coursepath, "").execute();
                                        Intent i = new Intent(mContext, HomeActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        mContext.startActivity(i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        mProgress.dismiss();
                        myVolleyError(mContext, error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(APITOKEN, ""));
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

    public void requestEnrolment(String instructorcourseid) {
        mProgress.setTitle(mContext.getString(R.string.requesting_enrolment));
        mProgress.show();
        try {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "enrolment-requests",
                    stringResponse -> {
                        try {
                            mProgress.dismiss();
                            JSONObject response = new JSONObject(stringResponse);
                            if (response.has("already_enrolled") && response.getInt("already_enrolled") == 1) {
                                mProgress.dismiss();
                                Toast.makeText(mContext, mContext.getString(R.string.already_enrolled), Toast.LENGTH_LONG).show();
                            }
                            else if (response.has("pending_approval") && response.getInt("pending_approval") == 1) {
                                mProgress.dismiss();
                                Toast.makeText(mContext, mContext.getString(R.string.your_enrolment_is_pending_approval), Toast.LENGTH_LONG).show();
                            }
                            else if (response.has("successful") && response.getInt("successful") == 1) {
                                new AlertDialog.Builder(mContext)
                                        .setTitle(mContext.getString(R.string.success))
                                        .setMessage(mContext.getString(R.string.enrolment_request_successfully_sent))

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialo    g is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(mContext.getResources().getString(R.string.ok), (dialog, which) -> {
                                            mProgress.dismiss();
                                            PreferenceManager
                                                    .getDefaultSharedPreferences(mContext)
                                                    .edit()
                                                    .putBoolean(JUSTENROLLED, true)
                                                    .apply();
                                            Toast.makeText(mContext, mContext.getString(R.string.successfully_enrolled), Toast.LENGTH_LONG).show();
                                            new notifyInstructor(mContext, "enrolment", confirmation_token, coursepath, "").execute();
                                            Intent i = new Intent(mContext, HomeActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            mContext.startActivity(i);
                                        })
                                        .show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        mProgress.dismiss();
                        myVolleyError(mContext, error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("studentid", PreferenceManager.getDefaultSharedPreferences(mContext).getString(MYUSERID, ""));
                    params.put("instructorcourseid", instructorcourseid);
                    return params;
                }
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(APITOKEN, ""));
                    return headers;
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

    public static class notifyInstructor extends AsyncTask<Void, Integer, String> {

        Context context;
        String type, confirmation_token, coursepath, sumbission_title;

        public notifyInstructor(Context context, String type, String confirmation_token, String coursepath, String sumbission_title) {
            this.context = context;
            this.type = type;
            this.confirmation_token = confirmation_token;
            this.coursepath = coursepath;
            this.sumbission_title = sumbission_title;
        }

        @Override
        protected String doInBackground(Void... params) {

            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                RealmStudent student = Realm.getInstance(RealmUtility.getDefaultConfig())
                        .where(RealmStudent.class)
                        .equalTo("infoid", PreferenceManager.getDefaultSharedPreferences(context).getString(MYUSERID, ""))
                        .findFirst();

                String title = "", body = "";
                String studentname = student.getTitle() + " " + student.getFirstname() + " " + student.getOthername() + student.getLastname();
                switch (type)
                {
                    case "assignment":
                        title = "Assignment Submission";
                        body = studentname + " " + "has submitted" + " " + "\'" + sumbission_title + "\'" + "assignment";
                        break;
                    case "quiz":
                        title = "Quiz Submission";
                        body = studentname + " " + "has submitted" + " " + "\'" + sumbission_title + "\'" + "quiz";
                        break;
                }

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject().put(
                            "message", new JSONObject()
                                    .put("notification", new JSONObject()
                                            .put("body", body)
                                            .put("title", title)
                                    )
                                    /*.put("data", new JSONObject()
                                            .put("type", type)
                                            .put("coursepath", coursepath)
                                            .put("sumbission_title", sumbission_title)
                                            .put("studentname", studentname)
                                    )*/
                                    .put("registration_ids", new JSONArray().put(confirmation_token))
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                String content = jsonObject.toString();
                RequestBody requestBody = RequestBody.create(mediaType, content);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(FCM_MESSAGE_URL)
                        .method("POST", requestBody)
                        .addHeader("Authorization", "key=" + SERVER_URL)
                        .build();
                okhttp3.Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String s = response.toString();
            });
            return null;
        }

        @Override
        protected void onPreExecute() {

            // Init and show dialog

        }

        @Override
        protected void onPostExecute(String result) {

            }
        }
    }



