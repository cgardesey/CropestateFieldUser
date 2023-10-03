package com.cropestate.fielduser.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.FaqAdapter;
import com.cropestate.fielduser.pojo.FAQ;
import com.cropestate.fielduser.util.LocaleHelper;

import java.util.ArrayList;
import java.util.Locale;


public class HelpActivity extends AppCompatActivity {

    private ImageView teacher_image, backbtn;
    RecyclerView helperview;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    private String api_token;

    ArrayList<FAQ> faqArrayList;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public void setLanguage() {
        SharedPreferences prefs = getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);
        String language = prefs.getString("language", "");
        // Toast.makeText(activity, language, Toast.LENGTH_SHORT).show();
        if (language.contains("French")) {
//use constructor with country
            Locale locale = new Locale("fr", "BE");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            Locale locale = new Locale("en", "GB");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helperview = findViewById(R.id.helperview);
        backbtn = findViewById(R.id.search);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        faqArrayList = new ArrayList<>();
//        faqArrayList.add(new FAQ(getString(R.string.how_to_enter_live_class), getString(R.string.go_to_enrolments_page)));
        faqArrayList.add(new FAQ(getString(R.string.how_to_approve_payments), getString(R.string.if_you_did_not_receive_popup)));
        faqArrayList.add(new FAQ(getString(R.string.how_to_use_system_default_for_calls), getString(R.string.click_to_use_app_for_calls)));
        faqArrayList.add(new FAQ(getString(R.string.how_to_use_app_for_calls), getString(R.string.click_on_settings_icon_to_use_app_for_calls)));
//        faqArrayList.add(new FAQ(getString(R.string.how_to_ask_question), getString(R.string.while_inside_live_class)));
        faqArrayList.add(new FAQ(getString(R.string.where_to_locate_downloaded_class_files), getString(R.string.all_files_and_videos_are_downloaded_to)));
        faqArrayList.add(new FAQ(getString(R.string.button_displays_subscribe), getString(R.string.click_and_proceed_to_enter_classroom)));
        faqArrayList.add(new FAQ(getString(R.string.class_call_drops), getString(R.string.if_you_use_different_sim_card)));

        FaqAdapter faqAdapter = new FaqAdapter(faqArrayList);


        helperview.setLayoutManager(new LinearLayoutManager(this));
        helperview.setHasFixedSize(true);
        helperview.setNestedScrollingEnabled(false);
        helperview.setItemAnimator(new DefaultItemAnimator());
        helperview.setAdapter(faqAdapter);
    }
}

