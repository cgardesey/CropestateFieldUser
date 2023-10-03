package com.cropestate.fielduser.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shockwave.pdfium.PdfiumCore;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.PictureActivity;
import com.cropestate.fielduser.activity.VideoActivity;
import com.cropestate.fielduser.constants.Const;
import com.cropestate.fielduser.pojo.DateItem;
import com.cropestate.fielduser.realm.RealmChat;
import com.cropestate.fielduser.util.DownloadFileAsync;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cropestate.fielduser.activity.ChatActivity.COURSEPATH;
import static com.cropestate.fielduser.activity.ChatActivity.TYPE_DATE;
import static com.cropestate.fielduser.activity.ChatActivity.TYPE_REALM_CHAT;
import static com.cropestate.fielduser.activity.ChatActivity.cardview;
import static com.cropestate.fielduser.activity.ChatActivity.chatContext;
import static com.cropestate.fielduser.activity.ChatActivity.downFileAsyncMap;
import static com.cropestate.fielduser.activity.ChatActivity.isMultiSelect;
import static com.cropestate.fielduser.activity.ChatActivity.mLinearLayoutManager;
import static com.cropestate.fielduser.activity.ChatActivity.sfd_time;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.activity.PictureActivity.profilePicBitmap;
import static com.cropestate.fielduser.activity.VideoActivity.VIDEO_URL;
import static com.cropestate.fielduser.constants.Const.fileSize;
import static com.cropestate.fielduser.constants.Const.getMimeType;
import static com.cropestate.fielduser.constants.Const.toTitleCase;
import static com.cropestate.fielduser.util.PixelUtil.dpToPx;


/**
 * Created by 2CLearning on 2/8/2018.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static String FOLDER = Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/PDF";
    private static final String TAG = "ChatAdapter";
    public ArrayList<Object> selected_usersList = new ArrayList<>();
    public ArrayList<Object> consolidatedList;
    String myUserName;
    Activity activity;
    String chatid = null;

    public ChatAdapter(ArrayList<Object> consolidatedList, Activity activity) {
        this.consolidatedList = consolidatedList;
        this.myUserName = myUserName;
        this.activity = activity;
    }

    public static String getDocType(String mimeType) {

        String type = "";

        List<String> word = new ArrayList<String>();
        List<String> excel = new ArrayList<String>();
        List<String> powerpoint = new ArrayList<String>();
        List<String> pdf = new ArrayList<String>();
        List<String> text = new ArrayList<String>();

        word.add("application/msword");
        word.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        word.add("application/vnd.openxmlformats-officedocument.wordprocessingml.template");

        excel.add("application/vnd.ms-excel");
        excel.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        excel.add("application/vnd.openxmlformats-officedocument.spreadsheetml.template");

        powerpoint.add("application/vnd.ms-powerpoint");
        powerpoint.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        powerpoint.add("application/vnd.openxmlformats-officedocument.presentationml.template");
        powerpoint.add("application/vnd.openxmlformats-officedocument.presentationml.slideshow");

        pdf.add("application/pdf");

        text.add("text/plain");

        if (word.contains(mimeType)) {
            type = "word";
        } else if (excel.contains(mimeType)) {
            type = "excel";
        } else if (powerpoint.contains(mimeType)) {
            type = "powerpoint";
        } else if (pdf.contains(mimeType)) {
            type = "pdf";
        } else if (text.contains(mimeType)) {
            type = "text";
        }
        return type;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if (consolidatedList.get(position) instanceof DateItem) {
            viewType = TYPE_DATE;
        } else if (consolidatedList.get(position) instanceof RealmChat) {
            viewType = TYPE_REALM_CHAT;
        }
        return viewType;
    }

    @Override
    public int getItemCount() {
        return consolidatedList != null ? consolidatedList.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {

            case TYPE_REALM_CHAT:
                View v1 = inflater.inflate(R.layout.recycle_chat, parent, false);
                viewHolder = new MessageViewHolder(v1, activity);
                break;

            case TYPE_DATE:
                View v2 = inflater.inflate(R.layout.recycle_date, parent, false);
                viewHolder = new DateViewHolder(v2);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        switch (viewHolder.getItemViewType()) {

            case TYPE_REALM_CHAT:
                final RealmChat realmChat = (RealmChat) consolidatedList.get(position);
                final MessageViewHolder holder = (MessageViewHolder) viewHolder;
                Date date = null;
                chatid = realmChat.getChatid();
                if (realmChat.getCreated_at() != null) {
                    try {
                        date = Const.dateTimeFormat.parse(realmChat.getCreated_at());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                final boolean messageIsMine = realmChat.getSenderid().equals(PreferenceManager.getDefaultSharedPreferences(chatContext).getString(MYUSERID, ""));
                final File extStorageDir = Environment.getExternalStorageDirectory();

                holder.name.setText(realmChat.getName());
                if (date != null) {
                    holder.time.setText(sfd_time.format(date));
                } else {
                    holder.time.setText("");
                }

                final String attachmenturl = realmChat.getAttachmenturl();
                if (messageIsMine) {
                    holder.identifierLayout.setVisibility(View.GONE);

                    holder.layout.setBackgroundResource(R.drawable.bubble_in);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.layout.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    params.setMarginStart(dpToPx(activity, 64));
                    params.setMarginEnd(dpToPx(activity, 0));
//                    params.addRule(RelativeLayout.LayoutParams.WRAP_CONTENT);
                    holder.layout.setLayoutParams(params);
                    holder.identifierLayout.setMinimumWidth(dpToPx(activity, 0));

                    holder.statusImg.setVisibility(View.VISIBLE);
                    Bitmap bitmap;
                    if (realmChat.getCreated_at() == null) {
                        bitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_timer_round);

                    } else {
                        bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_tick);
                    }
                    holder.statusImg.setImageBitmap(bitmap);
                } else {
                    holder.identifierLayout.setVisibility(View.VISIBLE);
                    holder.name.setText(realmChat.getName());

                    holder.layout.setBackgroundResource(R.drawable.bubble_out);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.layout.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    params.setMarginEnd(dpToPx(activity, 64));
                    params.setMarginStart(dpToPx(activity, 0));
//                    params.addRule(RelativeLayout.LayoutParams.WRAP_CONTENT);
                    holder.layout.setLayoutParams(params);
                    /*if (realmChat.isInstructor()) {
                        holder.identifierLayout.setMinimumWidth(dpToPx(activity, 144));
                    } else {
                        holder.identifierLayout.setMinimumWidth(dpToPx(activity, 0));
                    }*/

                    holder.statusImg.setVisibility(View.GONE);
                }

                if (realmChat.isInstructor()) {
                    holder.instructor.setVisibility(View.VISIBLE);
                } else {
                    holder.instructor.setVisibility(View.INVISIBLE);
                }

                final String sub_folder = messageIsMine ? "Sent" : "Received";
                String folder = null;
                String lastpathseg = null;

                File file = null;

                String imgLoc = null;
                String vidLoc = null;
                String audioLoc = null;
                String mapLoc = null;
                if (realmChat.getText() != null) {
                    holder.metaData.setVisibility(View.GONE);
                    holder.docFrame.setVisibility(View.GONE);
                    holder.mapFrame.setVisibility(View.GONE);
                    holder.picFrame.setVisibility(View.GONE);
                    holder.txtMsgFrame.setVisibility(View.VISIBLE);

                    holder.txtMsg.setText(StringEscapeUtils.unescapeJava(realmChat.getText()));
                } else {
                    holder.pdfImg.setVisibility(View.GONE);

                    String ext = attachmenturl.substring(attachmenturl.lastIndexOf('.') + 1);
                    switch (realmChat.getAttachmenttype()) {
                        case "image":
                            folder = "Images";
                            holder.metaData.setVisibility(View.GONE);
                            holder.docFrame.setVisibility(View.GONE);
                            holder.picFrame.setVisibility(View.VISIBLE);
                            holder.vidFrame.setVisibility(View.GONE);
                            holder.audioFrame.setVisibility(View.GONE);
                            holder.txtMsgFrame.setVisibility(View.GONE);
                            holder.mapFrame.setVisibility(View.GONE);
                            break;
                        case "video":
                            folder = "Video";
                            holder.metaData.setVisibility(View.GONE);
                            holder.docFrame.setVisibility(View.GONE);
                            holder.picFrame.setVisibility(View.GONE);
                            holder.vidFrame.setVisibility(View.VISIBLE);
                            holder.audioFrame.setVisibility(View.GONE);
                            holder.txtMsgFrame.setVisibility(View.GONE);
                            holder.mapFrame.setVisibility(View.GONE);
                            break;
                        case "audio":
                            folder = "Audio";
                            holder.metaData.setVisibility(View.GONE);
                            holder.docFrame.setVisibility(View.GONE);
                            holder.picFrame.setVisibility(View.GONE);
                            holder.vidFrame.setVisibility(View.GONE);
                            holder.audioFrame.setVisibility(View.VISIBLE);
                            holder.txtMsgFrame.setVisibility(View.GONE);
                            holder.mapFrame.setVisibility(View.GONE);
                            break;
                        case "map":
                            folder = "Maps";
                            holder.metaData.setVisibility(View.GONE);
                            holder.docFrame.setVisibility(View.GONE);
                            holder.picFrame.setVisibility(View.GONE);
                            holder.vidFrame.setVisibility(View.GONE);
                            holder.audioFrame.setVisibility(View.GONE);
                            holder.txtMsgFrame.setVisibility(View.GONE);
                            holder.mapFrame.setVisibility(View.VISIBLE);
                            break;
                        default:
                            folder = "Documents";
                            holder.pdfImg.setVisibility(View.GONE);
                            holder.metaData.setVisibility(View.VISIBLE);
                            holder.docFrame.setVisibility(View.VISIBLE);
                            holder.picFrame.setVisibility(View.GONE);
                            holder.vidFrame.setVisibility(View.GONE);
                            holder.audioFrame.setVisibility(View.GONE);
                            holder.txtMsgFrame.setVisibility(View.GONE);
                            holder.mapFrame.setVisibility(View.GONE);
                            break;
                    }


                    File file_dir = new File(extStorageDir + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder, sub_folder);
                    if (!file_dir.exists()) {
                        file_dir.mkdirs();
                    }

                    if (folder.equals("Maps")) {
                        lastpathseg = chatid + ".jpg";
                    } else {
                        lastpathseg = chatid + "." + ext;
                    }
                    file = new File(extStorageDir + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder + "/" + sub_folder, lastpathseg);
                    if (file.exists()) {
                        if (folder.equals("Images")) {
                            imgLoc = extStorageDir.getAbsolutePath() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder + "/" + sub_folder + "/" + lastpathseg;

                            Drawable drawable = Drawable.createFromPath(imgLoc);
                            holder.image.setImageDrawable(drawable);


                            holder.downloadStatusWrapper_pic.setVisibility(View.INVISIBLE);
                            holder.image.setVisibility(View.VISIBLE);
                        }
                        else if (folder.equals("Video")) {
                            vidLoc = extStorageDir.getAbsolutePath() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder + "/" + sub_folder + "/" + lastpathseg;

                            holder.videoImageView.setImageBitmap(createVideoThumbNail(vidLoc));
                            holder.downloadStatusWrapper_vid.setVisibility(View.INVISIBLE);
                            holder.videoImageView.setVisibility(View.VISIBLE);
                            holder.play.setVisibility(View.VISIBLE);
                        }
                        else if (folder.equals("Audio")) {
                            audioLoc = extStorageDir.getAbsolutePath() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder + "/" + sub_folder + "/" + lastpathseg;

                            holder.audioSenseiPlayerView.setAudioTarget(Uri.parse(audioLoc));
                            holder.downloadStatusWrapper_audio.setVisibility(View.GONE);
                            holder.audioSenseiPlayerView.setVisibility(View.VISIBLE);
                        }
                        else if (folder.equals("Maps")) {
                            mapLoc = extStorageDir.getAbsolutePath() + "/SchoolDirectStudent/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(COURSEPATH, "").replace(" >> ", "/") + "/Chats/" + folder + "/" + sub_folder + "/" + lastpathseg;
                            Drawable drawable = Drawable.createFromPath(mapLoc);
                            holder.map.setImageDrawable(drawable);


                            holder.downloadStatusWrapper_map.setVisibility(View.INVISIBLE);
                            holder.map.setVisibility(View.VISIBLE);
                        }
                        else {
                            String docType = getDocType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
                            Bitmap docIconBitmap = null;
                            switch (docType) {
                                case "word":
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_doc);
                                    break;
                                case "excel":
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_xls);
                                    break;
                                case "powerpoint":
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_ppt);
                                    break;
                                case "text":
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_txt);
                                    break;
                                case "pdf":
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_pdf);
                                    break;
                                default:
                                    docIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.audio);
                                    break;
                            }

                            holder.downloadStatusWrapper_doc.setVisibility(View.INVISIBLE);
                            holder.docIcon.setImageBitmap(docIconBitmap);
                            holder.docTitle.setText(realmChat.getAttachmenttitle());
                            String meata_data = fileSize(file.length()) + " ∙ " + ext.toUpperCase();
                            holder.metaData.setText(meata_data);
                            if (docType.equals("pdf")) {
                                File pdfImgFile = new File(extStorageDir + "/SchoolDirectStudent/PDF", "PDF-" + chatid + ".png");

                                if (!pdfImgFile.exists()) {
                                    generateImageFromPdf(Uri.fromFile(file));
                                }
                                String pdfPgPrevLoc = extStorageDir.getAbsolutePath() + "/SchoolDirectStudent/PDF/" + "PDF-" + chatid + ".png";
                                Bitmap toBeCropped = BitmapFactory.decodeFile(pdfPgPrevLoc);
                                if (toBeCropped != null) {
                                    int fromHere = (int) (toBeCropped.getHeight() * 0.5);
                                    Bitmap croppedBitmap = Bitmap.createBitmap(toBeCropped, 0, 0, toBeCropped.getWidth(), fromHere);
                                    int pageCount = getPageCount(Uri.fromFile(file));
                                    String pg = pageCount == 1 ? "1 page ∙ " : pageCount + " pages ∙ ";
                                    holder.metaData.setText(pg + meata_data);
                                    //holder.metaData.setGravity(Gravity.CENTER);
                                    holder.pdfImg.setVisibility(View.VISIBLE);
                                    holder.pdfImg.setImageBitmap(croppedBitmap);
                                }
                            }

                        }
                    }
                    else {
                        if (folder.equals("Documents")) {
                            holder.downloadStatusWrapper_doc.setVisibility(View.VISIBLE);
                            holder.uploadImg_doc.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                        }
                        else if (folder.equals("Maps")) {
                            holder.downloadStatusWrapper_map.setVisibility(View.VISIBLE);
                            holder.uploadImg_map.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                        }
                        else if (folder.equals("Video")) {
                            holder.downloadStatusWrapper_vid.setVisibility(View.VISIBLE);
                            holder.uploadImg_vid.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                            holder.videoImageView.setVisibility(View.INVISIBLE);
                            holder.play.setVisibility(View.GONE);
                        }
                        else if (folder.equals("Audio")) {
                            holder.downloadStatusWrapper_audio.setVisibility(View.VISIBLE);
                            holder.uploadImg_audio.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                            holder.audioSenseiPlayerView.setVisibility(View.INVISIBLE);
                        }
                        else {
                            holder.downloadStatusWrapper_pic.setVisibility(View.VISIBLE);
                            holder.uploadImg_pic.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                        }
                    }
                }

                File finalFile = file;
                if (realmChat.getCreated_at() == null) {
                    holder.downloadStatusWrapper_doc.setVisibility(View.INVISIBLE);
                    holder.downloadStatusWrapper_pic.setVisibility(View.INVISIBLE);
                    holder.downloadStatusWrapper_vid.setVisibility(View.INVISIBLE);
                    holder.downloadStatusWrapper_audio.setVisibility(View.INVISIBLE);
                    holder.downloadStatusWrapper_map.setVisibility(View.INVISIBLE);
                }

                holder.downloadStatusWrapper_doc.setOnClickListener(view -> {

                    if (!isMultiSelect) {
                        if (holder.pbar_doc.getVisibility() == View.GONE) {
                            holder.pbar_doc.setVisibility(View.VISIBLE);
                            holder.uploadImg_doc.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.cancel));
                            downFileAsyncMap.put(chatid, new DownloadFileAsync(response -> {
                                holder.downloadStatusWrapper_doc.setVisibility(View.INVISIBLE);
                                notifyItemChanged(position);
                                if (response != null) {
                                    // unsuccessful
                                    if (response.contains("java.io.FileNotFoundException")) {
                                        holder.pbar_doc.setVisibility(View.GONE);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(toTitleCase(chatContext.getString(R.string.download_failed)))
                                                .setMessage(chatContext.getString(R.string.file_no_longer_available_for_download))

                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, progress -> {
                                //                                    holder.pbar_doc.setProgress(progress);
                            }, () -> {
                                if (finalFile.exists()) {
                                    finalFile.delete();
                                }
                                holder.pbar_doc.setVisibility(View.GONE);
                                holder.uploadImg_doc.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                                Toast.makeText(activity, chatContext.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                            }).execute(attachmenturl, finalFile.getAbsolutePath()));
                        } else {
                            if (finalFile.exists()) {
                                finalFile.delete();
                            }
                            holder.pbar_doc.setVisibility(View.GONE);
                            holder.uploadImg_doc.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));

                            AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(chatid);
                            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                                // This would not cancel downloading from httpClient
                                //  we have do handle that manually in onCancelled event inside AsyncTask
                                downloadFileAsync.cancel(true);
                            }
                        }
                    }
                });
                holder.downloadStatusWrapper_pic.setOnClickListener(view -> {

                    if (!isMultiSelect) {
                        if (holder.pbar_pic.getVisibility() == View.GONE) {
                            holder.pbar_pic.setVisibility(View.VISIBLE);
                            holder.uploadImg_pic.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.cancel));
                            downFileAsyncMap.put(chatid, new DownloadFileAsync(response -> {
                                holder.downloadStatusWrapper_pic.setVisibility(View.INVISIBLE);
                                notifyItemChanged(position);
                                if (response != null) {
                                    // unsuccessful
                                    if (response.contains("java.io.FileNotFoundException")) {
                                        holder.pbar_pic.setVisibility(View.GONE);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(toTitleCase(chatContext.getString(R.string.download_failed)))
                                                .setMessage(chatContext.getString(R.string.file_no_longer_available_for_download))

                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, progress -> {
                                //                                    holder.pbar_pic.setProgress(progress);
                            }, () -> {
                                if (finalFile.exists()) {
                                    finalFile.delete();
                                }
                                holder.pbar_pic.setVisibility(View.GONE);
                                holder.uploadImg_pic.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                                Toast.makeText(activity, chatContext.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                            }).execute(attachmenturl, finalFile.getAbsolutePath()));
                        } else {
                            if (finalFile.exists()) {
                                finalFile.delete();
                            }
                            holder.pbar_pic.setVisibility(View.GONE);
                            holder.uploadImg_pic.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));

                            AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(chatid);
                            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                                // This would not cancel downloading from httpClient
                                //  we have do handle that manually in onCancelled event inside AsyncTask
                                downloadFileAsync.cancel(true);
                            }
                        }
                    }
                });
                holder.downloadStatusWrapper_vid.setOnClickListener(view -> {

                    if (!isMultiSelect) {
                        if (holder.pbar_vid.getVisibility() == View.GONE) {
                            holder.pbar_vid.setVisibility(View.VISIBLE);
                            holder.uploadImg_vid.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.cancel));
                            downFileAsyncMap.put(chatid, new DownloadFileAsync(response -> {
                                holder.downloadStatusWrapper_vid.setVisibility(View.INVISIBLE);
                                notifyItemChanged(position);
                                if (response != null) {
                                    // unsuccessful
                                    if (response.contains("java.io.FileNotFoundException")) {
                                        holder.pbar_vid.setVisibility(View.GONE);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(toTitleCase(chatContext.getString(R.string.download_failed)))
                                                .setMessage(chatContext.getString(R.string.file_no_longer_available_for_download))

                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, progress -> {
                                //                                    holder.pbar_vid.setProgress(progress);
                            }, () -> {
                                if (finalFile.exists()) {
                                    finalFile.delete();
                                }
                                holder.pbar_vid.setVisibility(View.GONE);
                                holder.uploadImg_vid.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                                Toast.makeText(activity, chatContext.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                            }).execute(attachmenturl, finalFile.getAbsolutePath()));
                        } else {
                            if (finalFile.exists()) {
                                finalFile.delete();
                            }
                            holder.pbar_vid.setVisibility(View.GONE);
                            holder.uploadImg_vid.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));

                            AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(chatid);
                            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                                // This would not cancel downloading from httpClient
                                //  we have do handle that manually in onCancelled event inside AsyncTask
                                downloadFileAsync.cancel(true);
                            }
                        }
                    }
                });
                holder.downloadStatusWrapper_audio.setOnClickListener(view -> {

                    if (!isMultiSelect) {
                        if (holder.pbar_audio.getVisibility() == View.GONE) {
                            holder.pbar_audio.setVisibility(View.VISIBLE);
                            holder.uploadImg_audio.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.cancel));
                            downFileAsyncMap.put(chatid, new DownloadFileAsync(response -> {
                                holder.downloadStatusWrapper_audio.setVisibility(View.INVISIBLE);
                                notifyItemChanged(position);
                                if (response != null) {
                                    // unsuccessful
                                    if (response.contains("java.io.FileNotFoundException")) {
                                        holder.pbar_audio.setVisibility(View.GONE);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(toTitleCase(chatContext.getString(R.string.download_failed)))
                                                .setMessage(chatContext.getString(R.string.file_no_longer_available_for_download))

                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, progress -> {
                                //                                    holder.pbar_audio.setProgress(progress);
                            }, () -> {
                                if (finalFile.exists()) {
                                    finalFile.delete();
                                }
                                holder.pbar_audio.setVisibility(View.GONE);
                                holder.uploadImg_audio.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                                Toast.makeText(activity, chatContext.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                            }).execute(attachmenturl, finalFile.getAbsolutePath()));
                        } else {
                            if (finalFile.exists()) {
                                finalFile.delete();
                            }
                            holder.pbar_audio.setVisibility(View.GONE);
                            holder.uploadImg_audio.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));

                            AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(chatid);
                            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                                // This would not cancel downloading from httpClient
                                //  we have do handle that manually in onCancelled event inside AsyncTask
                                downloadFileAsync.cancel(true);
                            }
                        }
                    }
                });
                holder.downloadStatusWrapper_map.setOnClickListener(view -> {
                    if (!isMultiSelect) {
                        if (holder.pbar_map.getVisibility() == View.GONE) {
                            holder.pbar_map.setVisibility(View.VISIBLE);
                            holder.uploadImg_map.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.cancel));
                            downFileAsyncMap.put(chatid, new DownloadFileAsync(response -> {
                                holder.downloadStatusWrapper_map.setVisibility(View.INVISIBLE);
                                notifyItemChanged(position);
                                if (response != null) {
                                    // unsuccessful
                                    if (response.contains("java.io.FileNotFoundException")) {
                                        holder.pbar_map.setVisibility(View.GONE);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(toTitleCase(chatContext.getString(R.string.download_failed)))
                                                .setMessage(chatContext.getString(R.string.file_no_longer_available_for_download))

                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, progress -> {
                                //                                    holder.pbar_map.setProgress(progress);
                            }, () -> {
                                if (finalFile.exists()) {
                                    finalFile.delete();
                                }
                                holder.pbar_map.setVisibility(View.GONE);
                                holder.uploadImg_map.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));
                                Toast.makeText(activity, chatContext.getString(R.string.download_cancelled), Toast.LENGTH_SHORT).show();
                            }).execute(attachmenturl, finalFile.getAbsolutePath()));
                        } else {
                            if (finalFile.exists()) {
                                finalFile.delete();
                            }
                            holder.pbar_map.setVisibility(View.GONE);
                            holder.uploadImg_map.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.download));

                            AsyncTask<String, Integer, String> downloadFileAsync = downFileAsyncMap.get(chatid);
                            if (downloadFileAsync != null && downloadFileAsync.getStatus() != AsyncTask.Status.FINISHED) {
                                // This would not cancel downloading from httpClient
                                //  we have do handle that manually in onCancelled event inside AsyncTask
                                downloadFileAsync.cancel(true);
                            }
                        }
                    }
                });

                boolean linkPrevExists = realmChat.getLinktitle() != null;
                if (linkPrevExists) {
                    holder.linkPrevFrame.setVisibility(View.VISIBLE);
                    holder.close.setVisibility(View.GONE);
                    if (realmChat.getLinkimage() == null) {
                        holder.linkImg.setVisibility(View.GONE);
                    } else {
                        holder.linkImg.setVisibility(View.VISIBLE);
                        Glide.with(chatContext).load(realmChat.getLinkimage()).apply(new RequestOptions().fitCenter()).into(holder.linkImg);
                    }

                    holder.linkTitle.setText(realmChat.getLinktitle());
                    holder.linkDesc.setText(realmChat.getLinkdescription());
                } else {
                    holder.linkPrevFrame.setVisibility(View.GONE);
                }

                boolean referencedChatExists = realmChat.getChatrefid() != null;
                if (referencedChatExists) {
                    holder.replyPrevFrame.setVisibility(View.VISIBLE);
                    holder.replyClose.setVisibility(View.GONE);
                    holder.replyName.setText(realmChat.getReplyname());
                    holder.replyBody.setText(realmChat.getReplybody());
                } else {
                    holder.replyPrevFrame.setVisibility(View.GONE);
                }

                holder.replyPrevFrame.setOnClickListener(v -> {
                    if (!isMultiSelect) {
                        for (int i = 0; i < consolidatedList.size(); i++) {
                            Object obj = consolidatedList.get(i);
                            if (obj instanceof RealmChat) {
                                if (((RealmChat) obj).getChatid().equals(((RealmChat) consolidatedList.get(position)).getChatrefid())) {
                                    mLinearLayoutManager.scrollToPositionWithOffset(i, 0);
                                    return;
                                }
                            }
                        }
                    }
                });

                holder.linkPrevFrame.setOnClickListener(v -> {
                    if (!isMultiSelect) {
                        openLinkIfIsLink(realmChat);
                    }
                });

                holder.docFrame.setOnClickListener(v -> {

                    if (!isMultiSelect) {
                        if (holder.downloadStatusWrapper_doc.getVisibility() == View.INVISIBLE) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            if (finalFile.exists()) {

                                Log.d("gard", finalFile.toString());

                                String mimeType = getMimeType(finalFile.getAbsolutePath());
                                Uri docURI = FileProvider.getUriForFile(chatContext, chatContext.getApplicationContext().getPackageName() + ".provider", finalFile);
                                intent.setDataAndType(docURI, mimeType);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                try {
                                    activity.startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(chatContext, chatContext.getString(R.string.no_suitable_app_for_viewing_this_file), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(activity, activity.getString(R.string.sorry_document_file_not_exist_on_your_internal_storrage), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                String finalImgLoc = imgLoc;
                holder.picFrame.setOnClickListener(v -> {
                    if (!isMultiSelect) {
                        if (holder.downloadStatusWrapper_pic.getVisibility() == View.INVISIBLE) {
                            if (finalFile.exists()) {
                                profilePicBitmap = BitmapFactory.decodeFile(finalImgLoc);
                                activity.startActivity(new Intent(activity, PictureActivity.class));
                            } else {
                                Toast.makeText(activity, chatContext.getString(R.string.sorry_document_file_not_exist_on_your_internal_storrage), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                String finalVidLoc = vidLoc;
                holder.vidFrame.setOnClickListener(v -> {
                    if (!isMultiSelect) {
                        if (holder.downloadStatusWrapper_vid.getVisibility() == View.INVISIBLE) {
                            if (finalFile.exists()) {
                                VIDEO_URL = finalVidLoc;
                                activity.startActivity(new Intent(activity, VideoActivity.class));
                            } else {
                                Toast.makeText(activity, chatContext.getString(R.string.sorry_document_file_not_exist_on_your_internal_storrage), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                holder.layout_parent.setOnClickListener(v -> cardview.setVisibility(View.GONE));

                holder.mapFrame.setOnClickListener(v -> {
                    if (!isMultiSelect) {
                        Uri gmmIntentUri = Uri.parse(realmChat.getLinkdescription());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                            activity.startActivity(mapIntent);
                        }
                    }
                });

                if (selected_usersList.contains(consolidatedList.get(position)))
                    holder.layout_parent.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.list_item_selected_state));
                else
                    holder.layout_parent.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.list_item_normal_state));

                break;

            case TYPE_DATE:
                DateItem dateItem = (DateItem) consolidatedList.get(position);
                DateViewHolder dateViewHolder = (DateViewHolder) viewHolder;

                // Populate date item data here
                dateViewHolder.date.setText(dateItem.getDate());

                break;
        }
    }
    //PdfiumAndroid (https://github.com/barteksc/PdfiumAndroid)
//https://github.com/barteksc/AndroidPdfViewer/issues/49

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setFilter(ArrayList<Object> arrayList) {
        consolidatedList = new ArrayList<>();
        consolidatedList.addAll(arrayList);
        notifyDataSetChanged();
    }

    int getPageCount(Uri pdfUri) {
        int pageCount = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(chatContext);
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = chatContext.getContentResolver().openFileDescriptor(pdfUri, "r");
            com.shockwave.pdfium.PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pageCount = pdfiumCore.getPageCount(pdfDocument);
        } catch (Exception e) {
            //todo with exception
            Log.d(TAG, e.toString());
        }
        return pageCount;
    }

    void generateImageFromPdf(Uri pdfUri) {
        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(chatContext);
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            ParcelFileDescriptor fd = chatContext.getContentResolver().openFileDescriptor(pdfUri, "r");
            com.shockwave.pdfium.PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
            saveImage(bmp);
            pdfiumCore.closeDocument(pdfDocument); // important!
        } catch (Exception e) {
            //todo with exception
            Log.d(TAG, e.toString());
        }
    }

    private void saveImage(Bitmap bmp) {
        FileOutputStream out = null;
        try {
            File folder = new File(FOLDER);
            if (!folder.exists())
                folder.mkdirs();
            File file = new File(folder, "PDF-" + chatid + ".png");
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            //todo with exception
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //todo with exception
            }
        }
    }

    public void openLinkIfIsLink(RealmChat realmChat) {
        boolean isLink = realmChat.getLink() != null;
        if (isLink) {
            Uri webpage = Uri.parse(realmChat.getLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        }
    }

    public Bitmap createVideoThumbNail(String path){
        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
    }
}

