package com.cropestate.fielduser.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.ClassroomActivity;
import com.cropestate.fielduser.activity.PhoneActivity;
import com.cropestate.fielduser.materialDialog.RatingMaterialDialog;
import com.cropestate.fielduser.materialDialog.SubscriptionMaterialDialog;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmEnrolment;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.ACTIVE;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.ALL;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.EXPIRED;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.LISTTYPE;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.LIVE;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.UPCOMING;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.activeClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.allClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.enrolmentFragmentAdapter;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.expiredClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.liveClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.menu;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.populateActive;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.populateAllClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.populateExpired;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.populateLiveClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.populateUpcomingClasses;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.refresh;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.upcomingClasses;

public class EnrolmentFragmentAdapter extends RecyclerView.Adapter<EnrolmentFragmentAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmEnrolment> enrolments;
    Activity mContext;
    private ProgressDialog mProgress;

    public EnrolmentFragmentAdapter(ArrayList<RealmEnrolment> enrolments, Activity mContext) {
        this.enrolments = enrolments;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_enrolment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RealmEnrolment enrolment = enrolments.get(position);

        String level_en = enrolment.getCoursepath().split((" >> "))[0];
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

        holder.coursename.setText(enrolment.getCoursepath().replaceFirst(level_en, level));
        holder.intructorname.setText(enrolment.getInstructorname().replace("null", ""));
        holder.rating.setText(String.valueOf(enrolment.getRating()));
        String rating_text = "(" + enrolment.getTotalrating() + " " + mContext.getString(R.string.rating);
        if (enrolment.getTotalrating() == 1) {
            rating_text += ")";
        } else {
            rating_text += "s)";
        }
        holder.totalrating.setText(rating_text);
        holder.ratingbar.setRating(enrolment.getRating());
        holder.time.setText(enrolment.getTime());
        holder.price.setText(enrolment.getCurrency() + enrolment.getPrice());
        holder.expirydate.setText(enrolment.getCurrency() + enrolment.getSubsriptionexpirydate());

        if (enrolment.getPrice().equals("0.00") || !enrolment.isActivelysubscribed()) {
            holder.expirylayout.setVisibility(View.GONE);
        }
        else {
            holder.expirylayout.setVisibility(View.VISIBLE);
            holder.expirydate.setText(enrolment.getSubsriptionexpirydate());
        }

        if (enrolment.isActivelysubscribed()|| enrolment.getPrice().equals("0.00")) {
            holder.renewsubscriptionbtn.setVisibility(View.GONE);
            holder.enterclass.setVisibility(View.VISIBLE);
        } else {
            holder.renewsubscriptionbtn.setVisibility(View.VISIBLE);
            holder.enterclass.setVisibility(View.GONE);
            if (enrolment.hassubsribedbefore()) {
                holder.renewsubscriptionbtn.setText(mContext.getString(R.string.renew_subscription));
            } else {
                holder.renewsubscriptionbtn.setText(mContext.getString(R.string.subscribe));
            }
        }
//        holder.statustext.setText(enrolment.getPercentagecompleted() + "% complete");
//        holder.statusmessage.setText(mContext.getString(R.string.completed));
//        holder.progressbar.setProgress(enrolment.getPercentagecompleted());
        holder.enterclass.setOnClickListener(v -> {
            Intent i = new Intent(mContext, PhoneActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", enrolment.getInstructorcourseid())
                    .putExtra("COURSEPATH", enrolment.getCoursepath())
                    .putExtra("ENROLMENTID", enrolment.getEnrolmentid())
                    .putExtra("INSTRUCTORNAME", enrolment.getInstructorname());

            if (enrolment.isLive()) {
                i.putExtra("ISLIVE", true);
            }
            if (enrolment.isUpcoming()) {
                i.putExtra("ISUPCOMING", true);
            }
            mContext.startActivity(i);
        });
        holder.renewsubscriptionbtn.setOnClickListener(v -> {
            SubscriptionMaterialDialog subscriptionMaterialDialog = new SubscriptionMaterialDialog();
            if(subscriptionMaterialDialog != null && subscriptionMaterialDialog.isAdded()) {

            } else {
                subscriptionMaterialDialog.setCurrency(enrolment.getCurrency());
                subscriptionMaterialDialog.setAmount(Double.valueOf(enrolment.getPrice()));
                subscriptionMaterialDialog.setDescription(enrolment.getCurrency() + enrolment.getPrice() + " " + mContext.getString(R.string.per_week));
                subscriptionMaterialDialog.setEnrolmentid(enrolment.getEnrolmentid());
                subscriptionMaterialDialog.show(mContext.getFragmentManager(), "SubscriptionMaterialDialog");
            }
        });
        holder.more_details.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(mContext, holder.more_details);

            popup.inflate(R.menu.enrolment_menu);

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.ratecourse) {
                    RatingMaterialDialog ratingMaterialDialog = new RatingMaterialDialog();
                    if (ratingMaterialDialog != null && ratingMaterialDialog.isAdded()) {

                    } else {
                        ratingMaterialDialog.setInstructorcourseid(enrolment.getInstructorcourseid());
                        ratingMaterialDialog.setCoursepath(enrolment.getCoursepath());
                        ratingMaterialDialog.setProfilepicurl(enrolment.getProfilepicurl());
                        ratingMaterialDialog.setInstructorname(enrolment.getInstructorname());

                        ratingMaterialDialog.show(mContext.getFragmentManager(), "RatingMaterialDialog");
                    }
                    return true;
                } else if (itemId == R.id.unenroll) {
                    try {
                        JSONObject request = new JSONObject();

                        request.put("enrolled", 0);
                        mProgress = new ProgressDialog(mContext);
                        mProgress.setTitle(mContext.getString(R.string.unenrolling));
                        mProgress.setMessage(mContext.getString(R.string.pls_wait));
                        mProgress.setCancelable(false);
                        mProgress.setIndeterminate(true);
                        mProgress.show();
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.PATCH,
                                API_URL + "enrolments/" + enrolment.getEnrolmentid(),
                                request,
                                response -> {
                                    mProgress.dismiss();
                                    if (response != null) {
                                        Toast.makeText(mContext, mContext.getString(R.string.successfully_unenrolled), Toast.LENGTH_LONG).show();
                                        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                            realm.createOrUpdateObjectFromJson(RealmEnrolment.class, response);
                                            RealmInstructorCourse realmInstructorCourse = realm.where(RealmInstructorCourse.class).equalTo("instructorcourseid", enrolment.getInstructorcourseid()).findFirst();
//                                                String confirmation_token = realm.where(RealmUser.class).equalTo("userid", realmInstructorCourse.getInstructorid()).findFirst().getConfirmation_token();
//                                                new EnrolmentActivityAdapter.notifyInstructor(mContext, "unenrolment", confirmation_token, enrolment.getCoursepath()).execute();
                                        });
                                        enrolmentFragmentAdapter.enrolments.clear();
                                        switch (LISTTYPE) {
                                            case ALL:
                                                populateAllClasses();
                                                enrolmentFragmentAdapter.enrolments.addAll(allClasses);
                                                break;
                                            case UPCOMING:
                                                populateUpcomingClasses();
                                                enrolmentFragmentAdapter.enrolments.addAll(upcomingClasses);
                                                break;
                                            case LIVE:
                                                populateLiveClasses();
                                                enrolmentFragmentAdapter.enrolments.addAll(liveClasses);
                                                break;
                                            case EXPIRED:
                                                populateExpired();
                                                enrolmentFragmentAdapter.enrolments.addAll(expiredClasses);
                                                break;
                                            case ACTIVE:
                                                populateActive();
                                                enrolmentFragmentAdapter.enrolments.addAll(activeClasses);
                                                break;
                                        }
                                        notifyDataSetChanged();
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(enrolment.getInstructorcourseid())
                                                .addOnCompleteListener(task -> {
                                                    String msg = "unsubscription successful";
                                                    if (!task.isSuccessful()) {
                                                        msg = "unsubscription unccessful";
                                                    }
                                                    Log.d("engineer:sub_status:", msg);
                                                });
                                    }
                                },
                                error -> {
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
                    return true;
                }
                return false;
            });

            MenuItem rateCourseItem = popup.getMenu().findItem(R.id.ratecourse);
            if (enrolment.isRatedbyme()) {
                rateCourseItem.setVisible(false);
            } else {
                rateCourseItem.setVisible(true);
            }

            popup.show();
        });

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("ENROLMENT_FRAG_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.more_details.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.more_details.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.more_details.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder(mContext).setPoint(refresh)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.refresh_payment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder(mContext).setPoint(menu)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.filter_courses_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget thirdTarget = new SimpleTarget.Builder(mContext).setPoint(holder.more_details)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.unenrol_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with(mContext)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget, thirdTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightEndedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                                    .edit()
                                    .putBoolean("ENROLMENT_FRAG_TIPS_DISMISSED", true)
                                    .apply())
                            .start();

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return enrolments.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView time, coursename, statusmessage, price, expirydate, schoolid;
        LinearLayout expirylayout;
        Button enterclass, renewsubscriptionbtn;
        ImageView more_details;
        RatingBar ratingbar;
        TextView totalrating, rating;
        TextView intructorname;

        public ViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.time);
            ratingbar = view.findViewById(R.id.ratingbar);
            rating = view.findViewById(R.id.rating);
            totalrating = view.findViewById(R.id.totalrating);
            coursename = view.findViewById(R.id.coursename);
            statusmessage = view.findViewById(R.id.statusmessage);
            price = view.findViewById(R.id.price);
            expirylayout = view.findViewById(R.id.expirylayout);
            expirydate = view.findViewById(R.id.expirydate);
            schoolid = view.findViewById(R.id.schoolid);
            more_details = view.findViewById(R.id.more_details);
            enterclass = view.findViewById(R.id.enterclass);
            renewsubscriptionbtn = view.findViewById(R.id.renewsubscriptionbtn);
            intructorname = view.findViewById(R.id.intructorname);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), ClassroomActivity.class);
            view.getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }


    }
}

