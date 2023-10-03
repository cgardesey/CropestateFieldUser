package com.cropestate.fielduser.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.QuizActivity;
import com.cropestate.fielduser.activity.QuizzesActivity;
import com.cropestate.fielduser.constants.Const;
import com.cropestate.fielduser.realm.RealmQuiz;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class QuizzesAdapter extends RecyclerView.Adapter<QuizzesAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmQuiz> realmQuizzes;
    private Context mContext;

    public QuizzesAdapter(ArrayList<RealmQuiz> realmQuizzes) {
        this.realmQuizzes = realmQuizzes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_quiz, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmQuiz realmQuiz = realmQuizzes.get(position);
        Date date = null;
        try {

            date = Const.dateFormat.parse(realmQuiz.getDate());
            DateTime dateTime = new DateTime(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        holder.starttime.setText(realmQuiz.getStarttime());
        holder.endtime.setText(realmQuiz.getEndtime());
        holder.description.setText(realmQuiz.getDescription());
        holder.title.setText(realmQuiz.getTitle());
        holder.gotoquiz.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, QuizActivity.class)
                .putExtra("ENROLMENTID", QuizzesActivity.enrolmentid)
                .putExtra("quizid", realmQuiz.getQuizid())
                .putExtra("title", realmQuiz.getTitle())
                .putExtra("startime", realmQuiz.getStarttime())
                .putExtra("endtime", realmQuiz.getEndtime())
        ));
        holder.parent.setOnClickListener(v -> {
            if (holder.detailsarea.getVisibility() == View.VISIBLE) {
                holder.detailsarea.setVisibility(View.GONE);
                holder.downbtn.animate().rotation(360).start();
            } else {
                holder.detailsarea.setVisibility(View.VISIBLE);
                holder.downbtn.animate().rotation(-180).start();
                if (realmQuiz.getPercentagescore() == null) {
                    holder.details.setVisibility(View.VISIBLE);
                    holder.scorearea.setVisibility(View.GONE);
                } else {
                    holder.details.setVisibility(View.GONE);
                    holder.scorearea.setVisibility(View.VISIBLE);
                    holder.score.setText(realmQuiz.getPercentagescore());
                }
            }
        });
        /*if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.cardview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.cardview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.cardview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // make an target
                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.cardview)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription("Click on a row to view payment status")
                            .build();

                    Spotlight.with((Activity) mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget)
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
                                            .putBoolean("PAYMENT_ACTIVITY_TIPS_DISMISSED", true)
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
        return realmQuizzes.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmQuiz> quizArrayList) {
        this.realmQuizzes = quizArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView month, day, year, starttime, endtime, description, title, score;
        LinearLayout detailsarea, details, scorearea;
        ImageView downbtn;
        CardView cardview;
        Button gotoquiz;
        RelativeLayout parent;

        public ViewHolder(View view) {
            super(view);

            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            year = view.findViewById(R.id.year);
            starttime = view.findViewById(R.id.starttime);
            endtime = view.findViewById(R.id.endtime);
            description = view.findViewById(R.id.description);
            title =  view.findViewById(R.id.response);
            detailsarea = view.findViewById(R.id.detailsarea);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);
            downbtn = view.findViewById(R.id.upbtn);
            gotoquiz = view.findViewById(R.id.gotoquiz);
            scorearea = view.findViewById(R.id.scorearea);
            score = view.findViewById(R.id.score);
            parent = view.findViewById(R.id.parent);

            itemView.setOnClickListener(this);

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

