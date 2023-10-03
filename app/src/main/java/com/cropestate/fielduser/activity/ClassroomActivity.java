package com.cropestate.fielduser.activity;

import android.animation.ArgbEvaluator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.dialer.dialpadview.DialpadView;
import com.android.dialer.dialpadview.DigitsEditText;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.gsm.gsm.CallManager;
import com.gsm.gsm.UiCall;
import com.makeramen.roundedimageview.RoundedImageView;
import com.shockwave.pdfium.PdfDocument;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.ClassroomAdapter;
import com.cropestate.fielduser.materialDialog.ClassRoomDialog;
import com.cropestate.fielduser.pojo.Participant;
import com.cropestate.fielduser.realm.RealmDialcode;
import com.cropestate.fielduser.util.RealmUtility;
import com.cropestate.fielduser.util.Time_Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static com.cropestate.fielduser.activity.GetAuthActivity.getAuthActivityContext;
import static com.cropestate.fielduser.activity.PhoneActivity.ROOMID;
import static com.cropestate.fielduser.fragment.SettingsFragment.ISNIGHTMODE;

public class ClassroomActivity extends PermisoActivity implements CallManager.StateListener, Handler.Callback, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    public static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
    public static final int FILE_PICKER_REQUEST_CODE = 21;
    public static final int FILE_PICKER_REQUEST_CODE_PB = 22;
    private static final String INCOMING_CALL = "INCOMING_CALL";

    static public File classFile;

    boolean VERIFICATIONCALLTIMERSTOPED = false;
    boolean callsuccessfulyplaced = false;
    private static RemoteViews contentView;
    private static Notification notification;
    private static NotificationManager notificationManager;
    private static final int NotificationID = 1005;
    private static NotificationCompat.Builder mBuilder;
    static Boolean CLASSINSESSION = false;

    private static Context mContext;
    ImageView menubtn, searchbtn;
    //  private Callbacks mCallbacks;
    ArrayList<Participant> participantArrayList, existingParticipantList;
    ArrayList<String> phonenumberArrayList, starttimeArrayList, muteArraylist, durationArraylist;
    ShimmerFrameLayout shimmer_view_container;
    TextView totalnumberView;
    RecyclerView recyclerview;
    ImageView reloadbtn, pickfile, pickfile_pb;
    ArrayList<String> notifyArraylist;
    private static Handler mHandler;
    private static final long PERIOD_MILLIS = 1000L;
    private static final int MSG_UPDATE_ELAPSEDTIME = 100;
    private long mElapsedTime;
    private Timer mTimer;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    private static RecyclerView.LayoutManager layoutManager;
    long starttime;

    static LinearLayout activitylayout;

    public static ImageView loadimg;
    public static TextView resultnumber;
    private ClassroomAdapter classroomAdapter;
    ImageView speakerimg, speakerimg_gp, speakerimg_pb;
    private LinearLayout classspeakerview_gp, leavegroupcallview, classsbluetoothview;
    private AudioManager audioManager;
    Button retrybtn;
    LinearLayout newcreatecourselayout, error_loading;
    TextView timertext;
    LinearLayout parentLayout;

    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final String TAG = "CallActivity";
    private static final String MY_CALL_DURATION = "CALL DURATION";
    private static final String MY_TIMER = "CALL_TIMER";
    private final static String DEFAULT_REGION_CODE = "US";
    private static final int READ_REQUEST_CODE = 300;
    PDFView pdfView, pdfView_pb;
    SharedPreferences.Editor mEditor;
    LinearLayout classspeakerview;
    LinearLayout classbluetoothview;
    LinearLayout regularcalllayout;
    LinearLayout playbacklayout;
    static LinearLayout verifyuserlayout;
    LinearLayout classspeakerview_pb;
    LinearLayout classbluetoothview_pb;
    LinearLayout endplaybackview;
    ImageView rewindIcon, playIcon, fastforwardIcon;
    Boolean isCallpaused = false, isMute = false, isBluetoothon = false, isClassroom = false;
    RoundedImageView userImage;
    long currentTime, prevTime, timeDif = 0;
    Toast toast;
    Integer pageNumber = 0;
    String pdfFileName;
    private LinearLayout classroomcalllayout, groupcalllayout, leaveclassview;
    private AsYouTypeFormatter formatter;
    private String input = "";
    private String regionCode = DEFAULT_REGION_CODE;
    private boolean formatAsYouType = true;
    private Button ask;
    private DigitsEditText digits;
    View statuscolor;
    private AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver;
    private TextView nofiletext, nofiletext_pb;

    private int field = 0x00000020;
    static PowerManager powerManager;
    static PowerManager.WakeLock wakeLock;

    public static final int
            REGULARCALL = 1,
            CLASSCALL = 2,
            CLASSPLAYBACK = 3,
            GROUPCALL = 4,
            VERIFICATIONCALL = 5;

    public static int CALLMODE = REGULARCALL;

    TextView callobjid_class, callobjid_pb, callobjid_gp;
    private static int notid;

    static Activity activity;
    private String calldirection = "";
    private String callerid = "";

    RelativeLayout questionLayout;
    RecyclerView my_recyclerview_questions;

    private Handler timerHandler;

    static ValueAnimator animator, animator_verif;
    private Uri uri;
    static AlertDialog acceptCallAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);
        getInitialCallstate(CallManager.get().getUiCall());
        activity = ClassroomActivity.this;

        starttime = System.currentTimeMillis();
        activitylayout = findViewById(R.id.activitylayout);
        my_recyclerview_questions = findViewById(R.id.my_recyclerview_questions);
        pickfile = findViewById(R.id.pickfile);
        pickfile_pb = findViewById(R.id.pickfile_pb);
        questionLayout = findViewById(R.id.questionLayout);
        timertext = findViewById(R.id.timertext);
        nofiletext = findViewById(R.id.nofiletext);
        nofiletext_pb = findViewById(R.id.nofiletext_pb);
        pdfView = findViewById(R.id.pdfView);
        pdfView_pb = findViewById(R.id.pdfView_pb);
        ask = findViewById(R.id.ask);
        leaveclassview = findViewById(R.id.leaveclassview);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        hideBottomNavigationBar();
        classbluetoothview = findViewById(R.id.classbluetoothview);
        userImage = findViewById(R.id.view);

        playbacklayout = findViewById(R.id.playbacklayout);
        verifyuserlayout = findViewById(R.id.verifyuserlayout);
        classroomcalllayout = findViewById(R.id.classroomcalllayout);
        regularcalllayout = findViewById(R.id.regularcalllayout);
        groupcalllayout = findViewById(R.id.groupcalllayout);

        classspeakerview = findViewById(R.id.classspeakerview);

        classspeakerview_pb = findViewById(R.id.classspeakerview_pb);
        classbluetoothview_pb = findViewById(R.id.classbluetoothview_pb);
        endplaybackview = findViewById(R.id.endplaybackview);

        classspeakerview_gp = findViewById(R.id.classspeakerview_gp);
        leavegroupcallview = findViewById(R.id.leavegroupcallview);
        speakerimg = findViewById(R.id.speakerimg);
        speakerimg_gp = findViewById(R.id.speakerimg_gp);
        speakerimg_pb = findViewById(R.id.speakerimg_pb);

        classsbluetoothview = findViewById(R.id.classsbluetoothview);

        rewindIcon = findViewById(R.id.rewindIcon);
        playIcon = findViewById(R.id.playIcon);
        fastforwardIcon = findViewById(R.id.fastforwardIcon);

        callobjid_class = findViewById(R.id.callobjid_class);
        callobjid_pb = findViewById(R.id.callobjid_pb);
        callobjid_gp = findViewById(R.id.callobjid_gp);

        parentLayout = findViewById(R.id.parentLayout);

        pickfile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            try {
                startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                //alert user that file manager not working
                Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
            }
        });

        pickfile_pb.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            try {
                startActivityForResult(intent, FILE_PICKER_REQUEST_CODE_PB);
            } catch (ActivityNotFoundException e) {
                //alert user that file manager not working
                Toast.makeText(getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
            }
        });

        rewindIcon.setOnClickListener(v -> CallManager.get().playTone('*'));

        playIcon.setOnClickListener(v -> {
            Drawable currentDrawable = playIcon.getDrawable();

            Drawable playIconDrawable = getResources().getDrawable(R.drawable.play);
            Drawable pauseIconDrawable = getResources().getDrawable(R.drawable.pause);


            Drawable.ConstantState playIconConstantState = playIconDrawable.getConstantState();
            Drawable.ConstantState pauseIconConstantState = pauseIconDrawable.getConstantState();
            Drawable.ConstantState currentIconConstantState = currentDrawable.getConstantState();
            if (currentIconConstantState.equals(pauseIconConstantState)) {
                playIcon.setImageDrawable(playIconDrawable);
                CallManager.get().playTone('*');
                CallManager.get().playTone('3');
            } else {
                playIcon.setImageDrawable(pauseIconDrawable);
                CallManager.get().playTone('*');
                CallManager.get().playTone('2');
            }
        });

        fastforwardIcon.setOnClickListener(v -> CallManager.get().playTone('#'));

        ask.setOnClickListener(v -> {
            Toast.makeText(ClassroomActivity.this, getString(R.string.prompting_instructor), Toast.LENGTH_LONG).show();
            CallManager.get().playTone('9');
        });

        mHandler = new Handler(this);

        classbluetoothview.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();
        });
        classbluetoothview_pb.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();
        });

        classspeakerview.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // If you want to modify a view in your Activity
                    runOnUiThread(() -> {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        if (!audioManager.isSpeakerphoneOn()) {
                            audioManager.setSpeakerphoneOn(true);
                            speakerimg.setImageResource(R.drawable.speakeroff);
                        } else {
                            audioManager.setSpeakerphoneOn(false);
                            speakerimg.setImageResource(R.drawable.speaker);
                        }

                    });
                }
            }, 500);
        });
        classspeakerview_pb.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // If you want to modify a view in your Activity
                    runOnUiThread(() -> {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        if (!audioManager.isSpeakerphoneOn()) {
                            audioManager.setSpeakerphoneOn(true);
                            speakerimg_pb.setImageResource(R.drawable.speakeroff);
                        } else {
                            audioManager.setSpeakerphoneOn(false);
                            speakerimg_pb.setImageResource(R.drawable.speaker);
                        }

                    });
                }
            }, 500);
        });

        formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(formatAsYouType ? regionCode : "");

        leaveclassview.setOnClickListener(v -> {
            CallManager.mCurrentCall = CallManager.getSpecificCall(callobjid_class.getText().toString());
            CallManager.callArrayList.get(0).disconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ClassroomActivity.this.finish();
                }
            }, 500);
            if (CallManager.callArrayList.size() == 0) {
                if (notificationManager != null) {
                    notification = null;
                    notificationManager.cancelAll();
                    //notificationManager.deleteNotificationChannel(stringid);
                }
            }
        });

        leavegroupcallview.setOnClickListener(v -> {
            CallManager.mCurrentCall = CallManager.getSpecificCall(callobjid_gp.getText().toString());
            CallManager.callArrayList.get(0).disconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ClassroomActivity.this.finish();
                }
            }, 500);
            if (CallManager.callArrayList.size() == 0) {
                if (notificationManager != null) {
                    notification = null;
                    notificationManager.cancelAll();
                    //notificationManager.deleteNotificationChannel(stringid);
                }
            }
        });

        endplaybackview.setOnClickListener(v -> {
            CallManager.mCurrentCall = CallManager.getSpecificCall(callobjid_pb.getText().toString());
            CallManager.get().cancelCall();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ClassroomActivity.this.finish();
                }
            }, 500);
            if (CallManager.callArrayList.size() == 0) {
                if (notificationManager != null) {
                    notification = null;
                    notificationManager.cancelAll();
                    //notificationManager.deleteNotificationChannel(stringid);
                }
            }
        });

        // initBroadcasrReceiver();
        //   initAudioManager();

//500        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
//        registerReceiver(mBroadcastReceiver, intentFilter);

        /*Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmDialcode realmDialCode = realm.where(RealmDialcode.class).equalTo("dialcode", CallManager.get().getUiCall().getDisplayName().replace("+", "")).findFirst();
            if (realmDialCode != null && CALLMODE != VERIFICATIONCALL && CALLMODE != CLASSPLAYBACK) {
                CALLMODE = CLASSCALL;
                PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .edit()
                        .putBoolean(INCOMING_CALL, true)
                        .apply();
            }

        });*/

        parentLayout.setVisibility(View.GONE);
        if (CALLMODE == CLASSCALL && CallManager.getAll().size() == 1) {
            CLASSINSESSION = true;

            classroomcalllayout.setVisibility(View.VISIBLE);
            playbacklayout.setVisibility(View.GONE);
            groupcalllayout.setVisibility(View.GONE);
            verifyuserlayout.setVisibility(View.GONE);

            displayFile();
            setTitle(pdfFileName);

            String[] ids = CallManager.mCurrentCall.toString().split(",");
            String id = ids[0];
            callerid = getString(R.string.class_in_session);
            callobjid_class.setText(id);
            final RemoteViews contentview = new RemoteViews(getPackageName(), R.layout.notification);
            addNotification(id, callerid, contentview, false);
            timerHandler = new Handler();
            starttime = System.currentTimeMillis();
            final long finalStarttime = starttime;
            Runnable timerRunnable = new Runnable() {

                @Override
                public void run() {
                    CLASSINSESSION = true;
                    long millis = System.currentTimeMillis() - finalStarttime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    contentview.setTextViewText(R.id.timer, String.format("%d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 500);
                    if (ClassroomActivity.notificationManager != null && notification != null) {

                        String callid = callobjid_class.getText().toString().split(",")[0];
                        String[] id1s = callid.split("@");
                        String numberOnly = "";
                        if (id1s.length > 1) {
                            numberOnly = id1s[1].replaceAll("[^0-9]", "");
                        } else {
                            numberOnly = id1s[0].replaceAll("[^0-9]", "");
                        }

                        notid = Integer.parseInt(numberOnly);
                        Log.d("Coxydna1", String.valueOf(notid));
                        if (CallManager.getAll().size() > 0) {
                            //     ClassroomActivity.notificationManager.cancelAll();
                            ClassroomActivity.notificationManager.notify(notid, notification);
                        } else {
                            ClassroomActivity.notificationManager.cancel(notid);
                            timerHandler.removeCallbacksAndMessages(null);
                            timerHandler = null;
                            finish();
                        }
                    }
                    if ((CallManager.get().getUiCall().getStatus() == UiCall.Status.ACTIVE)) {
                        contentview.setViewVisibility(R.id.buttonAnswer, View.GONE);
                        //custom.findViewById(R.id.answerlayout).setVisibility(View.GONE);
                        /*if (notificationManager != null && notification != null) {
                            String[] ids = null;
                            String id = null;
                            if (CallManager.mCurrentCall.toString().contains(",")) {
                                ids = CallManager.mCurrentCall.toString().split(",");
                                id = ids[0];
                            } else {
                                id = CallManager.mCurrentCall.toString();
                            }
                            // String[] ids = CallManager.mCurrentCall.toString().split(",");

                            String[] id1s = id.split("@");
                            String numberOnly = "";
                            if (id1s.length > 1) {
                                numberOnly = id1s[1].replaceAll("[^0-9]", "");
                            } else {
                                numberOnly = id1s[0].replaceAll("[^0-9]", "");
                            }

                            notid = Integer.parseInt(numberOnly);
                            Log.d("Coxydna1", String.valueOf(notid));
                            notificationManager.notify(notid, notification);
                        }*/
                    }
                }
            };
            timerHandler.postDelayed(timerRunnable, 0);
            callobjid_class.setText(CallManager.mCurrentCall.toString());
        }
        else if (CALLMODE == CLASSPLAYBACK && CallManager.getAll().size() == 1)
        {
            CLASSINSESSION = true;

            playbacklayout.setVisibility(View.VISIBLE);
            classroomcalllayout.setVisibility(View.GONE);
            groupcalllayout.setVisibility(View.GONE);
            verifyuserlayout.setVisibility(View.GONE);

            displayFile();
            setTitle(pdfFileName);

            String[] ids = CallManager.mCurrentCall.toString().split(",");
            String id = ids[0];
            callerid = getString(R.string.playback_in_session);
            callobjid_pb.setText(id);
            final RemoteViews contentview = new RemoteViews(getPackageName(), R.layout.notification);
            addNotification(id, callerid, contentview, false);
            timerHandler = new Handler();
            starttime = System.currentTimeMillis();
            final long finalStarttime = starttime;
            Runnable timerRunnable = new Runnable() {

                @Override
                public void run() {
                    long millis = System.currentTimeMillis() - finalStarttime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    contentview.setTextViewText(R.id.timer, String.format("%d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 500);
                }
            };
            timerHandler.postDelayed(timerRunnable, 0);
            callobjid_pb.setText(CallManager.mCurrentCall.toString());
        } else if (CALLMODE == GROUPCALL && CallManager.getAll().size() == 1) {

            CLASSINSESSION = true;

            groupcalllayout.setVisibility(View.VISIBLE);
            playbacklayout.setVisibility(View.GONE);
            classroomcalllayout.setVisibility(View.GONE);
            verifyuserlayout.setVisibility(View.GONE);
            parentLayout.setVisibility(View.GONE);

            String[] ids = CallManager.mCurrentCall.toString().split(",");
            String id = ids[0];
            callerid = getString(R.string.group_call_in_session);
            callobjid_gp.setText(id);
            final RemoteViews contentview = new RemoteViews(getPackageName(), R.layout.notification);
            addNotification(id, callerid, contentview, false);
            timerHandler = new Handler();
            starttime = System.currentTimeMillis();
            final long finalStarttime = starttime;
            Runnable timerRunnable = new Runnable() {

                @Override
                public void run() {
                    long millis = System.currentTimeMillis() - finalStarttime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;
                    contentview.setTextViewText(R.id.timer, String.format("%d:%02d", minutes, seconds));
                    timerHandler.postDelayed(this, 500);
                }
            };
            timerHandler.postDelayed(timerRunnable, 0);
            callobjid_gp.setText(CallManager.mCurrentCall.toString());
        } else if (CALLMODE == VERIFICATIONCALL) {
            startAnimation();
            CLASSINSESSION = true;

            verifyuserlayout.setVisibility(View.VISIBLE);
            groupcalllayout.setVisibility(View.GONE);
            playbacklayout.setVisibility(View.GONE);
            classroomcalllayout.setVisibility(View.GONE);
            parentLayout.setVisibility(View.GONE);

//            starttimer_vf();
            startAnimation_verif();
        }
        else {
            parentLayout.setVisibility(View.VISIBLE);
            classroomcalllayout.setVisibility(View.GONE);
            playbacklayout.setVisibility(View.GONE);
            groupcalllayout.setVisibility(View.GONE);
            verifyuserlayout.setVisibility(View.GONE);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View custom = inflater.inflate(R.layout.normalcall, parentLayout, false);
            if (CallManager.getAll().size() == 1) {
                parentLayout.addView(custom);
            }
            setLocked(custom.findViewById(R.id.pauseimg));
            setLocked(custom.findViewById(R.id.muteimg));
            setLocked(custom.findViewById(R.id.addcallimg));
            startAnimation();
            View view = custom.findViewById(R.id.dialpad);
            final DialpadView dialpad1 = view.findViewById(R.id.dialpad_view);
            digits = (DigitsEditText) dialpad1.getDigits();
            dialpad1.setVisibility(View.GONE);
            final TextView detailsText = custom.findViewById(R.id.textDisplayName);
            final TextView callobjid = custom.findViewById(R.id.callobjid_class);
            String[] ids = CallManager.mCurrentCall.toString().split(",");
            String id = ids[0];
            callobjid.setText(id);
            if (CallManager.get().getSpecificCall(callobjid.getText().toString()) != null) {
                CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                        .getText().toString());
            }
            String displayName = CallManager.get().getUiCall().getDisplayName();
            if (displayName != null) {
                RoundedImageView imageView = custom.findViewById(R.id.view);

                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                             @Override
                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                 if (resultSet.isPermissionGranted(READ_CONTACTS) &&
                                                                         resultSet.isPermissionGranted(WRITE_CONTACTS)
                                                                 ) {
                                                                     detailsText.setText(getContactName(CallManager.get().getUiCall().getDisplayName(), getApplicationContext()));
                                                                     String uri = getContactsImage(CallManager.get().getUiCall().getDisplayName());
                                                                     if (uri != null && !uri.isEmpty()) {
                                                                         imageView.setImageBitmap(stringToBitmap(uri));
                                                                     }
                                                                 }
                                                             }

                                                             @Override
                                                             public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                 Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                             }
                                                         },
                        READ_CONTACTS,
                        WRITE_CONTACTS
                );


            }
            callerid = CallManager.get().getUiCall().getDisplayName();
            final TextView timerView = custom.findViewById(R.id.textDuration);
            final RemoteViews contentview = new RemoteViews(getPackageName(), R.layout.notification);

            notification = null;
            if (CallManager.get().getUiCall().getStatus() == UiCall.Status.CONNECTING || CallManager.get().getUiCall().getStatus() == UiCall.Status.DIALING) {
                custom.findViewById(R.id.answerlayout).setVisibility(View.GONE);
            }
            if (!CLASSINSESSION) {

            }


            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                RealmDialcode realmDialCode = realm.where(RealmDialcode.class).equalTo("dialcode", CallManager.get().getUiCall().getDisplayName().replace("+", "")).findFirst();
                boolean isSystemCall = realmDialCode != null;
                Log.d("Cyril", Boolean.toString(isSystemCall));
//                if (!isSystemCall) {
                    if (true) {
                    Boolean calldir;
                    if (CallManager.get().getUiCall().getStatus() == UiCall.Status.CONNECTING || CallManager.get().getUiCall().getStatus() == UiCall.Status.DIALING) {
                        contentview.setViewVisibility(R.id.buttonAnswer, View.GONE);
//                    custom.findViewById(R.id.answerlayout).setVisibility(View.GONE);
                        calldir = false;
                    } else
                    {
                        calldir = true;
                    }
                    if (notificationManager != null) {
                        Log.i("Bardist", String.valueOf(notid));
                        notificationManager.cancel(notid);
                    }
                    addNotification(id, callerid, contentview, calldir);
                }
            });


            timerHandler = new Handler();
            mEditor = getSharedPreferences(MY_TIMER, MODE_PRIVATE).edit();
            mEditor.putString("data", String.valueOf(starttime)).commit();

            final long finalStarttime = starttime;
            final TextView textStatus = custom.findViewById(R.id.textStatus);
            textStatus.setText(CallManager.get().getUiCall().getStatus().toString());
            final Runnable timerRunnable = new Runnable() {

                @Override
                public void run() {

                    if (CallManager.getAll().size() == 1) {
                        //     Log.d("Enochman", String.valueOf(contentview.g()));
                        CallManager.mCurrentCall = CallManager.getAll().get(0);
                        if (CallManager.mCurrentCall != null) {
                            textStatus.setText(CallManager.get().getUiCall().getStatus().toString());
                            if ((CallManager.get().getUiCall().getStatus() == UiCall.Status.ACTIVE)) {
                                long millis = System.currentTimeMillis() - starttime;
                                int seconds = (int) (millis / 1000);
                                int minutes = seconds / 60;
                                seconds = seconds % 60;
                                Log.d("Cyril", String.valueOf(CallManager.getAll().size()));
                                timerView.setText(String.format("%d:%02d", minutes, seconds));
                                contentview.setTextViewText(R.id.timer, String.format("%d:%02d", minutes, seconds));
                                contentview.setViewVisibility(R.id.buttonAnswer, View.GONE);
                                custom.findViewById(R.id.answerlayout).setVisibility(View.GONE);
                                if (notificationManager != null && notification != null) {
                                    String[] ids = null;
                                    String id = null;
                                    if (CallManager.mCurrentCall.toString().contains(",")) {
                                        ids = CallManager.mCurrentCall.toString().split(",");
                                        id = ids[0];
                                    } else {
                                        id = CallManager.mCurrentCall.toString();
                                    }
                                    // String[] ids = CallManager.mCurrentCall.toString().split(",");

                                    String[] id1s = id.split("@");
                                    String numberOnly = "";
                                    if (id1s.length > 1) {
                                        numberOnly = id1s[1].replaceAll("[^0-9]", "");
                                    } else {
                                        numberOnly = id1s[0].replaceAll("[^0-9]", "");
                                    }

                                    notid = Integer.parseInt(numberOnly);
                                    Log.d("Coxydna1", String.valueOf(notid));
                                    notificationManager.notify(notid, notification);
                                }
                            }


                        }
                    } else if (CallManager.getAll().size() > 1) {

                        for (int i = 0; i < CallManager.getAll().size(); i++) {
                            Call call = CallManager.getAll().get(i);

                            if (call != null) {
                                if ((call.getState() != Call.STATE_ACTIVE)) {
                                    long millis = System.currentTimeMillis() - finalStarttime;
                                    int seconds = (int) (millis / 1000);
                                    int minutes = seconds / 60;
                                    seconds = seconds % 60;
                                    timerView.setText(String.format("%d:%02d", minutes, seconds));

                                    if (notificationManager != null && notification != null) {
                                        String[] ids = call.toString().split(",");
                                        // String[] ids = CallManager.mCurrentCall.toString().split(",");
                                        String id = ids[0];
                                        Log.d("CallService23", id);
                                        String[] id1s = id.split("@");
                                        String numberOnly = "";
                                        if (id1s.length > 1) {
                                            numberOnly = id1s[1].replaceAll("[^0-9]", "");
                                        } else {
                                            numberOnly = id1s[0].replaceAll("[^0-9]", "");
                                        }
                                        notid = Integer.parseInt(numberOnly);
                                    }
                                }
                            }

                        }
                    } else if ((CallManager.getAll().size() == 0)) {
                        if (notificationManager != null) {
                            notificationManager.cancelAll();
                            notification = null;
                            notification = null;
                            notificationManager.cancelAll();
                        }
                        //   notificationManager.deleteNotificationChannel((String) callobjid.getText().toString());
                        // }
                        // if(timerHandler!=null)

                        timerHandler.removeCallbacksAndMessages(null);
                        //  timerHandler = null;
                        timerHandler.removeCallbacksAndMessages(this);
                        timerHandler = null;
                        finish();

                    }

                    if (CallManager.get().getUiCall().getStatus() == UiCall.Status.ACTIVE) {
//                        setUnlocked(custom.findViewById(R.id.addcallimg));
                        setUnlocked(custom.findViewById(R.id.pauseimg));
                        setUnlocked(custom.findViewById(R.id.muteimg));
                    }
                    if (timerHandler != null)
                        timerHandler.postDelayed(this, 500);
                }
            };
            timerHandler.postDelayed(timerRunnable, 0);


            dialpad1.findViewById(R.id.zero).setOnClickListener(view1 -> {
                append('0');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('0');

            });
            dialpad1.findViewById(R.id.zero).setOnLongClickListener(view12 -> {
                append('+');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('+');
                return true;
            });
            dialpad1.findViewById(R.id.one).setOnClickListener(view13 -> {
                append('1');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('1');
            });
            dialpad1.findViewById(R.id.two).setOnClickListener(view14 -> {
                append('2');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('2');
            });
            dialpad1.findViewById(R.id.three).setOnClickListener(view15 -> {
                append('3');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('3');
            });
            dialpad1.findViewById(R.id.four).setOnClickListener(view16 -> {
                append('4');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('4');
            });

            dialpad1.findViewById(R.id.five).setOnClickListener(view17 -> {
                append('5');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('5');
            });
            dialpad1.findViewById(R.id.six).setOnClickListener(view18 -> {
                append('6');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('6');
            });
            dialpad1.findViewById(R.id.seven).setOnClickListener(view19 -> {
                append('7');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('7');
            });
            dialpad1.findViewById(R.id.eight).setOnClickListener(view110 -> {
                append('8');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('8');
            });
            dialpad1.findViewById(R.id.nine).setOnClickListener(view111 -> {
                append('9');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('9');
            });
            // if (enableStar) {
            dialpad1.findViewById(R.id.star).setOnClickListener(view112 -> {
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                append('*');
                CallManager.get().playTone('*');
            });

            // if (enablePound) {
            dialpad1.findViewById(R.id.pound).setOnClickListener(view113 -> {
                append('#');
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());

                }
                CallManager.get().playTone('#');
            });
            dialpad1.getDeleteButton().setOnClickListener(view114 -> poll());
            dialpad1.getDeleteButton().setOnLongClickListener(view115 -> {
                clear();
                return true;
            });

            // if region code is null, no formatting is performed
            formatter = PhoneNumberUtil.getInstance()
                    .getAsYouTypeFormatter(formatAsYouType ? regionCode : "");

            // }
            //setContentView(parent);
            try {
                field = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
            } catch (Throwable ignored) {
            }
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(field, getLocalClassName());
            // This activity needs to show even if the screen is off or locked
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (km != null) {
                    km.requestDismissKeyguard(this, null);
                }
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (EasyPermissions.hasPermissions(this, WRITE_CONTACTS) && EasyPermissions.hasPermissions(this, READ_CONTACTS)) {
                updateView(CallManager.get().getUiCall());


            } else {
                //If permission is not present request for the same.
                EasyPermissions.requestPermissions(this, getString(R.string.read_file), READ_REQUEST_CODE, WRITE_CONTACTS);
                EasyPermissions.requestPermissions(this, getString(R.string.read_file), READ_REQUEST_CODE, READ_CONTACTS);
            }


            custom.findViewById(R.id.buttonHangup).setOnClickListener(view116 -> {
                Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                view116.startAnimation(animation1);
                if (CallManager.callArrayList.size() == 1) {
                    releaseWakeLock();
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                }
                String[] ids1 = callobjid
                        .getText().toString().split("@");
                ;
                String numberOnly;
                if (ids1.length > 1) {
                    numberOnly = ids1[1].replaceAll("[^0-9]", "");

                } else {
                    numberOnly = ids1[0].replaceAll("[^0-9]", "");
                }
                notificationManager.cancel(Integer.parseInt(numberOnly));
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());
                    CallManager.get().cancelCall();
                }
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if(CallManager.get().getSpecificCall(callobjid
//                            .getText().toString())!=null) {
//                        CallManager.get().getSpecificCall(callobjid
//                                .getText().toString()).disconnect();
//                    }
                // }
                new Handler().postDelayed(() -> finish(), 1000);
            });
            custom.findViewById(R.id.buttonAnswer).setOnClickListener(view117 -> {
                Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                view117.startAnimation(animation1);
                if (CallManager.get().getSpecificCall(callobjid
                        .getText().toString()) != null) {
                    CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid
                            .getText().toString());
                    CallManager.get().acceptCall();
                }
//                    if(CallManager.getSpecificCall(callobjid.getText().toString())!=null) {
//                        CallManager.getSpecificCall(callobjid.getText().toString()).answer(0);
//                    }
                custom.findViewById(R.id.answerlayout).setVisibility(View.GONE);
            });
            custom.findViewById(R.id.bluetoothview).setOnClickListener(view118 -> {
                view118.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                audioManager.setBluetoothScoOn(true);
                audioManager.startBluetoothSco();

            });
            custom.findViewById(R.id.speakerview).setOnClickListener(view119 -> {
                view119.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
                Timer myTimer = new Timer();
                final ImageView imageView = custom.findViewById(R.id.callspeakerimg);
                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // If you want to modify a view in your Activity
                        ClassroomActivity.this.runOnUiThread(() -> {
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            if (!audioManager.isSpeakerphoneOn()) {
                                audioManager.setSpeakerphoneOn(true);
                                imageView.setImageResource(R.drawable.speakeroff);
                            } else {
                                audioManager.setSpeakerphoneOn(false);
                                imageView.setImageResource(R.drawable.speaker);
                            }

                        });
                    }
                }, 500);
            });
            custom.findViewById(R.id.dialpadbtn).setOnClickListener(view120 -> {
                //  custom.findViewById(R.id.helperview).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
                //  custom.findViewById(R.id.helperview).setVisibility(View.GONE);
                Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                view120.startAnimation(animation1);
                custom.findViewById(R.id.dialpadbtn).startAnimation(animation1);
                if (dialpad1.getVisibility() == View.GONE) {
                    dialpad1.setVisibility(View.VISIBLE);
                    dialpad1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
                    //helperview.setVisibility(View.VISIBLE);
                    // helperview.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
                } else {
                    dialpad1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
                    dialpad1.setVisibility(View.GONE);

                }
            });
            custom.findViewById(R.id.addcallview).setOnClickListener(view121 -> {
                ImageView addcallimg = custom.findViewById(R.id.addcallimg);
                if (addcallimg.getImageAlpha() == 255) {
                    Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                    custom.findViewById(R.id.addcallview).startAnimation(animation1);
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    startActivity(intent);
                }
            });
            custom.findViewById(R.id.pausebtn).setOnClickListener(view122 -> {
                ImageView pauseimg = custom.findViewById(R.id.pauseimg);
                if (pauseimg.getImageAlpha() == 255) {
                    TextView textView = custom.findViewById(R.id.pausetext);
                    Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                    custom.findViewById(R.id.pausebtn).startAnimation(animation1);
                    if (!isCallpaused) {
                        isCallpaused = true;
                        if (CallManager.get().getSpecificCall(callobjid.getText().toString()) != null) {
                            CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid.getText().toString());
                            CallManager.get().pauseCall();
                        }

                        pauseimg.setImageResource(R.drawable.play);
                        textView.setText(getString(R.string.resume_call));
                        Toast.makeText(ClassroomActivity.this, getString(R.string.on_hold), Toast.LENGTH_SHORT).show();
                    } else {
                        isCallpaused = false;
                        textView.setText(getString(R.string.hold_call));
                        pauseimg.setImageResource(R.drawable.pause);
                        if (CallManager.get().getSpecificCall(callobjid.getText().toString()) != null) {
                            CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid.getText().toString());
                            CallManager.get().unPauseCall();
                        }
                    }
                }
            });
            custom.findViewById(R.id.muteview).setOnClickListener(view123 -> {
                ImageView muteimg = custom.findViewById(R.id.muteimg);
                if (muteimg.getImageAlpha() == 255) {
                    view123.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    TextView textView = custom.findViewById(R.id.mutetext);
                    if (!isMute) {
                        isMute = true;
                        muteimg.setImageResource(R.drawable.unmute);
                        audioManager.setMicrophoneMute(true);
                        textView.setText(getString(R.string.unmute_call));
                        Toast.makeText(ClassroomActivity.this, getString(R.string.call_muted), Toast.LENGTH_SHORT).show();

                    } else {
                        isMute = false;
                        audioManager.setMicrophoneMute(false);
                        muteimg.setImageResource(R.drawable.mute1);
                        Toast.makeText(ClassroomActivity.this, getString(R.string.mute_off), Toast.LENGTH_SHORT).show();
                        textView.setText(getString(R.string.mute_call));
                    }
                }
            });
        }

        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        recyclerview = findViewById(R.id.recyclerview);
        loadimg = findViewById(R.id.loadimg);
        resultnumber = findViewById(R.id.resultnumber);
        reloadbtn = findViewById(R.id.reloadbtn);
        reloadbtn = findViewById(R.id.reloadbtn);
        retrybtn = findViewById(R.id.retrybtn);
        newcreatecourselayout = findViewById(R.id.newcreatecourselayout);
        error_loading = findViewById(R.id.error_loading);
        phonenumberArrayList = new ArrayList<>();
        /*if (CALLMODE == GROUPCALL) {
            getCallStudents();
            CLASSINSESSION = true;
            final Handler ha = new Handler();
            ha.postDelayed(new Runnable() {
                public void run() {
                    getCallStudents();
                    getQuestionnotify();
                    ha.postDelayed(this, 10000);
                }
            }, 10000);
            reloadbtn.setOnClickListener(view -> {
                Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
                view.startAnimation(animation1);
                // shimmer_view_container.setVisibility(View.VISIBLE);
                //  recyclerview.setVisibility(View.GONE);
                // shimmer_view_container.startShimmerAnimation();
                getCallStudents();
            });
        }*/
        searchbtn = findViewById(R.id.searchbtn);
        searchbtn.setOnClickListener(view -> {
            Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);
            view.startAnimation(animation1);
        });
        menubtn = findViewById(R.id.menubtn);
        menubtn.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(menubtn.getContext(), menubtn);
            final ClassRoomDialog custom = new ClassRoomDialog();
            popup.inflate(R.menu.group_menu);

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.lockclass) {
                    custom.putdata("lockclass", "");
                    custom.show(getParent().getFragmentManager(), "");
                    // mContext.startActivity(new Intent(mContext, com.power.aubyn.mediasurveil.StatisticsActivity.class));
                    return true;
                } else if (itemId == R.id.inviteclass) {
                    custom.putdata("inviteclass", "");
                    custom.show(getParent().getFragmentManager(), "");
                    // mContext.startActivity(new Intent(mContext, com.power.aubyn.mediasurveil.StatisticsActivity.class));
                    return true;
                } else if (itemId == R.id.unlockstudent) {
                    custom.putdata("unlockstudent", "");
                    custom.show(getParent().getFragmentManager(), "");
                    // mContext.startActivity(new Intent(mContext, com.power.aubyn.mediasurveil.StatisticsActivity.class));
                    return true;
                }
                return false;
            });

            popup.show();
        });
        totalnumberView = findViewById(R.id.totalnumber);
        Glide.with(getApplicationContext()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(loadimg);

        loadimg.setVisibility(View.GONE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        classsbluetoothview.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();
        });
        classspeakerview_gp.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click));
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // If you want to modify a view in your Activity
                    runOnUiThread(() -> {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        if (!audioManager.isSpeakerphoneOn()) {
                            audioManager.setSpeakerphoneOn(true);
                            //  speakerimg.setColorFilter(R.color.blue);
                            speakerimg_gp.setImageResource(R.drawable.speakeroff);
                        } else {
                            audioManager.setSpeakerphoneOn(false);
                            speakerimg_gp.setImageResource(R.drawable.speaker);
                        }

                    });
                }
            }, 500);
        });
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                READ_EXTERNAL_STORAGE);

        CALLMODE = REGULARCALL;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CallManager.get().registerListener(this);

        /*Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmDialcode realmDialCode = realm.where(RealmDialcode.class).equalTo("dialcode", CallManager.get().getUiCall().getDisplayName().replace("+", "")).findFirst();
            boolean isIncomingCall = calldirection == getString(R.string.incoming);
            if (!isAcceptCallAlertDialogShowing() && isIncomingCall && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(INCOMING_CALL, false)) {

                acceptCallAlertDialog = new AlertDialog.Builder(ClassroomActivity.this)
                        .setTitle(getString(R.string.instructor_calling))
                        .setMessage(getString(R.string.accept_invitation_to_enter_live_class))

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(ClassroomActivity.this.getString(R.string.accept), (dialog, which) -> {
                            if (CallManager.get().getSpecificCall(callobjid_class
                                    .getText().toString()) != null) {
                                CallManager.mCurrentCall = CallManager.get().getSpecificCall(callobjid_class
                                        .getText().toString());
                                CallManager.get().acceptCall();
                                PreferenceManager
                                        .getDefaultSharedPreferences(getApplicationContext())
                                        .edit()
                                        .putBoolean(INCOMING_CALL, false)
                                        .apply();
                            }
                        })
                        .setNegativeButton(ClassroomActivity.this.getString(R.string.reject), (dialog, which) -> {
                            CallManager.mCurrentCall = CallManager.getSpecificCall(callobjid_class.getText().toString());

                            CallManager.get().cancelCall();
                            new Handler().postDelayed(() -> finish(), 500);
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
//                                        .setNegativeButton(android.R.string.no, null)
//                                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });*/
    }

    private boolean isAcceptCallAlertDialogShowing() {
        if (acceptCallAlertDialog == null){
            return false;
        }
        return acceptCallAlertDialog.isShowing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (CallManager.getNumOfInstances() == 1) {
            CallManager.get().unregisterListener();
        }
        releaseWakeLock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("engineer", "onDestroy");
        if (CallManager.getAll().size() == 1) {
            CallManager.get().unregisterListener();
        }

        releaseWakeLock();

        stopAnimation_verif();
        stopAnimation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_PICKER_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        uri = data.getData();
                        Log.d("engineer", uri.toString());
                        displayFromUri(uri);
                        pdfView.setVisibility(View.VISIBLE);
                        nofiletext.setVisibility(View.GONE);
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                }
                break;
            case FILE_PICKER_REQUEST_CODE_PB:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        uri = data.getData();
                        Log.d("engineer", uri.toString());
                        displayFromUri_pb(uri);
                        pdfView_pb.setVisibility(View.VISIBLE);
                        nofiletext_pb.setVisibility(View.GONE);
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                }
                break;


            default:
                break;

        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_ELAPSEDTIME:
                //  mTextDuration.setText(toDurationString(mElapsedTime));
                //  contentView.setTextViewText(R.id.timer, toDurationString(mElapsedTime));
//'                notificationManager.notify(NotificationID, notification);
                break;
        }
        return true;
    }

    @Override
    public void onCallStateChanged(UiCall call) {

        updateView(call);
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = null;
        if (CALLMODE == CLASSCALL) {
            meta = pdfView.getDocumentMeta();
            printBookmarksTree(pdfView.getTableOfContents(), "-");
        } else if (CALLMODE == CLASSPLAYBACK) {
            meta = pdfView_pb.getDocumentMeta();
            printBookmarksTree(pdfView_pb.getTableOfContents(), "-");
        }

        if (meta != null) {
            Log.e(TAG, "title = " + meta.getTitle());
            Log.e(TAG, "author = " + meta.getAuthor());
            Log.e(TAG, "subject = " + meta.getSubject());
            Log.e(TAG, "keywords = " + meta.getKeywords());
            Log.e(TAG, "creator = " + meta.getCreator());
            Log.e(TAG, "producer = " + meta.getProducer());
            Log.e(TAG, "creationDate = " + meta.getCreationDate());
            Log.e(TAG, "modDate = " + meta.getModDate());
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));

        /*currentTime = System.currentTimeMillis();
        timeDif = currentTime - prevTime;
        prevTime = currentTime;
        if (page != 0 && timeDif < 5000) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(this, "You are scrolling too fast.", Toast.LENGTH_SHORT);
            toast.show();
        }*/
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.d("engineer", "onPageError");
        Log.e(TAG, "Cannot load offset " + page);
    }

    private void displayFile() {
        if (classFile == null) {
            if (CALLMODE == CLASSCALL) {
                pdfView.setVisibility(View.GONE);
                nofiletext.setVisibility(View.VISIBLE);
            } else if (CALLMODE == CLASSPLAYBACK) {
                pdfView_pb.setVisibility(View.GONE);
                nofiletext_pb.setVisibility(View.VISIBLE);
            }
        } else {
            if (CALLMODE == CLASSCALL) {
                pdfView.setVisibility(View.VISIBLE);
                nofiletext.setVisibility(View.GONE);
            } else if (CALLMODE == CLASSPLAYBACK) {
                pdfView_pb.setVisibility(View.VISIBLE);
                nofiletext_pb.setVisibility(View.GONE);
            }
            displayPDF();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void updateView(UiCall uiCall) {
        Log.d("Cyril", "Number of call instance " + String.valueOf(CallManager.getAll().size()));
        if (CallManager.getAll().size() > 1) {
            CallManager.get().cancelCall();
        }
        if (CLASSINSESSION) {

        }

        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmDialcode realmDialCode = realm.where(RealmDialcode.class).equalTo("dialcode", CallManager.get().getUiCall().getDisplayName().replace("+", "")).findFirst();
            boolean isSystemCall = realmDialCode != null;
            Log.d("Cyril", Boolean.toString(isSystemCall));
            if (CLASSINSESSION) {
                if (uiCall.getStatus() == UiCall.Status.ACTIVE) {
                    String room_number = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(ROOMID, "");
                    Log.d("cyril character c:", room_number);
                    for (int i = 0; i < room_number.length(); i++) {
                        char c = room_number.charAt(i);
                        Log.d("cyril character c:", String.valueOf(c));
                        CallManager.get().playTone(c);
                    }
                    char c = '#';
                    Log.d("cyril character c:", String.valueOf(c));
                    CallManager.get().playTone(c);
                    PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putString(ROOMID, "")
                            .apply();
                }
            }
        });

        if (uiCall.getStatus() == UiCall.Status.DISCONNECTED) {
            stopTimer();

            if (acceptCallAlertDialog != null) {
                acceptCallAlertDialog.dismiss();
            }
            CALLMODE = REGULARCALL;
            CLASSINSESSION = false;
            PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean(INCOMING_CALL, false)
                    .apply();

            if (getAuthActivityContext != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finishAffinity();
                }
            }

            releaseWakeLock();

            /*getInitialCallstate(uiCall);

            if (notificationManager != null) {

                notificationManager.cancelAll();
            }

            if (CALLMODE == REGULARCALL) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finishAffinity();
                }
            }

            releaseWakeLock();
            if (notificationManager != null) {
                String[] ids = null;
                String id = null;
                if (CallManager.mCurrentCall.toString().contains(",")) {
                    ids = CallManager.mCurrentCall.toString().split(",");
                    id = ids[0];
                } else {
                    id = CallManager.mCurrentCall.toString();
                }

                String[] id1s = id.split("@");
                String numberOnly = "";
                if (id1s.length > 1) {
                    numberOnly = id1s[1].replaceAll("[^0-9]", "");
                } else {
                    numberOnly = id1s[0].replaceAll("[^0-9]", "");
                }

                notid = Integer.parseInt(numberOnly);

                notificationManager.cancel(notid);
                if (CallManager.callArrayList.size() <= 1) {
                    notificationManager.cancelAll();
                }
            }

            new Handler().postDelayed(() -> finish(), 3000);*/
        }

        if (uiCall.getStatus() == UiCall.Status.ACTIVE) {
            callsuccessfulyplaced = true;
            startTimer();
            starttime = System.currentTimeMillis();
            acquireWakeLock();

            if (CALLMODE == CLASSCALL && CallManager.getAll().size() == 1) {
//                classroomcalllayout.setBackgroundResource(R.color.active_background);
            } else if (CALLMODE == CLASSPLAYBACK && CallManager.getAll().size() == 1) {
//                playbacklayout.setBackgroundResource(R.color.active_background);
            } else if (CALLMODE == GROUPCALL && CallManager.getAll().size() == 1) {
//                groupcalllayout.setBackgroundResource(R.color.active_background);
            } else if (CALLMODE == REGULARCALL && CallManager.getAll().size() == 1) {

            }
        }
    }

    public String getContactsImage(String address) {
        Bitmap bp = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.rounded_corner_gradient);
        String image_uri = "";
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
//        ContentResolver cR = getApplicationContext().getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        String type = mime.getExtensionFromMimeType(cR.getType(contactUri));
//        Log.d("Obeng",type);
        // querying contact data store
        if (EasyPermissions.hasPermissions(this, WRITE_CONTACTS) && EasyPermissions.hasPermissions(this, READ_CONTACTS)) {
            Cursor phones = getApplicationContext().getContentResolver().query(contactUri, null, null, null, null);


            while (phones.moveToNext()) {
                image_uri = phones.getString(phones.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI));


            }


        } else {
            //If permission is not present request for the same.
            EasyPermissions.requestPermissions(this, getString(R.string.read_file), READ_REQUEST_CODE, WRITE_CONTACTS);
            EasyPermissions.requestPermissions(this, getString(R.string.read_file), READ_REQUEST_CODE, READ_CONTACTS);
        }


        return image_uri;

    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};


        String contactName = phoneNumber;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }


        return contactName;
    }

    private Bitmap stringToBitmap(String uri) {
        Bitmap bp = null;


        try {
            bp = MediaStore.Images.Media
                    .getBitmap(getApplicationContext().getContentResolver(),
                            Uri.parse(uri));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bp;
    }

    private void hideBottomNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void poll() {
        if (!input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode);
            if (formatAsYouType) {
                digits.setText("");
                for (char c : input.toCharArray()) {
                    digits.setText(formatter.inputDigit(c));
                }
            } else {
                digits.setText(input);
            }
        }
    }

    private void clear() {
        formatter.clear();
        digits.setText("");
        input = "";
    }

    private void append(char c) {
        CallManager.get().playTone(c);
        input += c;
        if (formatAsYouType) {
            digits.setText(formatter.inputDigit(c));
        } else {
            digits.setText(input);
        }
    }

    private void getInitialCallstate(UiCall uiCall) {
        Log.d("Obenga", uiCall.getStatus().toString());
        if (CallManager.getAll().size() > 1) {
            CallManager.get().cancelCall();
        }
        if ((uiCall.getStatus() != UiCall.Status.RINGING) && (uiCall.getStatus() != UiCall.Status.CONNECTING) && (uiCall.getStatus() != UiCall.Status.ACTIVE) && (uiCall.getStatus() != UiCall.Status.DIALING)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
//                this.finishAffinity();
            }
        } else if (uiCall.getStatus() == UiCall.Status.DIALING) {
            calldirection = getString(R.string.outgoing);
            Log.d("Obeng", calldirection);
        } else if (uiCall.getStatus() == UiCall.Status.RINGING) {
            calldirection = getString(R.string.incoming);
            String phone = uiCall.getDisplayName();
            Log.d("Obeng", calldirection);
        }

    }

    private void startTimer() {
        stopTimer();

        mElapsedTime = 0L;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mElapsedTime++;
                mHandler.sendEmptyMessage(MSG_UPDATE_ELAPSEDTIME);
            }
        }, 0, PERIOD_MILLIS);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String date_time = simpleDateFormat.format(calendar.getTime());
        mEditor = getSharedPreferences(MY_TIMER, MODE_PRIVATE).edit();
        mEditor.putString("data", date_time).commit();
        mEditor.putString("hours", date_time).commit();


        Intent intent_service = new Intent(getApplicationContext(), Time_Service.class);
        startService(intent_service);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void displayPDF() {
        Uri uri = Uri.fromFile(classFile);
        pdfFileName = getFileName(uri);

        if (CALLMODE == CLASSCALL) {
            pdfView.fromUri(uri)
                    .defaultPage(pageNumber)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10) // in dp
                    .onPageError(this)
                    .nightMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(ISNIGHTMODE, false))
                    .load();
        } else if (CALLMODE == CLASSPLAYBACK) {
            pdfView_pb.fromUri(uri)
                    .defaultPage(pageNumber)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10) // in dp
                    .onPageError(this)
                    .nightMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(ISNIGHTMODE, false))
                    .load();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    private static void releaseWakeLock() {
        if (CALLMODE == REGULARCALL) {
            if ((wakeLock != null) && (powerManager != null)) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }
    }

    private void acquireWakeLock() {
        if (CALLMODE == REGULARCALL) {
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(field, getLocalClassName());
            if (!wakeLock.isHeld()) {
                wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
            }
        }
    }

    private void starttimer_vf() {
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                timertext.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                VERIFICATIONCALLTIMERSTOPED = true;
                finish();
            }
        }.start();
    }

    public static class CallCancelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //CallManager.get().getUiCall()
            // Log.d("Coxy","heibei fortune");
            // System.out.println("Call Cancelled Event");
            //  int id  = intent.getIntExtra("notid",0);
            // Log.d("bnotid",String.valueOf(id));
            // String stringid = intent.getStringExtra("stringid");
            if (CallManager.callArrayList.size() == 1) {
                releaseWakeLock();
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
            Log.d("Coxydna", String.valueOf(CallManager.callArrayList.size()));
            //   notificationManager.cancel(id);
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (CallManager.get().getSpecificCall(String.valueOf(notid)) != null) {
                CallManager.mCurrentCall = CallManager.get().getSpecificCall(
                        String.valueOf(notid));
                CallManager.get().cancelCall();
            }
            if (notificationManager != null)
                notificationManager.cancel(notid);
            if (CallManager.callArrayList.size() == 0) {
                if (notificationManager != null) {
                    notification = null;
                    notificationManager.cancelAll();
                    //notificationManager.deleteNotificationChannel(stringid);
                }
            }
            // }
            new Handler().postDelayed(() -> {
                if (activity != null)
                    activity.finish();
            }, 500);


        }
    }

    public static class CallAcceptReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("notid", 0);
            if (CallManager.get().getSpecificCall(String.valueOf(id)) != null) {
                CallManager.mCurrentCall = CallManager.get().getSpecificCall(
                        String.valueOf(id));
                CallManager.get().acceptCall();
                if (acceptCallAlertDialog != null) {
                    acceptCallAlertDialog.dismiss();
                    PreferenceManager
                            .getDefaultSharedPreferences(context)
                            .edit()
                            .putBoolean(INCOMING_CALL, false)
                            .apply();
                }
            }
        }
    }

    private void addNotification(String id, String callerid, RemoteViews contentView, Boolean hideclosebtn) {
        String[] ids = id.split("@");
        String numberOnly = "";
        if (ids.length > 1) {
            numberOnly = ids[1].replaceAll("[^0-9]", "");
        } else {
            numberOnly = ids[0].replaceAll("[^0-9]", "");
        }
        notid = Integer.parseInt(numberOnly);
        if (hideclosebtn) {
            contentView.setViewVisibility(R.id.buttonAnswer, View.VISIBLE);
        } else {
            contentView.setViewVisibility(R.id.buttonAnswer, View.GONE);
        }
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), id);
        mBuilder.setSound(null);

        // contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);

        if ((CallManager.get().getUiCall().getStatus() == UiCall.Status.RINGING)) {
            contentView.setViewVisibility(R.id.buttonAnswer, View.VISIBLE);
        } else {
            contentView.setViewVisibility(R.id.buttonAnswer, View.GONE);
        }
        Intent closeButton = new Intent(getApplicationContext(), CallCancelReceiver.class);
        closeButton.putExtra("notid", notid);
        closeButton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(this, 0, closeButton, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.buttonHangup, pendingCancelIntent);


        Intent acceptIntent = new Intent(getApplicationContext(), CallAcceptReceiver.class);
        acceptIntent.putExtra("notid", notid);
        PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(this, 1020, acceptIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.buttonAnswer, pendingAcceptIntent);

        //contentView.setOnClickFillInIntent(R.id.buttonHangup, pendingCancelIntent);
        Intent openIntent = null;
//        if (isActivityFound) {
//            openIntent = new Intent();
//        } else {
        openIntent = new Intent(this, ClassroomActivity.class);
        //  openIntent.putExtra("Title", the_title);
        //    openIntent.putExtra("Message", the_message);
        openIntent.setAction(Long.toString(System.currentTimeMillis()));
        /// }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                openIntent, PendingIntent.FLAG_ONE_SHOT);
        //contentView.setOnClickFillInIntent(R.id.buttonHangup, pendingCancelIntent);
        mBuilder.setSmallIcon(R.drawable.logo);
        // mBuilder.setAutoCancel(true);

        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                     @Override
                                                     public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                         if (resultSet.isPermissionGranted(READ_CONTACTS) &&
                                                                 resultSet.isPermissionGranted(WRITE_CONTACTS)
                                                         ) {
                                                             contentView.setTextViewText(R.id.text, getContactName(CallManager.get().getUiCall().getDisplayName(), getApplicationContext()));
                                                             String uri = getContactsImage(CallManager.get().getUiCall().getDisplayName());
                                                             if (uri != null && !uri.isEmpty()) {
                                                                 contentView.setImageViewBitmap(R.id.image, stringToBitmap(uri));
                                                             }
                                                         } else {
                                                             contentView.setTextViewText(R.id.text, callerid);
                                                         }
                                                     }

                                                     @Override
                                                     public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                         Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                     }
                                                 },
                READ_CONTACTS,
                WRITE_CONTACTS
        );


        mBuilder.setOngoing(true);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        // mBuilder.setOnlyAlertOnce(true);
        mBuilder.setAutoCancel(true);
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(contentIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = NOTIFICATION_CHANNEL_ID;
            NotificationChannel channel = new NotificationChannel(id, "channel name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            //  channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(id);
        }

        notification = mBuilder.build();
        // notification.defaults = 0;

        notificationManager.notify(notid, notification);
    }

    private ArrayList<Participant> filterphone(ArrayList<Participant> models, ArrayList<String> search_txt) {


        final ArrayList<Participant> filteredModelList = new ArrayList<>();

        for (int i = 0; i < search_txt.size(); i++) {
            String qry = search_txt.get(i);
            Log.i("bbbb", "Search for" + qry);
            Log.i("bbbb", participantArrayList.toString());
            for (Participant model : models) {
                final String text = model.getPhone_number().toLowerCase();
                Log.i("bbbb", "Existing" + text);

                if (text.contains(qry)) {
                    filteredModelList.add(model);
                }
            }
        }
        // search_txt = search_txt.toLowerCase();

        return filteredModelList;
    }

    public static void startAnimation() {
        final int start = Color.parseColor("#E63A4E59");
        final int mid = Color.parseColor("#808080");
        final int end = Color.parseColor("#808080");


        final ArgbEvaluator evaluator = new ArgbEvaluator();
//        View preloader = activity.findViewById(R.id.gradientPreloaderView);
//        preloader.setVisibility(View.VISIBLE);
        final GradientDrawable gradient = (GradientDrawable) activitylayout.getBackground();

        animator = TimeAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(valueAnimator -> {
            Float fraction = valueAnimator.getAnimatedFraction();
            int newStrat = (int) evaluator.evaluate(fraction, start, end);
            int newMid = (int) evaluator.evaluate(fraction, mid, start);
            int newEnd = (int) evaluator.evaluate(fraction, end, mid);
            int[] newArray = {newStrat, newMid, newEnd};
            gradient.setColors(newArray);
        });

        animator.start();
    }

    public static void startAnimation_verif() {
        final int start = Color.parseColor("#EB4F12");
        final int mid = Color.parseColor("#FFC708");
        final int end = Color.parseColor("#FFC708");


        final ArgbEvaluator evaluator = new ArgbEvaluator();
//        View preloader = activity.findViewById(R.id.gradientPreloaderView);
//        preloader.setVisibility(View.VISIBLE);
        final GradientDrawable gradient = (GradientDrawable) verifyuserlayout.getBackground();

        animator_verif = TimeAnimator.ofFloat(0.0f, 1.0f);
        animator_verif.setDuration(500);
        animator_verif.setRepeatCount(ValueAnimator.INFINITE);
        animator_verif.setRepeatMode(ValueAnimator.REVERSE);
        animator_verif.addUpdateListener(valueAnimator -> {
            Float fraction = valueAnimator.getAnimatedFraction();
            int newStrat = (int) evaluator.evaluate(fraction, start, end);
            int newMid = (int) evaluator.evaluate(fraction, mid, start);
            int newEnd = (int) evaluator.evaluate(fraction, end, mid);
            int[] newArray = {newStrat, newMid, newEnd};
            gradient.setColors(newArray);
        });

        animator_verif.start();
    }

    public static void stopAnimation() {
        if (animator != null)
            animator.cancel();
    }

    public static void stopAnimation_verif() {
        if (animator_verif != null)
            animator_verif.cancel();
    }

    public static void setLocked(ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
        v.setImageAlpha(128);   // 128 = 0.5
    }

    public static void setUnlocked(ImageView v) {
        v.setColorFilter(null);
        v.setImageAlpha(255);
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    private void displayFromUri_pb(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView_pb.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }
}
