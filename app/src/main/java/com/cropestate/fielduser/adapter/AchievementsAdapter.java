package com.cropestate.fielduser.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.realm.RealmEnrolment;

import java.util.ArrayList;
import java.util.Random;


public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmEnrolment> achievements;
    Activity mContext;
    private ProgressDialog mProgress;

    public AchievementsAdapter(ArrayList<RealmEnrolment> achievements, Activity mContext) {
        this.achievements = achievements;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_achievement, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RealmEnrolment enrolment = achievements.get(position);

//        holder.institution.setText(enrolment.getInstitution());
        holder.course.setText(enrolment.getCoursepath());
        holder.grade.setText(String.valueOf(new Random().nextInt(21) + 80));
        holder.share.setOnClickListener(view -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            // Add data to the intent, the receiving app will decide
            // what to do with it.
//                share.putExtra(Intent.EXTRA_SUBJECT, enrolment.getInstitution());
            share.putExtra(Intent.EXTRA_TEXT, "http://mycropshare.com/univirtual/public/uploads/certificates/sample_certificate.pdf");

            mContext.startActivity(Intent.createChooser(share, mContext.getString(R.string.share_download_url)));
        });
        /*if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("USER_FRAG_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.share.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.share.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.share.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    SimpleTarget firstTarget = new SimpleTarget.Builder(mContext).setPoint(profile_imgview)
                            .setRadius(profile_imgview.getMeasuredWidth() / 2f)
                            .setDescription("Click on your profile image to view or update account information")
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder(mContext).setPoint(holder.share)
                            .setRadius(holder.share.getMeasuredWidth() / 4f)
                            .setDescription("Click on the highlighted area to share your certificate")
                            .build();

                    Spotlight.with(mContext)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(new OnSpotlightStartedListener() {
                                @Override
                                public void onStarted() {
                                }
                            })
                            .setOnSpotlightEndedListener(new OnSpotlightEndedListener() {
                                @Override
                                public void onEnded() {
                                    PreferenceManager
                                            .getDefaultSharedPreferences(mContext.getApplicationContext())
                                            .edit()
                                            .putBoolean("USER_FRAG_TIPS_DISMISSED", true)
                                            .apply();
                                }
                            })
                            .start();
                }
            });
        }*/
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView institution, course, completiondate, grade;
        LinearLayout share;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);
            institution = view.findViewById(R.id.institution);
            course = view.findViewById(R.id.course);
            completiondate = view.findViewById(R.id.completiondate);
            grade = view.findViewById(R.id.grade);
            share = view.findViewById(R.id.share);
            cardview = view.findViewById(R.id.cardview);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}