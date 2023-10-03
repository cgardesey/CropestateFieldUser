package com.cropestate.fielduser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.realm.RealmTimetable;

import java.util.ArrayList;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmTimetable> RealmTimetables;
    private Context mContext;

    public TimetableAdapter(ArrayList<RealmTimetable> RealmTimetables) {
        this.RealmTimetables = RealmTimetables;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_timetable, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmTimetable realmTimetable = RealmTimetables.get(position);

        holder.course.setText(realmTimetable.getCourse());
        holder.name.setText(realmTimetable.getInstructorname());
        holder.time.setText(realmTimetable.getStarttime() + " - " + realmTimetable.getEndtime());
        holder.about.setText(realmTimetable.getAbout());


        Glide.with(mContext).load(realmTimetable.getInstructorpic()).apply(new RequestOptions()).into(holder.pic);
    }

    @Override
    public int getItemCount() {
        return RealmTimetables.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmTimetable> institutionArrayList) {
        this.RealmTimetables = institutionArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout parent;
        TextView course, name, time, about;
        ImageView downbtn;
        RoundedImageView pic;
        LinearLayout details;

        public ViewHolder(View view) {
            super(view);
            parent = view.findViewById(R.id.parent);
            name = view.findViewById(R.id.name);
            course = view.findViewById(R.id.course);
            time = view.findViewById(R.id.time);
            about = view.findViewById(R.id.about);
            downbtn = view.findViewById(R.id.upbtn);
            pic = view.findViewById(R.id.pic);
            details = view.findViewById(R.id.details);

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

