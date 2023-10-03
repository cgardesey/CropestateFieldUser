package com.cropestate.fielduser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.cropestate.fielduser.R;

import org.apache.http.util.TextUtils;

import java.lang.reflect.Method;


/**
 * Created by Nana on 11/26/2017.
 */

public class GetPhoneNumberActivity extends AppCompatActivity {

    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    Button nextpagebtn, backbtn;
    CountryCodePicker ccp;
    EditText phoneView;
    public static String phonenumber;
    static GetPhoneNumberActivity getPhoneNumberActivity;

    TextView multiplesimtext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_phone_number);

        getPhoneNumberActivity = this;
        ccp = findViewById(R.id.ccp);
        multiplesimtext = findViewById(R.id.multiplesimtext);
        nextpagebtn = findViewById(R.id.nextpagebtn);
        backbtn = findViewById(R.id.search);
        phoneView = findViewById(R.id.phoneView);
        ccp.registerPhoneNumberTextView(phoneView);


        if (isMultiSim(getApplicationContext())) {
            multiplesimtext.setVisibility(View.VISIBLE);
        } else {
            multiplesimtext.setVisibility(View.GONE);
        }
        multiplesimtext.setOnClickListener(view -> startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0));

        phoneView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        nextpagebtn.setOnClickListener(view -> {
            String text_from_edit_text = phoneView.getText().toString().trim();
            if (TextUtils.isEmpty(text_from_edit_text)) {
                Toast.makeText(GetPhoneNumberActivity.this, getString(R.string.please_enter_number), Toast.LENGTH_SHORT).show();
            }
            else {
                int length = text_from_edit_text.length();
                //546676098
                if (length == 9 && (text_from_edit_text.startsWith("24") || text_from_edit_text.startsWith("54") || text_from_edit_text.startsWith("55") || text_from_edit_text.startsWith("59"))) {
                    phonenumber = ccp.getNumber().substring(1);
                    new AlertDialog.Builder(GetPhoneNumberActivity.this)
                            .setTitle(getString(R.string.terms_and_conditions))
                            .setMessage(getString(R.string.terms_and_conditions_text))

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(GetPhoneNumberActivity.this.getString(R.string.accept), (dialog, which) -> startActivity(new Intent(GetPhoneNumberActivity.this, GetAuthActivity.class)))
                            .setNegativeButton(GetPhoneNumberActivity.this.getString(R.string.reject), (dialog, which) -> {

                            })
                            // A null listener allows the button to dismiss the dialog and take no further action.
//                                        .setNegativeButton(android.R.string.no, null)
//                                        .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(GetPhoneNumberActivity.this, getString(R.string.invalid_mtn_number), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(ISNIGHTMODE, false)) {
//            mCallbacks.onChangeNightMOde();
//            PreferenceManager
//                    .getDefaultSharedPreferences(getApplicationContext())
//                    .edit()
//                    .putBoolean(ISNIGHTMODE, false)
//                    .apply();
//        }
    }

    public static GetPhoneNumberActivity getInstance(){
        return getPhoneNumberActivity;
    }

    public boolean isMultiSim(Context context) {
        Object tm = context.getSystemService(Context.TELEPHONY_SERVICE);
        Method isMultiSimEnabled;
        boolean multisimEnabled = false;
        try {
            isMultiSimEnabled = tm.getClass().getDeclaredMethod("isMultiSimEnabled");
            multisimEnabled = (boolean) isMultiSimEnabled.invoke(tm);
            Log.d("aesfsdf", Boolean.toString(multisimEnabled));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return multisimEnabled;
    }
}
