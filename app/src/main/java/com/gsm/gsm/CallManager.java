package com.gsm.gsm;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.TelecomManager;
import android.util.Log;

import java.util.ArrayList;


@TargetApi(Build.VERSION_CODES.M)
public class CallManager {
    private static final String TAG = "CallManager";

    public interface StateListener {
        void onCallStateChanged(UiCall call);
    }

    private static CallManager sInstance = null;
    private static int counter;
    private TelecomManager mTelecomManager;
    public static Call mCurrentCall = null;

    public static ArrayList<Call> callArrayList  = new ArrayList<>();
    private StateListener mStateListener = null;

    public static CallManager init(Context applicationContext) {
        if (sInstance == null) {
            sInstance = new CallManager(applicationContext);
            //  callArrayList.add(sInstance);

        } else {
            throw new IllegalStateException("CallManager has been initialized.");
        }
        return sInstance;
    }
    public static CallManager get() {
        if (sInstance == null) {
            throw new IllegalStateException("Call CallManager.init(Context) before calling this function.");
        }
        return sInstance;
    }
    public static ArrayList<Call> getAll() {
        if (callArrayList.size() == 0) {
//            throw new IllegalStateException("Call CallManager.init(Context) before calling this function.");
        }
        return callArrayList;
    }
    public static Call getSpecificCall(String phone) {
        if (callArrayList.size() == 0) {
            //  throw new IllegalStateException("Call CallManager.init(Context) before calling this function.");
        }
        for(Call call : callArrayList)
        {
            if(call!=null) {

                if (call.toString().contains(phone)) {
                    mCurrentCall = call;
                }
            }
        }
        return mCurrentCall;
    }

    private CallManager(Context context) {
        Log.i(TAG, "init CallManager");
        counter++;

        mTelecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
    }

    public void registerListener(StateListener listener) {
        mStateListener = listener;
    }

    public void unregisterListener() {
        mStateListener = null;
    }

    public UiCall getUiCall() {
        return UiCall.convert(mCurrentCall);
    }

    public void updateCall(Call call) {
        mCurrentCall = call;

        if (mStateListener != null && mCurrentCall != null) {
            mStateListener.onCallStateChanged(UiCall.convert(mCurrentCall));
        }
    }

    public void placeCall(String number) {
        Uri uri = Uri.fromParts("tel", number, null);
        mTelecomManager.placeCall(uri, null);
    }

    public void cancelCall() {
        if (mCurrentCall != null) {
            if (mCurrentCall.getState() == Call.STATE_RINGING) {
                rejectCall();
            } else {
                disconnectCall();
            }
        }
    }
    public void playTone(char c) {
        if (mCurrentCall != null) {
            mCurrentCall.playDtmfTone(c);
            mCurrentCall.stopDtmfTone();
        }
    }
    public void dialContinue() {
        if (mCurrentCall != null) {
            mCurrentCall.postDialContinue(true);
        }
    }
    public void acceptCall() {
        Log.i(TAG, "acceptCall");

        if (mCurrentCall != null) {
            mCurrentCall.answer(mCurrentCall.getDetails().getVideoState());
        }
    }

    private void rejectCall() {
        Log.i(TAG, "rejectCall");

        if (mCurrentCall != null) {
            mCurrentCall.reject(false, "");
        }
    }

    private void disconnectCall() {
        Log.i(TAG, "disconnectCall");

        if (mCurrentCall != null) {
            mCurrentCall.disconnect();
        }
    }

    public void pauseCall() {
        Log.i(TAG, "pauseCall");

        if (mCurrentCall != null) {
            mCurrentCall.hold();
        }
    }
    public void unPauseCall() {
        Log.i(TAG, "pauseCall");

        if (mCurrentCall != null) {
            mCurrentCall.unhold();
        }
    }
    public static int getNumOfInstances() {
        return counter;
    }
}
