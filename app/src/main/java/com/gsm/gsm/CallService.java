package com.gsm.gsm;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.cropestate.fielduser.activity.ClassroomActivity;


@TargetApi(Build.VERSION_CODES.M)
public class CallService extends InCallService {

    private static final String TAG = "CallService";

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Log.i(TAG, "onCallAdded: " + call);
        CallManager.mCurrentCall = call;
        call.registerCallback(callCallback);
        Intent myIntent = new Intent(this, ClassroomActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
        CallManager.callArrayList.add(call);
        Log.i(TAG,String.valueOf(CallManager.callArrayList.size()));
        CallManager.get().updateCall(call);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        Log.i(TAG, "onCallRemoved: " + call);
        call.unregisterCallback(callCallback);
        CallManager.callArrayList.remove(call);
        Log.i(TAG,String.valueOf(CallManager.callArrayList.size()));
         CallManager.get().updateCall(null);
    }

    private Call.Callback callCallback = new Call.Callback() {

        @Override
        public void onStateChanged(Call call, int state) {

            Log.i(TAG, "Call.Callback onStateChanged: " + call + "state: " + state);
            CallManager.get().updateCall(call);
        }
    };
}
