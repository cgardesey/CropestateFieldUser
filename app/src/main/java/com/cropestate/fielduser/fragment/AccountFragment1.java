package com.cropestate.fielduser.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ApplicationVersionSignature;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;
import com.noelchew.multipickerwrapper.library.MultiPickerWrapper;
import com.noelchew.multipickerwrapper.library.ui.MultiPickerWrapperSupportFragment;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.PictureActivity;
import com.cropestate.fielduser.constants.Const;
import com.cropestate.fielduser.util.PixelUtil;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

import static com.cropestate.fielduser.activity.AccountActivity.realmStudent;
import static com.cropestate.fielduser.activity.PictureActivity.profilePicBitmap;

/**
 * Created by 2CLearning on 12/13/2017.
 */

public class AccountFragment1 extends MultiPickerWrapperSupportFragment {
    public static final String PICTURE_TYPE = "PICTURE_TYPE";
    public static final String TYPE_PROFILE_PIC = "TYPE_PROFILE_PIC";
    private static final String TAG = "AccountFragment1";
    public RoundedImageView profilePic;
    Context mContext;
    LinearLayout controls;
    public static File profile_pic_file = null;
    MultiPickerWrapper.PickerUtilListener multiPickerWrapperListener = new MultiPickerWrapper.PickerUtilListener() {
        @Override
        public void onPermissionDenied() {
            // do something here
        }

        @Override
        public void onImagesChosen(List<ChosenImage> list) {
            controls.setVisibility(View.GONE);
            String imagePath = list.get(0).getOriginalPath();
            profilePic.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            profile_pic_file = new File(list.get(0).getOriginalPath());
        }

        @Override
        public void onVideosChosen(List<ChosenVideo> list) {
            Const.showToast(getContext(), mContext.getString(R.string.unsupported_file_format));
        }

        @Override
        public void onError(String s) {
            Toast.makeText(getContext(), getString(R.string.error_choosing_image), Toast.LENGTH_SHORT).show();
            Log.d(TAG, s);
        }
    };
    public static EditText firstName, lastName, otherNames, emailaddress;
    public static Spinner title, gender;
    private FloatingActionButton addimage, gal, cam;
    private ImageView opendate;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();

        final View rootView = inflater.inflate(R.layout.fragment_account1, container, false);
        profilePic = rootView.findViewById(R.id.profile_imgview);
        firstName = rootView.findViewById(R.id.first_name);
        lastName = rootView.findViewById(R.id.last_name);
        otherNames = rootView.findViewById(R.id.other_names);
        title = rootView.findViewById(R.id.title_spinner);
        emailaddress = rootView.findViewById(R.id.emailaddress);
        gender = rootView.findViewById(R.id.gender_spinner);
        progressBar = rootView.findViewById(R.id.progress_bar);
        addimage = rootView.findViewById(R.id.addimage);
        controls = rootView.findViewById(R.id.add);
        gal = rootView.findViewById(R.id.gal);
        cam = rootView.findViewById(R.id.cam);


        addimage.setOnClickListener(v -> {
            if (controls.getVisibility() == View.VISIBLE) {
                controls.setVisibility(View.GONE);

            } else {
                controls.setVisibility(View.VISIBLE);
            }
        });
        gal.setOnClickListener(v -> multiPickerWrapper.getPermissionAndPickSingleImageAndCrop(imgOptions(), 1, 1));
        cam.setOnClickListener(v -> multiPickerWrapper.getPermissionAndTakePictureAndCrop(imgOptions(), 1, 1));

        profilePic.setOnClickListener(view -> {
            if (profilePic.getDrawable() == null) {
                Toast.makeText(mContext, getString(R.string.image_not_set), Toast.LENGTH_SHORT).show();
            } else {
                profilePicBitmap = ((RoundedDrawable) profilePic.getDrawable()).getSourceBitmap();
                Intent intent = new Intent(getActivity(), PictureActivity.class);
                intent.putExtra(PICTURE_TYPE, TYPE_PROFILE_PIC);
                getActivity().startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // txtData = (TextView)view.findViewById(R.id.txtData);
    }

    @Override
    protected MultiPickerWrapper.PickerUtilListener getMultiPickerWrapperListener() {
        return multiPickerWrapperListener;
    }

    public void init() {
        if (realmStudent != null) {
            String picture = realmStudent.getProfilepicurl();
            boolean pictureExists = picture != null;
            if (pictureExists) {
                Glide.with(getContext())
                        .load(realmStudent.getProfilepicurl())
                        .apply(new RequestOptions().centerCrop())
                        .apply(RequestOptions.signatureOf(ApplicationVersionSignature.obtain(getContext())))
                        .into(profilePic);
            } else {
                profilePic.setImageBitmap(null);
            }

            title.setSelection(((ArrayAdapter) title.getAdapter()).getPosition(realmStudent.getTitle()));
            firstName.setText(realmStudent.getFirstname());
            lastName.setText(realmStudent.getLastname());
            otherNames.setText(realmStudent.getOthername());
            gender.setSelection(((ArrayAdapter) gender.getAdapter()).getPosition(realmStudent.getGender()));
            emailaddress.setText(realmStudent.getEmailaddress());
        }
    }

    public boolean validate() {
        boolean validated = true;

        if (TextUtils.isEmpty(firstName.getText())) {
            firstName.setError(getString(R.string.error_field_required));
            validated = false;
        } else {
//            realmStudent.setFirstname(firstName.getText().toString().trim());
        }
        if (TextUtils.isEmpty(lastName.getText())) {
            lastName.setError(getString(R.string.error_field_required));
            validated = false;
        } else {
//            realmStudent.setLastname(lastName.getText().toString().trim());
        }
        if (title.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) title.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            validated = false;
        } else {
//            realmStudent.setTitle(title.getSelectedItem().toString());
        }
        if (gender.getSelectedItemPosition() == 0) {
            TextView errorText = (TextView) gender.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            validated = false;
        } else {
//            realmStudent.setGender(gender.getSelectedItem().toString());
        }
        if (!isValidEmail(emailaddress.getText().toString().trim())){
            emailaddress.setError("Invalid email!");
            validated = false;
        }
        else {

        }
//        realmStudent.setOthername(otherNames.getText().toString().trim());
        return validated;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private UCrop.Options imgOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setCropFrameColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setCropFrameStrokeWidth(PixelUtil.dpToPx(getContext(), 4));
        options.setCropGridColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setCropGridStrokeWidth(PixelUtil.dpToPx(getContext(), 2));
        options.setActiveWidgetColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setToolbarTitle(getString(R.string.crop_image));

        // set rounded cropping guide
        options.setCircleDimmedLayer(true);
        return options;
    }
}