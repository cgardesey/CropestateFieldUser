package com.cropestate.fielduser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.cropestate.fielduser.activity.HomeActivity.fetchAllMyData;
import static com.cropestate.fielduser.constants.Const.isNetworkAvailable;


public class NetworkReceiver extends BroadcastReceiver {

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcastWithFirebase.
        //throw new UnsupportedOperationException("Not yet implemented");

        mContext = context;
        if (isNetworkAvailable(context)) {
            fetchAllMyData(context);
        }
    }
}
