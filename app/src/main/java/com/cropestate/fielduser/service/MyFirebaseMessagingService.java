package com.cropestate.fielduser.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.HomeActivity;
import com.cropestate.fielduser.realm.RealmChat;
import com.cropestate.fielduser.realm.RealmInstructor;
import com.cropestate.fielduser.realm.RealmStudent;
import com.cropestate.fielduser.realm.RealmUser;
import com.cropestate.fielduser.receiver.AlarmReceiver;
import com.cropestate.fielduser.util.MyWorker;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.ChatActivity.activity;
import static com.cropestate.fielduser.activity.HomeActivity.retriev_current_registration_token;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 * <p>
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 * <p>
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public static final String INSTRUCTORCOURSEID = "INSTRUCTORCOURSEID";
    public static final String COURSEPATH = "COURSEPATH";
    public static final String TITLE = "TITLE";
    public static final String TOPIC = "TOPIC";
    public static final String URL = "URL";
    public static final String TYPE = "TYPE";
    public static final String BODY = "BODY";
    public static final String TITTLE = "TITTLE";
    public static final String MESSAGE = "MESSAGE";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "engineer_From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("engineer", "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            String type = data.get("type").toString();
            if (type.equals("chat")) {
                JSONObject jsonResponse = new JSONObject(data);

                Realm.init(getApplicationContext());
                JSONObject finalJsonResponse = jsonResponse;
                Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                    try {
                        JSONObject chatresponse = new JSONObject(finalJsonResponse.getString("chatresponse"));
                        RealmChat realmChat = realm.createOrUpdateObjectFromJson(RealmChat.class, chatresponse.getJSONObject("chat"));
                        realm.createOrUpdateObjectFromJson(RealmUser.class, chatresponse.getJSONObject("sender"));

                        if (chatresponse.has("student")) {
                            realm.createOrUpdateObjectFromJson(RealmStudent.class, chatresponse.getJSONObject("student"));
                        } else if (chatresponse.has("instructor")) {
                            realm.createOrUpdateObjectFromJson(RealmInstructor.class, chatresponse.getJSONObject("instructor"));
                        }
                        if (chatresponse.has("referenced_chat")) {
                            realm.createOrUpdateObjectFromJson(RealmChat.class, chatresponse.getJSONObject("referenced_chat"));
                        }

                        boolean chatClassInForeground = activity != null && realmChat.getInstructorcourseid().equals(PreferenceManager.getDefaultSharedPreferences(activity).getString(INSTRUCTORCOURSEID, ""));
                        if (!chatClassInForeground) {

                            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class)
                                    .putExtra(TYPE, "CHAT")
                                    .putExtra(INSTRUCTORCOURSEID, realmChat.getInstructorcourseid())
                                    .putExtra(COURSEPATH, data.get("type").toString())
                                    .putExtra(TITLE, finalJsonResponse.getString("title"))
                                    .putExtra(BODY, finalJsonResponse.getString("body"));
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 23424243, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (2 * 1000), pendingIntent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
            else {
                String instructorcourseid = data.get("instructorcourseid").toString();
                String coursepath = data.get("coursepath").toString();
                String title = data.get("title").toString();
                String url = data.get("url").toString();

                Log.d("engineer", "type: " + type);
                Log.d("engineer", "instructorcourseid: " + instructorcourseid);
                Log.d("engineer", "coursepath: " + coursepath);
                Log.d("engineer", "title: " + title);
                Log.d("engineer", "url: " + url);

                /*if (*//* Check if data needs to be processed by long running job *//* true) {
                    // For long-running tasks (10 seconds or more) use WorkManager.
                    scheduleJob();
                } else {
                    // Handle message within 10 seconds
                    handleNow();
                }*/

                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class)
                        .putExtra(TYPE, type)
                        .putExtra(INSTRUCTORCOURSEID, instructorcourseid)
                        .putExtra(COURSEPATH, coursepath)
                        .putExtra(TITLE, title)
                        .putExtra(URL, url);
                if (type.equals("custom")) {
                    intent.putExtra(MESSAGE, data.get("message").toString());
                }
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 23424243, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (2 * 1000), pendingIntent);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        retriev_current_registration_token(getApplicationContext(), token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo_square)
                        .setContentTitle("Message Title")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
