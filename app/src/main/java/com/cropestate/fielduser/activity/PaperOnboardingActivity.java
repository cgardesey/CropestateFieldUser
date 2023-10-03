package com.cropestate.fielduser.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ramotion.paperonboarding.PaperOnboardingEngine;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.cropestate.fielduser.R;

import java.util.ArrayList;

public class PaperOnboardingActivity extends AppCompatActivity {

    Button getstarted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);

        getstarted = findViewById(R.id.getstarted);

        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener((oldElementIndex, newElementIndex) -> {

        });

        engine.setOnRightOutListener(() -> {
        });

        getstarted.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), GetPhoneNumberActivity.class));
            finish();
        });

    }

    // Just example data for Onboarding
    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage(getString(R.string.live), getResources().getString(R.string.live_interactive),
                Color.parseColor("#FFC60A"), R.drawable.live, R.drawable.liveicon);
        PaperOnboardingPage scr2 = new PaperOnboardingPage(getString(R.string.recorded), getResources().getString(R.string.available_for_download),
                Color.parseColor("#9B90BC"), R.drawable.recorded, R.drawable.recorded);
        PaperOnboardingPage scr3 = new PaperOnboardingPage(getResources().getString(R.string.internet), getString(R.string.no_internet_required),
                Color.parseColor("#F47822"), R.drawable.internet, R.drawable.internet);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }
}
