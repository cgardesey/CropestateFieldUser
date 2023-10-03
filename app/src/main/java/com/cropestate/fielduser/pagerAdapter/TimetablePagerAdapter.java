package com.cropestate.fielduser.pagerAdapter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cropestate.fielduser.fragment.TimetableFragment;

import java.util.ArrayList;

public class TimetablePagerAdapter extends FragmentPagerAdapter {
    ArrayList<String> titles;

    public TimetablePagerAdapter(FragmentManager fm, ArrayList<String> titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        TimetableFragment timetableFragment = new TimetableFragment();
        Bundle bundle = new Bundle(7);
        bundle.putString("title", getPageTitle(position).toString());
        timetableFragment.setArguments(bundle);

        return timetableFragment;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = titles.get(position);
        return title;
    }
}