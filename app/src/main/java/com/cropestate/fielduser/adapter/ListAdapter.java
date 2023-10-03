package com.cropestate.fielduser.adapter;

/**
 * Created by Nana on 11/10/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.takusemba.spotlight.SimpleTarget;
import com.takusemba.spotlight.Spotlight;
import com.cropestate.fielduser.R;

import java.util.ArrayList;

import static com.cropestate.fielduser.fragment.SearchCourseFragment.searchlayout;

/**
 * Created by Belal on 6/6/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private static final String YOUR_DIALOG_TAG = "";
    ListAdapterInterface listAdapterInterface;
    Context mContext;
    String title = "";
    private ArrayList<String> names;

    public ListAdapter(ListAdapterInterface listAdapterInterface, Context mContext, ArrayList<String> names, String title) {
        this.listAdapterInterface = listAdapterInterface;
        this.mContext = mContext;
        this.names = names;
        this.title = title;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textViewName.setText(names.get(position));
        holder.textViewName.setOnClickListener(view -> listAdapterInterface.onListItemClick(names, position, holder));

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("SEARCH_FRAG_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.textViewName.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.textViewName.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.textViewName.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(searchlayout)
                            .setRadius(150F)
                            .setDescription(mContext.getString(R.string.search_courses_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    SimpleTarget secondTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.textViewName)
                            .setRadius(150F)
                            .setDescription(mContext.getString(R.string.choose_course_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with((Activity) mContext)
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(() -> {
                            })
                            .setOnSpotlightEndedListener(() -> PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                                    .edit()
                                    .putBoolean("SEARCH_FRAG_TIPS_DISMISSED", true)
                                    .apply())
                            .start();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public interface ListAdapterInterface {
        void onListItemClick(ArrayList<String> names, int position, ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewName;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
        }
    }
}
