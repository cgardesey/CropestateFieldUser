package com.cropestate.fielduser.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.EnrolmentActivity;

import java.util.ArrayList;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {
    ArrayList<String> names;
    private static Context mContext;
    public static ProgressDialog mProgress;

    public SearchAdapter(ArrayList<String> names) {
        this.names = names;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        mProgress = new ProgressDialog(mContext);
        mProgress.setTitle(mContext.getString(R.string.processing));
        mProgress.setMessage(mContext.getString(R.string.pls_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String name = names.get(position);
        holder.textViewName.setText(name);
        holder.textViewName.setOnClickListener(view -> mContext.startActivity(new Intent(mContext, EnrolmentActivity.class).putExtra("coursepath", name)));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<String> names) {
        this.names = names;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewName;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
        }
    }
}

