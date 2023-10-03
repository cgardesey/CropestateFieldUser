package com.cropestate.fielduser.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.PictureActivity;
import com.cropestate.fielduser.realm.RealmLeaseUpload;
import com.cropestate.fielduser.util.DownloadFileAsync;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

import static com.cropestate.fielduser.activity.ChatActivity.COURSEPATH;
import static com.cropestate.fielduser.activity.ChatActivity.chatContext;
import static com.cropestate.fielduser.activity.ChatActivity.downFileAsyncMap;
import static com.cropestate.fielduser.activity.ChatActivity.isMultiSelect;
import static com.cropestate.fielduser.activity.PictureActivity.profilePicBitmap;
import static com.cropestate.fielduser.constants.Const.toTitleCase;


/**
 * Created by Andy-Obeng on 4/3/2018.
 */

public class LeaseUploadsAdapter extends RecyclerView.Adapter<LeaseUploadsAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmLeaseUpload> realmLeaseUploads;
    Context context;
    int type;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_lease_upload, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public LeaseUploadsAdapter(ArrayList<RealmLeaseUpload> realmLeaseUploads, int type) {
        this.realmLeaseUploads = realmLeaseUploads;
        this.type = type;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RealmLeaseUpload realmLeaseUpload = realmLeaseUploads.get(position);
        context = holder.photoImageView.getContext();
        Glide.with(context).load(realmLeaseUpload.getUrl()).apply( new RequestOptions().centerCrop()).into(holder.photoImageView);

        File file_dir = new File(Environment.getExternalStorageDirectory() + "/CropEstate", "Images");
        if (!file_dir.exists()) {
            file_dir.mkdirs();
        }
        String ext = realmLeaseUpload.getUrl().substring(realmLeaseUpload.getUrl().lastIndexOf("."));
        String lastpathseg = realmLeaseUpload.getLeaseuploadid() + ext;
        String imgLoc = Environment.getExternalStorageDirectory() + "/CropEstate/Images/" + lastpathseg;
        File file = new File(imgLoc);

        if (file.exists()) {
            Drawable drawable = Drawable.createFromPath(imgLoc);
            holder.photoImageView.setImageDrawable(drawable);
            holder.downloadStatusWrapper.setVisibility(View.INVISIBLE);
            holder.photoImageView.setVisibility(View.VISIBLE);
        }
        else {
            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
            holder.uploadImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.download));
        }

        holder.downloadStatusWrapper.setOnClickListener(view -> {
            if (file.exists()) {
                file.delete();
            }

            if (!isMultiSelect) {
                if (holder.pbar.getVisibility() == View.GONE) {
                    holder.pbar.setVisibility(View.VISIBLE);
                    holder.uploadImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.cancel));
                    String url = realmLeaseUpload.getUrl();
                    downFileAsyncMap.put(realmLeaseUpload.getLeaseuploadid(), new DownloadFileAsync(response -> {
                        holder.downloadStatusWrapper.setVisibility(View.INVISIBLE);
                        notifyItemChanged(position);
                        if (response != null) {
                            // unsuccessful
                            if (response.contains("java.io.FileNotFoundException")) {
                                holder.pbar.setVisibility(View.GONE);
                                new AlertDialog.Builder(context)
                                        .setTitle(toTitleCase(context.getString(R.string.download_failed)))
                                        .setMessage(context.getString(R.string.file_no_longer_available_for_download))

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                        })
                                        .show();

                            } else {
                                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, progress -> {
                        //                                    holder.pbar_pic.setProgress(progress);
                    }, () -> {
                        if (file.exists()) {
                            file.delete();
                        }
                        holder.pbar.setVisibility(View.GONE);
                        holder.uploadImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.download));
                        Toast.makeText(context, context.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                    }).execute(url, file.getAbsolutePath()));
                } else {
                    holder.pbar.setVisibility(View.GONE);
                    holder.uploadImg.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.download));

                    AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(realmLeaseUpload.getLeaseuploadid());
                    if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                        // This would not cancel downloading from httpClient
                        //  we have do handle that manually in onCancelled event inside AsyncTask
                        downloadFileAsync.cancel(true);
                    }
                }
            }
        });

        holder.photoImageView.setOnClickListener(v -> {
            if (!isMultiSelect) {
                if (file.exists()) {
                    profilePicBitmap = BitmapFactory.decodeFile(imgLoc);
                    context.startActivity(new Intent(context, PictureActivity.class));
                } else {
                    Toast.makeText(context, chatContext.getString(R.string.sorry_document_file_not_exist_on_your_internal_storrage), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return realmLeaseUploads.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmLeaseUpload> participantArrayList) {
        this.realmLeaseUploads = participantArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<RealmLeaseUpload> arrayList) {
        realmLeaseUploads = new ArrayList<>();
        realmLeaseUploads.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ProgressBar pbar;
        TextView retry_text;
        RelativeLayout downloadStatusWrapper;
        ImageView downbtn, photoImageView, uploadImg;
        CardView retry;
        Button uploads;


        public ViewHolder(View view) {
            super(view);
            retry_text = view.findViewById(R.id.retry_text);
            retry = view.findViewById(R.id.retry);
            uploadImg = view.findViewById(R.id.uploadImg);
            pbar = view.findViewById(R.id.pbar);
            uploads = view.findViewById(R.id.uploads);
            downbtn = view.findViewById(R.id.upbtn);
            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            photoImageView = view.findViewById(R.id.photoImageView);
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

