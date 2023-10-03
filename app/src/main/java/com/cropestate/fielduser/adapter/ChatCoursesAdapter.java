package com.cropestate.fielduser.adapter;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.ChatActivity;
import com.cropestate.fielduser.realm.RealmInstructorCourse;

import java.util.ArrayList;

import static com.cropestate.fielduser.activity.ChatActivity.INSTRUCTORCOURSEID;

public class ChatCoursesAdapter extends RecyclerView.Adapter<ChatCoursesAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmInstructorCourse> realmInstructorCourses;
    private Context mContext;

    public ChatCoursesAdapter(ArrayList<RealmInstructorCourse> realmInstructorCourses) {
        this.realmInstructorCourses = realmInstructorCourses;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_chat_course, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmInstructorCourse realmInstructorCourse = realmInstructorCourses.get(position);
        holder.course.setText(realmInstructorCourse.getCoursepath());
        holder.parent.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("name", realmInstructorCourse.getCoursepath());
            mContext.startActivity(intent);
        });
        Glide.with(mContext).load(realmInstructorCourse.getPicture()).apply(new RequestOptions().centerCrop()).into(holder.logo);
        holder.parent.setOnClickListener(view -> {
            PreferenceManager
                    .getDefaultSharedPreferences(mContext.getApplicationContext())
                    .edit()
                    .putString(INSTRUCTORCOURSEID, realmInstructorCourse.getInstructorcourseid())
                    .apply();

            mContext.startActivity(new Intent(mContext, ChatActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return realmInstructorCourses.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmInstructorCourse> realmInstructorCourses) {
        this.realmInstructorCourses = realmInstructorCourses;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView course;
        ImageView logo;
        RelativeLayout parent;

        public ViewHolder(View view) {
            super(view);
            course = view.findViewById(R.id.course);
            logo = view.findViewById(R.id.logo);
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

