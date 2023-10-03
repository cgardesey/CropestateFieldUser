package com.cropestate.fielduser.pagerAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cropestate.fielduser.fragment.AccountFragment1;

public class AccountPageAdapter extends FragmentPagerAdapter {

    public AccountPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                AccountFragment1 tab1 = new AccountFragment1();
                return tab1;
            /*case 1:
                AccountFragment2 tab2 = new AccountFragment2();
                return tab2;*/
            /*case 1:
                AccountFragment2 tab3 = new AccountFragment2();
                return tab3;*/
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }
}