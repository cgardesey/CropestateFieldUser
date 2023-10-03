package com.cropestate.fielduser.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.pojo.MyFile;

import java.io.File;
import java.util.ArrayList;

import static com.cropestate.fielduser.constants.Const.fileSize;
import static com.cropestate.fielduser.constants.Const.getMimeType;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> implements Filterable {
    FileListAdapterInterface fileListAdapterInterface;
    ArrayList<MyFile> myFiles;
    private Context mContext;

    public FileListAdapter(FileListAdapterInterface fileListAdapterInterface, ArrayList<MyFile> myFiles) {
        this.fileListAdapterInterface = fileListAdapterInterface;
        this.myFiles = myFiles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_file_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        MyFile myFile = myFiles.get(position);
        holder.path.setText(myFile.getPath());
        File file = new File(myFile.getPath());
        if (file.exists()) {
            holder.downloadStatusWrapper.setVisibility(View.GONE);
            holder.removelayout.setVisibility(View.VISIBLE);
            holder.size.setText(fileSize(file.length()));
        } else {
            holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
            holder.pbar.setVisibility(View.GONE);
            holder.download.setVisibility(View.VISIBLE);
            holder.removelayout.setVisibility(View.GONE);
        }
        if (myFile.getGiflink() != null) {
            Glide.with(mContext).asGif().load(myFile.getGiflink()).apply(new RequestOptions()
                    .centerCrop()
                    .placeholder(null)
                    .error(R.drawable.error))
                    .into(holder.preview);
            holder.preview.setVisibility(View.VISIBLE);
        }
        else {
            holder.preview.setVisibility(View.GONE);
        }
        holder.removelayout.setOnClickListener(v -> {
            if (file.delete()) {
                holder.downloadStatusWrapper.setVisibility(View.VISIBLE);
                holder.pbar.setVisibility(View.GONE);
                holder.download.setVisibility(View.VISIBLE);
                holder.removelayout.setVisibility(View.GONE);
            }
            else {
                Toast.makeText(mContext, mContext.getString(R.string.error_deleting_file_from_device), Toast.LENGTH_SHORT).show();
            }
        });
        holder.downloadStatusWrapper.setOnClickListener(view -> fileListAdapterInterface.onListItemClick(myFiles, position, holder));
        holder.cardview.setOnClickListener(v -> {

            if (holder.downloadStatusWrapper.getVisibility() == View.GONE) {
                Intent intent = new Intent(Intent.ACTION_VIEW);

                String mimeType = getMimeType(myFile.getPath());
                Uri docURI = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(docURI, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            }
        });
    }

    public interface FileListAdapterInterface {
        void onListItemClick(ArrayList<MyFile> files, int position, FileListAdapter.ViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return myFiles.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<MyFile> myFiles) {
        this.myFiles = myFiles;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView path;
        public TextView size;
        CardView cardview;
        public RelativeLayout downloadStatusWrapper, removelayout;
        public ProgressBar pbar;
        public ImageView download;
        public ImageView remove, preview;

        public ViewHolder(View view) {
            super(view);
            path = view.findViewById(R.id.path);
            cardview = view.findViewById(R.id.cardview);
            downloadStatusWrapper = view.findViewById(R.id.downloadStatusWrapper);
            removelayout = view.findViewById(R.id.removelayout);
            pbar = view.findViewById(R.id.pbar);
            download = view.findViewById(R.id.download);
            remove = view.findViewById(R.id.remove);
            size = view.findViewById(R.id.size);
            preview = view.findViewById(R.id.preview);

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

