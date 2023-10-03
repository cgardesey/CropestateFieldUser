package com.cropestate.fielduser.adapter;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.LeaseUploadsActivity;
import com.cropestate.fielduser.realm.RealmLease;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * Created by Andy-Obeng on 4/3/2018.
 */

public class LeaseAdapter extends RecyclerView.Adapter<LeaseAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmLease> realmLeases;
    Context context;
    int type;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_lease, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public LeaseAdapter(ArrayList<RealmLease> realmLeases, int type) {
        this.realmLeases = realmLeases;
        this.type = type;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RealmLease realmLease = realmLeases.get(position);
        context = holder.name.getContext();
        holder.name.setText(realmLease.getNAME());
        holder.location.setText(realmLease.getLOCATION());
        holder.crop.setText(realmLease.getCROP());
        holder.code.setText(realmLease.getCODE());
        holder.coordinates.setText(realmLease.getCOORDINATES());

        holder.uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, LeaseUploadsActivity.class).putExtra("LEASECODE", realmLease.getCODE()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return realmLeases.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmLease> participantArrayList) {
        this.realmLeases = participantArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<RealmLease> arrayList) {
        realmLeases = new ArrayList<>();
        realmLeases.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        LinearLayout details;
        CardView cardview;
        TextView name, location, crop, code, coordinates;
        ImageView downbtn;
        Button uploads;


        public ViewHolder(View view) {
            super(view);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);
            name = view.findViewById(R.id.name);
            location = view.findViewById(R.id.location);
            crop = view.findViewById(R.id.crop);
            code = view.findViewById(R.id.code);
            coordinates = view.findViewById(R.id.coordinates);
            uploads = view.findViewById(R.id.uploads);
            downbtn = view.findViewById(R.id.upbtn);

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

    public static int[] splitToComponentTimes(BigDecimal biggy)
    {
        long longVal = biggy.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }
}

