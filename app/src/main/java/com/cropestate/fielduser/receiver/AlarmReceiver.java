package com.cropestate.fielduser.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.AssignmentActivity;
import com.cropestate.fielduser.activity.ChatActivity;
import com.cropestate.fielduser.activity.FileListActivity;
import com.cropestate.fielduser.activity.QuizzesActivity;
import com.cropestate.fielduser.pojo.MyFile;
import com.cropestate.fielduser.realm.RealmClassSessionDoc;
import com.cropestate.fielduser.realm.RealmEnrolment;
import com.cropestate.fielduser.realm.RealmRecordedVideo;
import com.cropestate.fielduser.util.FilenameUtils;
import com.cropestate.fielduser.util.RealmUtility;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.cropestate.fielduser.activity.FileListActivity.myFiles;
import static com.cropestate.fielduser.service.MyFirebaseMessagingService.COURSEPATH;
import static com.cropestate.fielduser.service.MyFirebaseMessagingService.INSTRUCTORCOURSEID;
import static com.cropestate.fielduser.service.MyFirebaseMessagingService.MESSAGE;
import static com.cropestate.fielduser.service.MyFirebaseMessagingService.TITLE;
import static com.cropestate.fielduser.service.MyFirebaseMessagingService.TYPE;

/**
 * Created by Andy on 11/8/2019.
 */


public class AlarmReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "NOTIFICATION_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Remember in the SetAlarm file we made an intent to this, this is way this work, otherwise you would have to put an action
        /*Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);*/


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Importance applicable to all the notifications in this Channel
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        // Notification channel should only be created for devices running Android 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, importance);
            //Boolean value to set if lights are enabled for Notifications from this Channel
            notificationChannel.enableLights(true);
            //Boolean value to set if vibration are enabled for Notifications from this Channel
            notificationChannel.enableVibration(true);
            //Sets the color of Notification Light
            notificationChannel.setLightColor(Color.GREEN);
            //Set the vibration pattern for notifications. Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
            notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500});
            notificationManager.createNotificationChannel(notificationChannel);
            //Sets whether notifications from these Channel should be visible on Lockscreen or not
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_splash);
        String type = intent.getStringExtra(TYPE);
        String title = "";
        String body = "";
        String coursepath = intent.getStringExtra(COURSEPATH);
        switch (type) {
            case "class":
                title = coursepath + " " + context.getString(R.string.class_upcoming_now);
                body = context.getString(R.string.download_class_material_if_one_exists);
                break;
            case "doc":
                title = coursepath + " " + context.getString(R.string.class_doc_available_for_download);
                body = context.getString(R.string.download_class_session_document);
                break;
            case "video":
                title = coursepath + " " + context.getString(R.string.class_video_available_for_download);
                body = context.getString(R.string.download_class_video);
                break;
            case "chat":
                title = intent.getStringExtra(TITLE);
                break;
            case "assignment":
                title = coursepath + " " + context.getString(R.string.class_assignment_available_for_download);
                body = context.getString(R.string.download_class_assignment);
                break;
            case "quiz":
                title = coursepath + " " + context.getString(R.string.class_class_quiz_available);
                body = context.getString(R.string.download_class_quiz);
                break;
            case "custom":
                title = context.getString(R.string.msg_from) + " " + coursepath + " " + context.getString(R.string.instructor);
                body = intent.getStringExtra(MESSAGE);
                break;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_splash)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        Intent notificationIntent;
        if (type.equals("chat")) {
            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putString(ChatActivity.INSTRUCTORCOURSEID, intent.getStringExtra(INSTRUCTORCOURSEID))
                    .putString(ChatActivity.COURSEPATH, coursepath)
                    .apply();
            notificationIntent = new Intent(context, ChatActivity.class);
        } else if (type.equals("assignment")) {
            notificationIntent = new Intent(context, AssignmentActivity.class);
        }
        else if (type.equals("quiz")) {
            final String[] enrolmentid = new String[1];
            Realm.init(context);
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                enrolmentid[0] = realm.where(RealmEnrolment.class)
                        .equalTo("instructorcourseid", intent.getStringExtra(INSTRUCTORCOURSEID))
                        .findFirst().getEnrolmentid();
            });
            notificationIntent = new Intent(context, QuizzesActivity.class)
                    .putExtra("INSTRUCTORCOURSEID", intent.getStringExtra("INSTRUCTORCOURSEID"))
                    .putExtra("ENROLMENTID", enrolmentid[0]);
        }
        else if (type.equals("video")) {
            myFiles.clear();

            Realm.init(context);
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                RealmResults<RealmRecordedVideo> realmRecordedVideos = realm.where(RealmRecordedVideo.class)
                        .equalTo("instructorcourseid", intent.getStringExtra("INSTRUCTORCOURSEID"))
                        .equalTo("isactive", 1)
                        .sort("id", Sort.DESCENDING)
                        .findAll();

                for (RealmRecordedVideo realmRecordedVideo : realmRecordedVideos) {
                    myFiles.add(new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + intent.getStringExtra("COURSEPATH").replace(" >> ", "/") + "/Videos/" + intent.getStringExtra("INSTRUCTORCOURSEID") + "/" +  FilenameUtils.getName(realmRecordedVideo.getUrl()),
                            realmRecordedVideo.getUrl(),
                            realmRecordedVideo.getGiflink()
                    ));
                }
            });


            final String[] enrolmentid = new String[1];
            Realm.init(context);
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                enrolmentid[0] = realm.where(RealmEnrolment.class)
                        .equalTo("instructorcourseid", intent.getStringExtra(INSTRUCTORCOURSEID))
                        .findFirst().getEnrolmentid();
            });

            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, enrolmentid[0])
                    .putString(FileListActivity.INSTRUCTORCOURSEID, intent.getStringExtra(INSTRUCTORCOURSEID))
                    .putString(FileListActivity.COURSEPATH, intent.getStringExtra(COURSEPATH))
                    .apply();

            notificationIntent = new Intent(context, FileListActivity.class)
                    .putExtra("activitytitle", intent.getStringExtra("COURSEPATH"))
                    .putExtra("assignmenttitle", context.getString(R.string.classvideos));
        }
        else if (type.equals("custom")) {
            notificationIntent = new Intent();
        }
        else {
            myFiles.clear();

            Realm.init(context);
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
      /*          RealmResults<RealmAudio> audios = realm.where(RealmAudio.class)
                        .isNotNull("url")
                        .notEqualTo("url", "")
                        .distinct("url")
                        .equalTo("instructorcourseid", intent.getStringExtra(INSTRUCTORCOURSEID))
                        .sort("id", Sort.DESCENDING)
                        .findAll();

                for (RealmAudio audio : audios) {
                    realm.copyToRealmOrUpdate(new RealmClassSessionDoc(audio.getUrl(), intent.getStringExtra(INSTRUCTORCOURSEID)));
                }*/

                RealmResults<RealmClassSessionDoc> classSessionDocs = realm.where(RealmClassSessionDoc.class)
                        .equalTo("instructorcourseid", intent.getStringExtra(INSTRUCTORCOURSEID))
                        .findAll();

                for (RealmClassSessionDoc classSessionDoc : classSessionDocs) {
                    myFiles.add(new MyFile(Environment.getExternalStorageDirectory() + "/SchoolDirectStudent/" + coursepath.replace(" >> ", "/") + "/Class-sessions/Documents/" + intent.getStringExtra(INSTRUCTORCOURSEID) + "/" + FilenameUtils.getName(classSessionDoc.getUrl()),
                            classSessionDoc.getUrl(),
                            null
                    ));
                }
            });
            final String[] enrolmentid = new String[1];
            Realm.init(context);
            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                enrolmentid[0] = realm.where(RealmEnrolment.class)
                        .equalTo("instructorcourseid", intent.getStringExtra(INSTRUCTORCOURSEID))
                        .findFirst().getEnrolmentid();
            });
            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putString(FileListActivity.ENROLMENTID, enrolmentid[0])
                    .apply();
            notificationIntent = new Intent(context, FileListActivity.class)
                    .putExtra("activitytitle", coursepath)
                    .putExtra("assignmenttitle", R.string.class_documents)
                    .putExtra("LAUNCED_FROM_NOTIFICATION", true);
        }


        PendingIntent contentIntent = PendingIntent.getActivity(context, 1000, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1000, builder.build());
    }
}

