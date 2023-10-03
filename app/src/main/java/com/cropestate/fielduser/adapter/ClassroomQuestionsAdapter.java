package com.cropestate.fielduser.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.other.InitApplication;
import com.cropestate.fielduser.pojo.Participant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cropestate.fielduser.activity.ClassroomActivity.loadimg;
import static com.cropestate.fielduser.activity.ClassroomActivity.resultnumber;
import static com.cropestate.fielduser.activity.PhoneActivity.ROOMID;
import static com.cropestate.fielduser.constants.keyConst.CALL_API_BASE_URL;

public class ClassroomQuestionsAdapter extends RecyclerView.Adapter<ClassroomQuestionsAdapter.ViewHolder> implements Filterable {
    ArrayList<Participant> participantArrayList;
    Context context;
    int type;



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_mystudents_classroom_questions,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    public ClassroomQuestionsAdapter(ArrayList<Participant> participantArrayList, int type)
    {
        this.participantArrayList = participantArrayList;
        this.type = type;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Participant participant = participantArrayList.get(position);
        context = holder.studentnameView.getContext();
        holder.studentnameView.setText(participant.getStd_name());
        String[] separated = participant.getLogin_time().split("@");
       // holder.logintimeView.setText(separated[separated.length-1]);
        holder.daysdate.setText(separated[0]);
        holder.statustextView.setText(participant.getIsmute());
        holder.contactView.setText(participant.getPhone_number());
        holder.studentidView.setText(participant.getDuration() + " " + context.getString(R.string.seconds));
        String role = participant.getParticipant_type();
        String mute = participant.getIsmute();
        Glide.with(holder.studentnameView.getContext())
                .load(participant.getStd_image())
                .apply(new RequestOptions().centerCrop())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(holder.studentimageView);
        
        
        
        if(mute.contains("No"))
        {
            holder.statustextView.setText(context.getString(R.string.active));
        }
        else {
            holder.statustextView.setText(context.getString(R.string.mute));
        }
        holder.roletextView.setText(participant.getParticipant_type());
        if(role.contains("ADMIN"))
        {
            holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.letter_tile_default_color));
            holder.studentnameView.setTextColor(context.getResources().getColor(R.color.white));
            holder.logintimeView.setTextColor(context.getResources().getColor(R.color.white));
            holder.statustextView.setTextColor(context.getResources().getColor(R.color.white));
            holder.studentidView.setTextColor(context.getResources().getColor(R.color.white));
            holder.studentnameView.setText(context.getString(R.string.administrator));
        }




    }

    @Override
    public int getItemCount() {
        return participantArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }
    public void reload(ArrayList<Participant> participantArrayList)
    {
this.participantArrayList = participantArrayList;
notifyDataSetChanged();
    }

    public void setFilter(ArrayList<Participant> arrayList) {
        participantArrayList = new ArrayList<>();
        participantArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private Context mContext;
        String ROOM_NUMBER;
        LinearLayout parentLayout,detailstudentLayout,unmutebtnLayout,exitviewLayout,resultlayout,mutebtn;
        ImageView studentimageView,unmuteimgView,exitimgView,loadimgView,downbtn;
        View statuscolorView;
        TextView studentnameView,logintimeView,statustextView,studentidView,unmutetextView
                ,exittextView,resultextView,roletextView,contactView,daysdate;
        public ViewHolder(View view) {
            super(view);
            statustextView = view.findViewById(R.id.statustext);
            daysdate = view.findViewById(R.id.daysdate);
            studentidView = view.findViewById(R.id.studentid);
            contactView = view.findViewById(R.id.instructorname);
            logintimeView = view.findViewById(R.id.logintime);
            studentnameView = view.findViewById(R.id.studentname);
            unmutetextView = view.findViewById(R.id.unmutetext);
            exittextView = view.findViewById(R.id.exittext);
            statuscolorView = view.findViewById(R.id.statuscolor);
            studentimageView = view.findViewById(R.id.studentimage);
            unmuteimgView = view.findViewById(R.id.unmuteimg);
            exitimgView = view.findViewById(R.id.exitimg);
            exitviewLayout = view.findViewById(R.id.exitview);
            parentLayout = view.findViewById(R.id.bannerstudent);
            detailstudentLayout = view.findViewById(R.id.details);
            unmutebtnLayout = view.findViewById(R.id.unmutebtn);
            resultlayout = view.findViewById(R.id.resultlayout);
            loadimgView = view.findViewById(R.id.loadimg);
            resultextView = view.findViewById(R.id.resultext);
            downbtn = view.findViewById(R.id.upbtn);
            mutebtn = view.findViewById(R.id.mutebtn);
            roletextView = view.findViewById(R.id.roletext);

            mContext = unmutebtnLayout.getContext();
            unmutebtnLayout.setOnClickListener(view1 -> {
                Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.click);
                view1.startAnimation(animation1);
                unmutestudent(contactView.getText().toString().substring(3));
            });
            mutebtn.setOnClickListener(view12 -> {
                Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.click);
                view12.startAnimation(animation1);
                mutestudent(contactView.getText().toString().substring(3));
            });
            exitviewLayout.setOnClickListener(view13 -> {
                Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.click);
                view13.startAnimation(animation1);
                kickoutstudent(contactView.getText().toString().substring(3));
            });
//            menubtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.click);
//                    view.startAnimation(animation1);
//                    PopupMenu popup = new PopupMenu(mContext,menubtn);
//
//                    popup.inflate(R.menu.classroom_menu);
//
//                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.lockclass:
//                                    lockstudent(studentnameView.getText().toString());
//                                    return true;
//                                case R.id.inviteclass:
//                                    inviteclass(studentnameView.getText().toString());
//                                    return true;
//
//
//                            }
//                            return false;
//                        }
//                    });
//
//                    popup.show();
//                }
//            });
            itemView.setOnClickListener(this);
//            openassignmentbtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(view.getContext(), UploadActivity.class);
//                    intent.putExtra("coursename",coursename.getText().toString());
//                    intent.putExtra("topic",topic.getText().toString());
//                    intent.putExtra("submitdate",submitdate.getText().toString());
//                    intent.putExtra("downloadurl",downloadurl.getText().toString());
//                    view.getContext().startActivity(intent);
//
//
//                }
//            });

        }
        @Override
        public void onClick(View view) {
            Animation animation1 = AnimationUtils.loadAnimation(view.getContext(), R.anim.click);
           // view.startAnimation(animation1);


        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
        private void kickoutstudent(String phone) {
            String URL = CALL_API_BASE_URL + "api/v1/participant/kickout/" + PreferenceManager.getDefaultSharedPreferences(context).getString(ROOMID, "") + "/"+phone;
            try {
                if(type == 0) {
                   loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.kicking_out_student));
                    resultnumber.setVisibility(View.VISIBLE);
                }
                else if(type == 1)
                {
                    loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.kicking_out_student));
                    resultnumber.setVisibility(View.VISIBLE);
                }
                else
                {
                    loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.kicking_out_student));
                    resultnumber.setVisibility(View.VISIBLE);
                }
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
                    String status = null;
                    try {
                        status = response.getString("description");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(!status.contains(mContext.getString(R.string.kick_action_failed_for_user))) {
                        if (type == 0) {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(mContext.getString(R.string.successfully_kicked_student_out));
                        } else if (type == 1) {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(mContext.getString(R.string.successfully_kicked_student_out));
                        } else {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(mContext.getString(R.string.successfully_kicked_student_out));
                        }
                    }
                    else
                    {
                        if (type == 0) {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        } else if (type == 1) {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(R.string.student_not_in_class);
                        } else {
                            loadimg.setVisibility(View.VISIBLE);
                            resultnumber.setText(R.string.student_not_in_class);
                        }
                    }

                }, error -> {
                    Log.i("bbbb",error.toString());
                    if(type == 0) {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else if(type==1)
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }

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
        private void mutestudent(String phone) {
            String URL = CALL_API_BASE_URL + "api/v1/participant/mute/" + PreferenceManager.getDefaultSharedPreferences(context).getString(ROOMID, "") + "/" + phone;
            try {

                JSONObject jsonBody = new JSONObject();
                if(type == 0) {
                    loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.muting_student));
                } else if(type == 1)
                {
                    loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.muting_student));
                }
                else
                {
                    loadimg.setVisibility(View.VISIBLE);
                    resultnumber.setText(mContext.getString(R.string.muting_student));
                }
                Log.i("bbbb",URL);
                JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.POST, URL,null, response -> {
                    int roomid = 0;
                    if(response==null)
                    {
                        return;
                    }
                    JSONArray jsonArray = null;
                    Log.i("bbbb",response.toString());
                    unmutebtnLayout.setVisibility(View.VISIBLE);
                    mutebtn.setVisibility(View.GONE);
                    String status = null;
                    try {
                        status = response.getString("description");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(!status.contains("Mute action failed for user")) {
                        if (type == 0) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_muted_student));

                        } else if (type == 1) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_muted_student));
                        } else {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_muted_student));
                        }
                    }
                    else
                    {
                        if (type == 0) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));

                        } else if (type == 1) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        } else {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        }
                    }
                }, error -> {
                    Log.i("bbbb",error.toString());
                    if(type == 0) {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else if(type==1)
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }

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
        private void unmutestudent(String phone) {
            String URL = CALL_API_BASE_URL + "api/v1/participant/unmute/" + PreferenceManager.getDefaultSharedPreferences(context).getString(ROOMID, "") + "/" + phone;
            if(type == 0) {
                loadimg.setVisibility(View.VISIBLE);
                resultnumber.setText(mContext.getString(R.string.unmuting_student));
                resultnumber.setVisibility(View.VISIBLE);
            }
            else if(type == 1)
            {
                loadimg.setVisibility(View.VISIBLE);
                resultnumber.setText(mContext.getString(R.string.unmuting_student));
                resultnumber.setVisibility(View.VISIBLE);
            }
            else
            {
                loadimg.setVisibility(View.VISIBLE);
                resultnumber.setText(mContext.getString(R.string.unmuting_student));
                resultnumber.setVisibility(View.VISIBLE);
            }
            try {

                JSONObject jsonBody = new JSONObject();

                Log.i("bbbb",URL);
                JsonObjectRequest jsonOblect = new JsonObjectRequest(Request.Method.GET, URL,null, response -> {
                    int roomid = 0;
                    if(response==null)
                    {
                        return;
                    }
                    JSONArray jsonArray = null;
                    Log.i("bbbb",response.toString());
                    unmutebtnLayout.setVisibility(View.GONE);
                    mutebtn.setVisibility(View.VISIBLE);
                    String status = null;
                    try {
                         status = response.getString("description");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(!status.contains("Unmute action failed for user")) {
                        if (type == 0) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_unmuted_student));
                        } else if (type == 1) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_unmuted_student));
                        } else {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.successfully_unmuted_student));
                        }
                    }
                    else
                    {
                        if (type == 0) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        } else if (type == 1) {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        } else {
                            loadimg.setVisibility(View.GONE);
                            resultnumber.setText(mContext.getString(R.string.student_not_in_class));
                        }
                    }

                }, error -> {
                    Log.i("bbbb",error.toString());
                    if(type == 0) {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else if(type == 1)
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }
                    else
                    {
                        loadimg.setVisibility(View.GONE);
                        resultnumber.setText(error.toString());
                    }

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
    //get bitmap image from byte array

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
    private void inviteclass(String phone) {
        String URL = CALL_API_BASE_URL + "api/v1/participant/invite/"+ PreferenceManager.getDefaultSharedPreferences(context).getString(ROOMID, "") + "/"+phone;
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

            }, error -> Log.i("bbbb",error.toString())) {
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

