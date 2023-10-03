package com.cropestate.fielduser.materialDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.activity.ClassroomActivity;
import com.cropestate.fielduser.other.InitApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static com.cropestate.fielduser.activity.PhoneActivity.ROOMID;
import static com.cropestate.fielduser.constants.keyConst.CALL_API_BASE_URL;


public class ClassRoomDialog extends DialogFragment {
    public static ProgressDialog dialog1;
    private static final String MY_PREFS_NAME = "AUTHID";
    EditText usernameView;
    String action,phonenumber;
    TextView resultext;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_classroom,null);
        Button cancelbtn = view.findViewById(R.id.cancelbtn);
        Button okbtn = view.findViewById(R.id.gobtn);
        final LinearLayout loadview = view.findViewById(R.id.loadview);
        resultext = view.findViewById(R.id.resultext);
        usernameView = view.findViewById(R.id.username);
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//        final String email = prefs.getString("email", "No name defined");//"No name defined" is the default value.
//        final String username = prefs.getString("username", "");
        ImageView iView = view.findViewById(R.id.load);
        Glide.with(getActivity()).asGif().load(R.drawable.spinner).apply(new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .error(R.drawable.error)).into(iView);
        loadview.setVisibility(View.GONE);
        okbtn.setOnClickListener(view1 -> {
            String email  = usernameView.getText().toString().trim();
            if(email.isEmpty())
            {
                return;
            }
            switch (action)
            {
                case "inviteclass":
                    resultext.setText(getResources().getString(R.string.inviting_to_class));
                    inviteclass(email);
                    break;
                case "lockstudent":
                    resultext.setText(getResources().getString(R.string.locking_student));
                    lockstudent(email);
                    break;
                case "unlockstudent":
                    resultext.setText(getResources().getString(R.string.unlocking_student));
                    unlockstudent(email);
                    break;
            }
            loadview.setVisibility(View.VISIBLE);
            // emailreports(email,username);
          //  changepassword(email,username);
        });

        cancelbtn.setOnClickListener(v -> {
            // view.setVisibility(View.GONE);
            dismiss();
        });
        // doneBtn.setOnClickListener(doneAction);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                getActivity().runOnUiThread(() -> getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)));
            }
        }, 5);
        return builder.create();

    }
    public void putdata(String action,String phonenumber)
    {
        this.phonenumber = phonenumber;
        this.action = action;
    }
    private void inviteclass(String phone) {
        String URL = CALL_API_BASE_URL + "api/v1/participant/invite/" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(ROOMID, "") +"/"+phone;
        try {

            JSONObject jsonBody = new JSONObject();

            Log.i("bbbb",URL);
            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.GET, URL,null, response -> {
                int roomid = 0;
                if(response==null)
                {
                    return;
                }
                resultext.setText(getResources().getString(R.string.success));
                new Handler().postDelayed(() -> dismiss(), 3000);
                JSONArray jsonArray = null;
                Log.i("bbbb",response.toString());

            }, error -> {
                Log.i("bbbb",error.toString());
                resultext.setText(error.toString());
                new Handler().postDelayed(() -> dismiss(), 3000);

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    return headers;
                }
            };
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void lockstudent(String phone) {
        ClassroomActivity.loadimg.setVisibility(View.VISIBLE);
        ClassroomActivity.resultnumber.setText(getResources().getString(R.string.locking_student));
        ClassroomActivity.resultnumber.setVisibility(View.VISIBLE);
        String URL = CALL_API_BASE_URL + "api/v1/participant/lock/" + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(ROOMID, "") + "/"+phone;
        try {

            JSONObject jsonBody = new JSONObject();

            Log.i("bbbb",URL);
            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, URL,null, response -> {
                int roomid = 0;
                if(response==null)
                {
                    return;
                }
                JSONArray jsonArray = null;
                Log.i("bbbb",response.toString());
                resultext.setText(getActivity().getString(R.string.success));
                new Handler().postDelayed(() -> dismiss(), 3000);
            }, error -> {
                resultext.setText(error.toString());
                new Handler().postDelayed(() -> dismiss(), 3000);
//                    MyStudentsFragment.loadimg.setVisibility(View.GONE);
//                    MyStudentsFragment.resultnumber.setText(error.toString());
                Log.i("bbbb",error.toString());

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    return headers;
                }
            };
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void unlockstudent(String phone) {
        String URL = CALL_API_BASE_URL + "api/v1/participant/unlock/"+ PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(ROOMID, "") + "/"+phone;
        try {

            JSONObject jsonBody = new JSONObject();

            Log.i("bbbb",URL);
            JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, URL,null, response -> {
                int roomid = 0;
                if(response==null)
                {
                    return;
                }
                JSONArray jsonArray = null;
                Log.i("bbbb",response.toString());
                resultext.setText(getActivity().getString(R.string.success));
                new Handler().postDelayed(() -> dismiss(), 3000);

            }, error -> {
                Log.i("bbbb",error.toString());
                resultext.setText(error.toString());
                new Handler().postDelayed(() -> dismiss(), 3000);

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    return headers;
                }
            };
            jsonOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonOblect);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}