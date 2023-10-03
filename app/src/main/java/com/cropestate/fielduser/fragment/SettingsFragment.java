package com.cropestate.fielduser.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.greysonparrelli.permiso.Permiso;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.AccountActivity;
import com.cropestate.fielduser.activity.AssignmentActivity;
import com.cropestate.fielduser.activity.AttendanceActivity;
import com.cropestate.fielduser.activity.GetPhoneNumberActivity;
import com.cropestate.fielduser.activity.HelpActivity;
import com.cropestate.fielduser.activity.PaymentActivity;
import com.cropestate.fielduser.activity.TimetableActivity;
import com.cropestate.fielduser.util.RealmUtility;

import io.realm.Realm;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.MANAGE_OWN_CALLS;
import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.activity.HomeActivity.getDefaultDialerPackage;
import static com.cropestate.fielduser.constants.Const.changeDefaultDialer;
import static com.cropestate.fielduser.constants.Const.getPackagesOfDialerApps;


/**
 * Created by Nana on 11/26/2017.
 */

public class SettingsFragment extends Fragment {

    public static final String ISNIGHTMODE = "ISNIGHTMODE";

    CardView attendancebtn, homeworkbtn, paymentsbtn, website, timetablebtn, profilebtn, displaybtn, permission,apppermission, logout, groups, faqs;
    LinearLayout detailnightmode;
    ImageView displayright;
    Switch aSwitch;
    public static Callbacks mCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settingsfrag, container, false);
        timetablebtn = rootView.findViewById(R.id.timetablebtn);
        homeworkbtn = rootView.findViewById(R.id.homeworkbtn);
        groups = rootView.findViewById(R.id.groups);
        permission = rootView.findViewById(R.id.permission);
        apppermission = rootView.findViewById(R.id.apppermission);
//        subscriptiontime = rootView.findViewById(R.id.subscriptiontime);
        paymentsbtn = rootView.findViewById(R.id.paymentsbtn);
        website = rootView.findViewById(R.id.website);
        profilebtn = rootView.findViewById(R.id.profilebtn);
        attendancebtn = rootView.findViewById(R.id.attendancebtn);
        displaybtn = rootView.findViewById(R.id.displaybtn);
        detailnightmode = rootView.findViewById(R.id.detailnightmode);
        displayright = rootView.findViewById(R.id.displayright);
        aSwitch = rootView.findViewById(R.id.day_night_switch);
        logout = rootView.findViewById(R.id.logout);
        faqs = rootView.findViewById(R.id.faqs);

        faqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        attendancebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AttendanceActivity.class));
            }
        });


        logout.setOnClickListener(view -> {
            Toast.makeText(getContext(), getString(R.string.successfully_logged_out), Toast.LENGTH_SHORT).show();

//                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false)) {
            if (false) {
                PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit()
                        .apply();
                mCallbacks.onChangeNightMOde();
            }
            else {
                PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .edit()
//                            .putBoolean(ISNIGHTMODE, false)
                        .putString(MYUSERID, "")
                        .putString(APITOKEN, "")
                        .apply();
                Realm.init(getContext());
                Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> realm.deleteAll());
                startActivity(new Intent(getActivity(), GetPhoneNumberActivity.class));
                getActivity().finish();
            }
        });

        profilebtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AccountActivity.class));

        });

        timetablebtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), TimetableActivity.class));
        });

        homeworkbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        paymentsbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), PaymentActivity.class));
        });
        website.setOnClickListener(view -> {
            clickview(view);
            Uri webpage = Uri.parse("https://www.univirtualschools.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }else{
                Toast.makeText(getContext(), getString(R.string.page_not_found), Toast.LENGTH_LONG).show();
            }
        });
        permission.setOnClickListener(view -> {
            clickview(view);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            String defaultDialerPackage = getDefaultDialerPackage(getContext());
            String firstIndexPackage = getPackagesOfDialerApps(getContext()).get(0);
            if (defaultDialerPackage.equals(firstIndexPackage)) {
                Toast.makeText(getContext(), getString(R.string.you_are_already_using_system_default), Toast.LENGTH_SHORT).show();
            }
            else {
                changeDefaultDialer(getActivity(), getPackagesOfDialerApps(getContext()).get(0));
            }
        });

        apppermission.setOnClickListener(view -> {
            clickview(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                             @Override
                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                 if (
                                                                         resultSet.isPermissionGranted(CALL_PHONE) &&
                                                                                 resultSet.isPermissionGranted(MANAGE_OWN_CALLS)
                                                                 ) {
                                                                     setSchoolDirectStudentAsDefaultCallingApp(getContext());
                                                                 }
                                                             }

                                                             @Override
                                                             public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                 Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                             }
                                                         },
                        CALL_PHONE,
                        MANAGE_OWN_CALLS
                );
            } else {
                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                             @Override
                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                 if (
                                                                         resultSet.isPermissionGranted(CALL_PHONE)
                                                                 ) {
                                                                     setSchoolDirectStudentAsDefaultCallingApp(getContext());
                                                                 }
                                                             }

                                                             @Override
                                                             public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                 Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                             }
                                                         },
                        CALL_PHONE
                );
            }
        });
        homeworkbtn.setOnClickListener(view -> {

            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        homeworkbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), AssignmentActivity.class));
        });
        displaybtn.setOnClickListener(view -> {
            clickview(view);
            aSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false));
            if (detailnightmode.getVisibility() == View.VISIBLE) {
                detailnightmode.setVisibility(View.GONE);
                displayright.setImageResource(R.drawable.right);
            } else {
                detailnightmode.setVisibility(View.VISIBLE);
                displayright.setImageResource(R.drawable.arrowdown);
            }
        });
        aSwitch.setOnClickListener(v -> {
            PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .edit()
                    .putBoolean(ISNIGHTMODE, !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false))
                    .apply();

            mCallbacks.onChangeNightMOde();
        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setSchoolDirectStudentAsDefaultCallingApp(Context context) {
        if (getDefaultDialerPackage(getContext()).equals(context.getPackageName())) {
            Toast.makeText(getContext(), getString(R.string.you_are_already_using_school_direct_student_app), Toast.LENGTH_SHORT).show();
        }
        else {
            changeDefaultDialer(getActivity(), context.getPackageName());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks
        mCallbacks = (SettingsFragment.Callbacks) activity;
    }


    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);

    }

    public interface Callbacks {
        //Callback for when button clicked.
        void onChangeNightMOde();
    }
}
