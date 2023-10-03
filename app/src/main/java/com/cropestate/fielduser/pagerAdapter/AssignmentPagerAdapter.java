package com.cropestate.fielduser.pagerAdapter;

/**
 * Created by Nana on 11/26/2017.
 */

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.cropestate.fielduser.fragment.AssignedFragment;
import com.cropestate.fielduser.fragment.CompleteFragment;


public class AssignmentPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public AssignmentPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                AssignedFragment assignedFragment = new AssignedFragment();
                return assignedFragment;
            case 1:
                CompleteFragment completeFragment = new CompleteFragment();
                return completeFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
