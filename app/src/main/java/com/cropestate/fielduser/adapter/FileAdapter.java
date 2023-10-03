package com.cropestate.fielduser.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;

import java.io.File;
import java.util.ArrayList;

import static com.cropestate.fielduser.activity.SubmitAssignmentActivity.submit;
import static com.cropestate.fielduser.constants.Const.getMimeType;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> implements Filterable {
    ArrayList<File> files;
    private Context mContext;

    public FileAdapter(ArrayList<File> files) {
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_submit_file_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        File file = files.get(position);
        holder.path.setText(file.getAbsolutePath());
        holder.remove.setOnClickListener(view -> {
            files.remove(file);
            notifyDataSetChanged();

            if (files.size() < 1) {
                submit.setVisibility(View.GONE);
            }
        });
        holder.cardview.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);

            String mimeType = getMimeType(file.getAbsolutePath());
            Uri docURI = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(docURI, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, mContext.getString(R.string.sorry_no_document_found), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView path;
        ImageView remove;
        CardView cardview;
//        ImageView uploadImg;
//        ProgressBar pbar;
//        RelativeLayout downloadStatusWrapper;

        public ViewHolder(View view) {
            super(view);
            path = view.findViewById(R.id.path);
            remove = view.findViewById(R.id.remove);
            cardview = view.findViewById(R.id.cardview);
//            uploadImg = (ImageView) view.findViewById(R.id.uploadImg);
//            downloadStatusWrapper = (RelativeLayout) view.findViewById(R.id.downloadStatusWrapper);
//            pbar = (ProgressBar) view.findViewById(R.id.pbar);

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

