package com.cropestate.fielduser.materialDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.realm.RealmPayment;
import com.cropestate.fielduser.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
import static com.cropestate.fielduser.constants.keyConst.API_URL;
import static com.cropestate.fielduser.constants.Const.myVolleyError;
import static com.cropestate.fielduser.fragment.EnrolmentsFragment.initEnrolmentsFragment;

/**
 * Created by Nana on 10/22/2017.
 */

public class SubscriptionMaterialDialog extends DialogFragment {

    private static final String TAG = "SubscriptionMaterialDialog";

    Activity activity;
    public static Spinner duration;
    TextView pay, amount_text, currency_text;
    EditText number;
    Double amount;
    String currency;
    String description;
    String enrolmentid;
    ProgressDialog progressDialog;

    SubscriptionMaterialDialog subscriptionMaterialDialog;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnrolmentid() {
        return enrolmentid;
    }

    public void setEnrolmentid(String enrolmentid) {
        this.enrolmentid = enrolmentid;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_subscription, null);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.processing));
        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        duration = view.findViewById(R.id.duration_spinner);
        amount_text = view.findViewById(R.id.amount_text);
        currency_text = view.findViewById(R.id.currency_text);
        pay = view.findViewById(R.id.pay);
        number = view.findViewById(R.id.number);

        subscriptionMaterialDialog = SubscriptionMaterialDialog.this;

        pay.setOnClickListener(v -> {
            /*if (duration.getSelectedItemPosition() == 0) {
                TextView errorText = (TextView) duration.getSelectedView();
                errorText.setError("");
                errorText.setTextColor(Color.RED);
            }*/


            String mobileno = number.getText().toString();
            if (TextUtils.isEmpty(mobileno)) {
                number.setError(getString(R.string.error_field_required));
            }
            else if (mobileno.length() != 12) {
                Toast.makeText(getActivity(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
            }
            else {
                savePayment();
            }
        });

        duration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0:
                        amount_text.setText(String.valueOf(getAmount()));
                        description = getCurrency() + amount_text.getText().toString() + " " + getActivity().getString(R.string.per_week);
                        break;
                    case 1:
                        amount_text.setText(String.valueOf(getAmount() * 4));
                        description = getCurrency() + amount_text.getText().toString() + " " + getActivity().getString(R.string.per_month);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        //  builder.setCancelable(false);
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

    private void savePayment() {

        try {
            JSONObject request = new JSONObject();
            request.put("msisdn", number.getText().toString());
            request.put("countrycode", "GH");
            request.put("network", "MTNGHANA");
            request.put("currency", "GHS");
            request.put("amount", amount);
            request.put("description", description);
            request.put("payerid", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MYUSERID, ""));
            request.put("enrolmentid", enrolmentid);
            progressDialog.show();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "payments",
                    request,
                    response -> {
                        if (response != null) {
                            Realm.init(getActivity());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
                                try {
                                    if (response.has("not_registered")) {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.not_registered))
                                                .setMessage(getString(R.string.number_is_not_registered_for_mobile_money))
                                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                                                })
                                                .show();
                                    }
                                    else if (response.has("already_subscribed")) {
                                        progressDialog.dismiss();
                                        realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("payment"));
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.already_subscribed))
                                                .setMessage(getString(R.string.your_are_already_subscribed))
                                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                                    subscriptionMaterialDialog.dismiss();
                                                    initEnrolmentsFragment();
                                                })
                                                .show();
                                    }
                                    else if (response.has("wait_time")) {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.pending_payment))
                                                .setMessage(getString(R.string.try_again_later))
                                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                                                })
                                                .show();
                                    }
                                    else if (response.has("current_payment")) {
                                        realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("current_payment"));
                                        realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("prev_payment"));


                                        JSONObject instructorCourseJson = response.getJSONObject("instructorcourse");
                                        String instructorcourseid = instructorCourseJson.getString("instructorcourseid");
                                        String realmPrice = realm.where(RealmInstructorCourse.class).equalTo("instructorcourseid", instructorcourseid).findFirst().getPrice();
                                        String serverPrice = instructorCourseJson.getString("price");
                                        if (realmPrice.equals(serverPrice)) {
                                            pay(response.getJSONObject("current_payment").getString("paymentid"));
                                        }
                                        else {
                                            realm.createOrUpdateObjectFromJson(RealmInstructorCourse.class, instructorCourseJson);
                                            progressDialog.dismiss();
                                            new AlertDialog.Builder(getActivity())
                                                    .setTitle(getString(R.string.price_change))
                                                    .setMessage("Subscription charge has now changed to " +  instructorCourseJson.getString("currency") + getUpdatedPrice(serverPrice) + "\n\n Continue?")
                                                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                                        try {
                                                            progressDialog.show();
                                                            pay(response.getJSONObject("current_payment").getString("paymentid"));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    })
                                                    .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                                                        subscriptionMaterialDialog.dismiss();
                                                        initEnrolmentsFragment();
                                                    })
                                                    .setCancelable(false)
                                                    .show();
                                        }
                                    }
                                    else {
                                        realm.createOrUpdateObjectFromJson(RealmPayment.class, response.getJSONObject("stored_payment"));
                                        pay(response.getJSONObject("stored_payment").getString("paymentid"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        myVolleyError(getActivity(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("My error", e.toString());
        }
    }

    private String getWaitTimeMsg(int wait_time) {
        if (wait_time < 1) {
            return getString(R.string.already_have_a_processing_payment);
        } else {
            return getString(R.string.already_have_a_processing_payment) + " " + wait_time  + " " + getMinutesString(wait_time) + " " + getString(R.string.before_trying_again);
        }
    }

    private String getMinutesString(int wait_time) {
        return wait_time == 1 ? getString(R.string.minute) : getString(R.string.minutes);
    }


    private void pay(String paymentid) {
        progressDialog.dismiss();
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.success))
                .setMessage(getString(R.string.dial_170))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        JSONObject request = new JSONObject();
                        request.put("paymentid", paymentid);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                API_URL + "payments/pay",
                                request,
                                response -> {
                                    if (response != null) {

                                    }
                                },
                                error -> {
                                    progressDialog.dismiss();
                                    myVolleyError(getActivity(), error);
                                }
                        ) {
                            /** Passing some request headers* */
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(APITOKEN, ""));
                                return headers;
                            }
                        };
                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("My error", e.toString());
                    }
                    dismiss();
                    subscriptionMaterialDialog.dismiss();
                    initEnrolmentsFragment();
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
//                                        .setNegativeButton(android.R.string.no, null)
//                                        .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private String getUpdatedPrice(String price) {
        switch (duration.getSelectedItemPosition()) {
            case 0:
                return price +  " per week";
            case 1:
                return String.valueOf(Double.parseDouble(price) * 4) +  " per month";
        }
        return null;
    }
}