package com.cropestate.fielduser.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.realm.RealmAudio;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmAudio> realmAudios;
    private Activity activity;
    AudioAdapterInterface audioAdapterInterface;
    Context mContext;

    public AudioAdapter(AudioAdapterInterface audioAdapterInterface, Activity activity, ArrayList<RealmAudio> realmAudios) {
        this.audioAdapterInterface = audioAdapterInterface;
        this.activity = activity;
        this.realmAudios = realmAudios;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_resource, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmAudio realmAudio = realmAudios.get(position);
        holder.title.setText(realmAudio.getTitle());
        holder.date.setText(realmAudio.getCreated_at());
        holder.cardview.setOnClickListener(v -> audioAdapterInterface.onListItemClick(realmAudios, position, holder));
    }

    @Override
    public int getItemCount() {
        return realmAudios.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmAudio> resourceArrayList) {
        this.realmAudios = resourceArrayList;
        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b) {

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
    //get bitmap image from byte array

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, duration;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            cardview = view.findViewById(R.id.cardview);
        }
    }

    public interface AudioAdapterInterface {
        void onListItemClick(ArrayList<RealmAudio> realmAudios, int position, AudioAdapter.ViewHolder holder);
    }
}

