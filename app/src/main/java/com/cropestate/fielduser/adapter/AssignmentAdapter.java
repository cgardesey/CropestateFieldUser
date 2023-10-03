package com.cropestate.fielduser.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.FileListActivity;
import com.cropestate.fielduser.activity.SubmitAssignmentActivity;
import com.cropestate.fielduser.constants.Const;
import com.cropestate.fielduser.pojo.MyFile;
import com.cropestate.fielduser.realm.RealmAssignment;
import com.cropestate.fielduser.realm.RealmSubmittedAssignment;
import com.cropestate.fielduser.util.RealmUtility;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.cropestate.fielduser.activity.AssignmentActivity.refreshassignmentsimg;
import static com.cropestate.fielduser.activity.FileListActivity.myFiles;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmAssignment> realmAssignments;
    private Context mContext;

    public AssignmentAdapter(ArrayList<RealmAssignment> realmAssignments) {
        this.realmAssignments = realmAssignments;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_assignment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmAssignment realmAssignment = realmAssignments.get(position);
        Date date = null;
        if (realmAssignment.getSubmitted() == 0) {
            try {

                date = Const.dateTimeFormat.parse(realmAssignment.getCreated_at());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.datetitle.setText(mContext.getString(R.string.assignment_date));
            holder.submitassignmentbtn.setVisibility(View.VISIBLE);
            holder.scorearea.setVisibility(View.GONE);

            holder.submitassignmentbtn.setVisibility(View.VISIBLE);
            holder.viewsubmittedassignmentbtn.setVisibility(View.GONE);
        } else {
            try {
                RealmSubmittedAssignment realmSubmittedAssignment = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmSubmittedAssignment.class).equalTo("assignmentid", realmAssignment.getAssignmentid()).findFirst();
                date = Const.dateTimeFormat.parse(realmSubmittedAssignment.getCreated_at());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.datetitle.setText(mContext.getString(R.string.submission_date));
            holder.scorearea.setVisibility(View.VISIBLE);

            holder.submitassignmentbtn.setVisibility(View.GONE);
            holder.viewsubmittedassignmentbtn.setVisibility(View.VISIBLE);
        }

        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        holder.topic.setText(realmAssignment.getCoursepath());
        holder.coursename.setText(realmAssignment.getTitle());
        holder.score.setText(realmAssignment.getScore() + "%");
        holder.viewassignmentbtn.setOnClickListener(view -> {

            RealmResults<RealmAssignment> realmAssignments = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmAssignment.class).equalTo("assignmentid", realmAssignment.getAssignmentid()).findAll();
            myFiles.clear();
            for (RealmAssignment realmAssignment1 : realmAssignments) {
                String url = realmAssignment1.getUrl();
                String[] split = url.split("/");
                myFiles.add(new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + realmAssignment.getCoursepath().replace(" >> ", "/") + "/Assignments/Received/" + split[split.length - 1],
                        url,
                        null
                ));
            }

            PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, realmAssignment.getEnrolmentid())
                    .apply();
            view.getContext().startActivity(new Intent(view.getContext(), FileListActivity.class)
                    .putExtra("activitytitle", "Received Assignment")
                    .putExtra("assignmenttitle", realmAssignment.getTitle())
            );
        });
        holder.submitassignmentbtn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), SubmitAssignmentActivity.class);
            intent.putExtra("assignmentid", realmAssignment.getAssignmentid())
                    .putExtra("COURSEPATH", realmAssignment.getCoursepath());
            view.getContext().startActivity(intent);
        });
        holder.viewsubmittedassignmentbtn.setOnClickListener(view -> {

            RealmResults<RealmSubmittedAssignment> realmSubmittedAssignments = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmSubmittedAssignment.class).sort("id").equalTo("assignmentid", realmAssignment.getAssignmentid()).findAll();
            myFiles.clear();
            for (RealmSubmittedAssignment realmSubmittedAssignment : realmSubmittedAssignments) {
                String url = realmSubmittedAssignment.getUrl();
                String[] split = url.split("/");
                myFiles.add(
                        new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + realmAssignment.getCoursepath().replace(" >> ", "/") + "/Assignments/Submitted/" + split[split.length - 1],
                                url,
                                null
                        ));
            }
            PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, realmAssignment.getEnrolmentid())
                    .apply();
            view.getContext().startActivity(new Intent(view.getContext(), FileListActivity.class)
                    .putExtra("activitytitle", "Submitted Assignment")
                    .putExtra("assignmenttitle", realmSubmittedAssignments.get(0).getTitle())
            );
        });

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("ASSIGNMENTS_FRAG_TIPS_DISMISSED", false) && realmAssignment.getSubmitted() == 0) {
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
                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(refreshassignmentsimg)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.check_for_new_assignment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.downbtn)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.information_about_assignment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with((Activity) mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightEndedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                                    .edit()
                                    .putBoolean("ASSIGNMENTS_FRAG_TIPS_DISMISSED", true)
                                    .apply())
                            .start();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return realmAssignments.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmAssignment> realmAssignments) {
        this.realmAssignments = realmAssignments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        LinearLayout details, scorearea;
        TextView score, day, month, year, coursename, submitdate, topic, datetitle;
        View statuscolor;
        ImageView downbtn;
        Button viewassignmentbtn, submitassignmentbtn, viewsubmittedassignmentbtn;

        public ViewHolder(View view) {
            super(view);
            score = view.findViewById(R.id.score);
            details = view.findViewById(R.id.details);
            year = view.findViewById(R.id.year);
            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            coursename = view.findViewById(R.id.coursename);
            submitdate = view.findViewById(R.id.status);
            topic = view.findViewById(R.id.topic);
            datetitle = view.findViewById(R.id.datetitle);
            statuscolor = view.findViewById(R.id.statuscolor);
            downbtn = view.findViewById(R.id.upbtn);
            viewassignmentbtn = view.findViewById(R.id.viewassignmentbtn);
            submitassignmentbtn = view.findViewById(R.id.submitassignmentbtn);
            viewsubmittedassignmentbtn = view.findViewById(R.id.viewsubmittedassignmentbtn);
            scorearea = view.findViewById(R.id.scorearea);


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
}

