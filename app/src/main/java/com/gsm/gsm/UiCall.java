package com.gsm.gsm;

import android.os.Build;
import android.telecom.Call;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * android.telecom.Call 객체를 UI에서 표현하기 위해 변환한 객체
 */
public class UiCall {

    private static final String UNKNOWN_NAME = "Unknown";

    public enum Status {
        CONNECTING ("Connecting"),
        DIALING ("Calling..."),
        RINGING ("Incoming call"),
        ACTIVE ("Active"),
        DISCONNECTED ("Finished call"),
        UNKNOWN ("");

        private String v;

        Status(String s) {
            v = s;
        }

        public String toString() {
            return this.v;
        }
    }

    private Status mStatus;
    private String mDisplayName,mLocation,callid;


    public String getmLocation() {
        return (mLocation != null) ? mLocation : UNKNOWN_NAME;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static UiCall convert(@Nullable Call call) {
        UiCall uiCall = new UiCall();

        if (call != null) {
            switch (call.getState()) {
                case Call.STATE_ACTIVE:
                    uiCall.mStatus = Status.ACTIVE;
                    break;
                case Call.STATE_RINGING:
                    uiCall.mStatus = Status.RINGING;

                    break;
                case Call.STATE_CONNECTING:
                    uiCall.mStatus = Status.CONNECTING;
                    break;
                case Call.STATE_DIALING:
                    uiCall.mStatus = Status.DIALING;
                    break;
                case Call.STATE_DISCONNECTED:
                    uiCall.mStatus = Status.DISCONNECTED;
                    break;
                default:
                    uiCall.mStatus = Status.UNKNOWN;
                    break;
            }

            uiCall.mDisplayName = call.getDetails().getHandle().getSchemeSpecificPart();
            uiCall.mLocation =   call.getDetails().getHandle().getUserInfo();
            uiCall.callid = call.toString();
        }

        return uiCall;
    }

    private UiCall() {
        mStatus = Status.UNKNOWN;
        mDisplayName = null;
        mLocation = null;
    }

    public String getCallid()
    {
        return callid;
    }
    public Status getStatus() {
        return mStatus;
    }

    public String getDisplayName() {
        return (mDisplayName != null) ? mDisplayName : UNKNOWN_NAME;
    }
}
