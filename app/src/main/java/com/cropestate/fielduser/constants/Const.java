package com.cropestate.fielduser.constants;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.cropestate.fielduser.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.cropestate.fielduser.activity.PhoneActivity.REQUEST_CODE_SET_DEFAULT_DIALER;

final public class Const {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static String[] months = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec."};
    public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static Toast toast;

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static void showToast(Context context, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String fileSize(long length) {
        String file_size;
        if (length < 1000L) {
            file_size = length + " B";
        } else if (length >= 1000L && length < 1000000L) {
            file_size = length / 1000L + " KB";
        } else {
            file_size = length / 1000000L + " MB";
        }
        return file_size;
    }

    public static void myVolleyError(Context context, VolleyError error) {
        Log.d("My VolleyError", error.toString());
        if (error instanceof NoConnectionError) {
            //This indicates that the reuest has either time out or there is no connection
            showToast(context, context.getString(R.string.connection_error));

        } else if (error instanceof TimeoutError) {
            // Error indicating that there was an Authentication Failure while performing the request
            showToast(context, context.getString(R.string.timeout_error));

        } else if (error instanceof AuthFailureError) {
            // Error indicating that there was an Authentication Failure while performing the request
            showToast(context, context.getString(R.string.authentication_failure));

        } else if (error instanceof ServerError) {
            //Indicates that the server responded with a error response
           /* NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && response != null) {
                try {
                    String res = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));

                    Toast.makeText(chatContext, res, Toast.LENGTH_LONG).show();
                    // Now you can use any deserializer to make sense of data
                    JSONObject obj = new JSONObject(res);

                    Log.d("asfasdfasfd", obj.toString());
                } catch (UnsupportedEncodingException e1) {
                    // Couldn't properly decode data to string
                    e1.printStackTrace();
                } catch (JSONException e2) {
                    // returned data is not JSONObject?
                    e2.printStackTrace();
                }
            }
            else {
                showToast(chatContext, "Server error");
            }*/
            showToast(context, context.getString(R.string.server_error));
        } else if (error instanceof NetworkError) {
            //Indicates that there was network error while performing the request
            showToast(context, context.getString(R.string.network_error));
        } else if (error instanceof ParseError) {
            // Indicates that the server response could not be parsed
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static final SimpleDateFormat sfd_date = new SimpleDateFormat("MMMM d, yyyy");

    public static String getFormattedDate(Context context, long timeInMillis) {
        String formattedDate = "";
        Calendar cal = Calendar.getInstance();
        long timeNowInMillis = cal.getTimeInMillis();
        cal.add(Calendar.DATE, -1);
        long timeYestInMillis = cal.getTimeInMillis();
        SimpleDateFormat sfd_year = new SimpleDateFormat("yyyy");
        SimpleDateFormat sfd_month = new SimpleDateFormat("MMMM d");
        if (sfd_date.format(new java.util.Date(timeInMillis)).equals(sfd_date.format(new java.util.Date(timeNowInMillis)))) {
            formattedDate = context.getString(R.string.today);
        } else if (sfd_date.format(new java.util.Date(timeInMillis)).equals(sfd_date.format(new java.util.Date(timeYestInMillis)))) {
            formattedDate = context.getString(R.string.yesterday);
        } else if (sfd_year.format(new java.util.Date(timeInMillis)).equals(sfd_year.format(new java.util.Date(timeNowInMillis)))) {
            formattedDate = sfd_month.format(new java.util.Date(timeInMillis));
        } else {
            formattedDate = sfd_date.format(new java.util.Date(timeInMillis));
        }
        return formattedDate;
    }

    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    public static void changeDefaultDialer(Activity activity, String packagename) {
        /*Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packagename);
        activity.startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);*/

        RoleManager roleManager = (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);
        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
        intent.putExtra(roleManager.ROLE_DIALER, packagename);
        activity.startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
    }

    public static List<String> getPackagesOfDialerApps(Context context){

        List<String> packageNames = new ArrayList<>();

        // Declare action which target application listen to initiate phone call
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        // Query for all those applications
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        // Read package name of all those applications
        for(ResolveInfo resolveInfo : resolveInfos){
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            packageNames.add(activityInfo.applicationInfo.packageName);
        }

        return packageNames;
    }
}