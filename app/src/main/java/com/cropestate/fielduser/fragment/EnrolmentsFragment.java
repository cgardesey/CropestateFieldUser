package com.cropestate.fielduser.fragment;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.appcompat.widget.PopupMenu;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.android.volley.AuthFailureError;
        import com.android.volley.DefaultRetryPolicy;
        import com.android.volley.Request;
        import com.android.volley.toolbox.JsonObjectRequest;
        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.adapter.EnrolmentFragmentAdapter;
        import com.cropestate.fielduser.constants.Const;
        import com.cropestate.fielduser.other.InitApplication;
        import com.cropestate.fielduser.realm.RealmCourse;
        import com.cropestate.fielduser.realm.RealmEnrolment;
        import com.cropestate.fielduser.realm.RealmInstructor;
        import com.cropestate.fielduser.realm.RealmInstructorCourse;
        import com.cropestate.fielduser.realm.RealmInstructorCourseRating;
        import com.cropestate.fielduser.realm.RealmPayment;
        import com.cropestate.fielduser.realm.RealmPeriod;
        import com.cropestate.fielduser.realm.RealmTimetable;
        import com.cropestate.fielduser.util.RealmUtility;

        import org.joda.time.DateTime;
        import org.json.JSONException;

        import java.text.ParseException;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Map;

        import io.realm.Realm;
        import io.realm.RealmResults;
        import io.realm.Sort;

        import static com.cropestate.fielduser.activity.GetAuthActivity.APITOKEN;
        import static com.cropestate.fielduser.activity.GetAuthActivity.MYUSERID;
        import static com.cropestate.fielduser.constants.keyConst.API_URL;
        import static com.cropestate.fielduser.constants.Const.myVolleyError;
        import static com.cropestate.fielduser.constants.Const.toTitleCase;
        import static io.realm.Sort.ASCENDING;
        import static io.realm.Sort.DESCENDING;


public class EnrolmentsFragment extends Fragment {

    public static final int
            LIVE = 1,
            UPCOMING  = 2,
            ALL = 3,
            EXPIRED = 4,
            ACTIVE = 5;
    public static int LISTTYPE = ALL;
    public static ArrayList<RealmEnrolment> enrolments = new ArrayList<>(), liveClasses = new ArrayList<>(), upcomingClasses = new ArrayList<>(), allClasses = new ArrayList<>(), expiredClasses = new ArrayList<>(), activeClasses = new ArrayList<>();
    private static Context mContext;
    static RecyclerView enrolments_recyclerview;
    ImageView backbtn;
    static TextView nodatatextview;
    public static ImageView menu, refresh;
    public static EnrolmentFragmentAdapter enrolmentFragmentAdapter;
    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_enrolments, container, false);
        mContext = getContext();
        enrolments_recyclerview = rootView.findViewById(R.id.enrolments_recyclerview);
        nodatatextview = rootView.findViewById(R.id.nodatatext);
        menu = rootView.findViewById(R.id.menu);
        refresh = rootView.findViewById(R.id.refresh);
        enrolments = new ArrayList<>();
        liveClasses = new ArrayList<>();
        upcomingClasses = new ArrayList<>();

        populateAllClasses();
        enrolments.addAll(allClasses);
        enrolmentFragmentAdapter = new EnrolmentFragmentAdapter(enrolments, getActivity());
        enrolments_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        enrolments_recyclerview.setHasFixedSize(true);
        enrolments_recyclerview.setNestedScrollingEnabled(false);
        enrolments_recyclerview.setItemAnimator(new DefaultItemAnimator());
        enrolments_recyclerview.setAdapter(enrolmentFragmentAdapter);

        backbtn = rootView.findViewById(R.id.search);


        backbtn.setOnClickListener(v -> getActivity().finish());

        menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), menu);

            popup.inflate(R.menu.course_menu);

            popup.setOnMenuItemClickListener(item -> {
                enrolments.clear();
                int itemId = item.getItemId();
                if (itemId == R.id.upcoming) {
                    populateUpcomingClasses();
                    enrolments.addAll(upcomingClasses);
                    enrolmentFragmentAdapter.notifyDataSetChanged();
                    return true;
                } else if (itemId == R.id.allmyclasses) {
                    populateAllClasses();
                    enrolments.addAll(allClasses);
                    enrolmentFragmentAdapter.notifyDataSetChanged();
                    return true;
                } else if (itemId == R.id.expiredsubscription) {
                    populateExpired();
                    enrolments.addAll(expiredClasses);
                    enrolmentFragmentAdapter.notifyDataSetChanged();
                    return true;
                } else if (itemId == R.id.activesubscriptions) {
                    populateActive();
                    enrolments.addAll(activeClasses);
                    enrolmentFragmentAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        refresh.setOnClickListener(v -> refresh());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initEnrolmentsFragment();
    }

    public static void populateAllClasses() {
        initRealTimetables();
        initRealPeriods();
        LISTTYPE = ALL;

        HashMap<String, String> weeks = new HashMap<String, String>();
        weeks.put("Monday", "1");
        weeks.put("Tuesday", "2");
        weeks.put("Wednesday", "3");
        weeks.put("Thursday", "4");
        weeks.put("Friday", "5");
        weeks.put("Saturday", "6");
        weeks.put("Sunday", "7");
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmEnrolment> results = realm.where(RealmEnrolment.class)
                    .equalTo("enrolled", 1)
                    .equalTo("approved", true)
                    .findAll();
            for (RealmEnrolment realmEnrolment : results) {

                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid()).findFirst();
                realmEnrolment.setPrice(realmInstructorCourse.getPrice());
                realmEnrolment.setCurrency(realmInstructorCourse.getCurrency());
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                realmEnrolment.setCoursepath(realmCourse.getCoursepath());

                RealmPayment realmPayment = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmPayment.class).equalTo("enrolmentid", realmEnrolment.getEnrolmentid()).sort("id", DESCENDING).findFirst();

                boolean activelysubscribed = realmPayment != null && !realmPayment.isExpired();
                realmEnrolment.setActivelysubscribed(activelysubscribed);

                if (activelysubscribed) {
                    Date date = null;
                    try {

                        date = Const.dateFormat.parse(realmPayment.getExpirydate());
                        DateTime dateTime = new DateTime(date);
                        String day = String.valueOf(new DateTime(date).getDayOfMonth());
                        String month = Const.months[date.getMonth()];
                        String year = String.valueOf(new DateTime(date).getYear());
                        realmEnrolment.setSubsriptionexpirydate(month + " " + day + ", " + year);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                realmEnrolment.setTotalrating(realmInstructorCourse.getTotal_ratings());
                realmEnrolment.setRating(realmInstructorCourse.getRating());

                Date currentTime = Calendar.getInstance().getTime();
                String currentDow = String.valueOf(new DateTime(currentTime).getDayOfWeek());
                String currentHod = String.valueOf(new DateTime(currentTime).getHourOfDay());
                RealmTimetable realmTimetable = null;
                String[] fieldNames = {"downum", "period_id"};
                Sort[] sorts = {ASCENDING, ASCENDING};

                long min = (long) realm.where(RealmPeriod.class).min("order");
                long max = (long) realm.where(RealmPeriod.class).max("order");

                int period_id_now;

                DateTime dateTime = new DateTime(Calendar.getInstance().getTime());
                long order = dateTime.getHourOfDay();
                if (order >= min && order < max) {
                    period_id_now = realm.where(RealmPeriod.class)
                            .greaterThanOrEqualTo("order", order)
                            .lessThan("order", max)
                            .findFirst()
                            .getId();
                }
                else {
                    period_id_now = realm.where(RealmPeriod.class)
                            .equalTo("order", min)
                            .findFirst()
                            .getId();
                }


                int downum_now;
                if (order >= max ) {
                    downum_now = Integer.parseInt(currentDow) + 1;
                } else {
                    downum_now = Integer.parseInt(currentDow);
                }
                RealmTimetable timetable_query = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmTimetable.class)
                        .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                        .equalTo("downum", downum_now)
                        .findFirst();
                if (timetable_query == null) {
                    realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmTimetable.class)
                            .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                            .greaterThan("downum", downum_now)
                            .sort(fieldNames, sorts)
                            .findFirst();
                } else {
                    realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmTimetable.class)
                            .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                            .equalTo("downum", downum_now)
                            .sort(fieldNames, sorts)
                            .greaterThanOrEqualTo("period_id", period_id_now)
                            .findFirst();
                }
                if (realmTimetable == null) {
                    realmTimetable = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmTimetable.class)
                            .equalTo("instructorcourseid", realmEnrolment.getInstructorcourseid())
                            .sort(fieldNames, sorts)
                            .findFirst();
                }

                if (realmTimetable != null) {
                    RealmPeriod realmPeriod = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmPeriod.class).equalTo("id", realmTimetable.getPeriod_id()).findFirst();
                    realmTimetable.setStarttime(realmPeriod.getStarttime());

                    realmEnrolment.setDow(realmTimetable.getDow());
                    realmEnrolment.setDownum(weeks.get(realmTimetable.getDow()));
                    String dow_un = null;
                    if (realmEnrolment.getDownum().equals("1")) {
                        dow_un = mContext.getResources().getString(R.string.monday);
                    } else if (realmEnrolment.getDownum().equals("2")) {
                        dow_un = mContext.getResources().getString(R.string.tuesday);
                    } else if (realmEnrolment.getDownum().equals("3")) {
                        dow_un = mContext.getResources().getString(R.string.wednesday);
                    } else if (realmEnrolment.getDownum().equals("4")) {
                        dow_un = mContext.getResources().getString(R.string.thursday);
                    } else if (realmEnrolment.getDownum().equals("5")) {
                        dow_un = mContext.getResources().getString(R.string.friday);
                    } else if (realmEnrolment.getDownum().equals("6")) {
                        dow_un = mContext.getResources().getString(R.string.saturday);
                    } else if (realmEnrolment.getDownum().equals("7")) {
                        dow_un = mContext.getResources().getString(R.string.sunday);
                    }
                    realmEnrolment.setStarttime(realmPeriod.getStarttime());
                    realmEnrolment.setEndtime(realmPeriod.getEndtime());
                    realmEnrolment.setTime(mContext.getString(R.string.next_class_on) + " " + toTitleCase(dow_un) + " @ " + realmPeriod.getStarttime());

                    String dow = weeks.get(realmEnrolment.getDow());
                    String startH = realmEnrolment.getStarttime().split(":")[0];
                    String endH = realmEnrolment.getEndtime().split(":")[0];
                    boolean isLive = currentDow.equals(dow) && Integer.parseInt(currentHod) >= Integer.parseInt(startH) && Integer.parseInt(currentHod) < Integer.parseInt(endH);
                    boolean isUpComing = currentDow.equals(realmEnrolment.getDownum()) && Integer.parseInt(realmEnrolment.getStarttime().split(":")[0]) > Integer.parseInt(currentHod);

                    realmEnrolment.setLive(isLive);

                    realmEnrolment.setUpcoming(isUpComing);
                }

                RealmInstructor realmInstructor = realm.where(RealmInstructor.class).equalTo("infoid", realmInstructorCourse.getInstructorid()).findFirst();
                realmEnrolment.setProfilepicurl(realmInstructor.getProfilepicurl());
                realmEnrolment.setInstructorname(realmInstructor.getTitle() + " " + realmInstructor.getFirstname() + " " + realmInstructor.getOthername() + " " + realmInstructor.getLastname());


                RealmInstructorCourseRating realmInstructorCourseRating = realm.where(RealmInstructorCourseRating.class)
                        .equalTo("instructorcourseid", realmInstructorCourse.getInstructorcourseid())
                        .equalTo("studentid", PreferenceManager.getDefaultSharedPreferences(mContext).getString(MYUSERID, ""))
                        .findFirst();
                realmEnrolment.setRatedbyme(realmInstructorCourseRating != null);

                allClasses.add(realmEnrolment);
            }

            allClasses.clear();
            RealmResults<RealmEnrolment> sortedResults = realm.where(RealmEnrolment.class)
                    .equalTo("enrolled", 1)
                    .equalTo("approved", true)
                    .findAll().sort("starttime", ASCENDING)
                    .sort("downum", ASCENDING);
            for (RealmEnrolment sortedEnrolment : sortedResults) {
                allClasses.add(sortedEnrolment);
            }
            noDataCheck(allClasses.size(), mContext.getString(R.string.enrolments_will_show_here));
        });
    }

    private static String getHourWithLeadingZero(int h) {
        if (h < 12) {
            return "0" + String.valueOf(h);
        }
        return String.valueOf(h);
    }

    public static void populateUpcomingClasses() {
        LISTTYPE = UPCOMING;
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            upcomingClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {

                if (realmEnrolment.isUpcoming()) {
                    realmEnrolment.setUpcoming(true);
                    realmEnrolment.setTime(mContext.getString(R.string.today_at) + " " + realmEnrolment.getStarttime());

                    upcomingClasses.add(realmEnrolment);
                }
            }
            noDataCheck(upcomingClasses.size(), mContext.getString(R.string.no_upcoming_course));
        });
    }

    public static void populateLiveClasses() {
        LISTTYPE = LIVE;
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            liveClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (realmEnrolment.isLive()) {
                    realmEnrolment.setTime(realmEnrolment.getStarttime());
                    liveClasses.add(realmEnrolment);
                }
            }int size = liveClasses.size();
            noDataCheck(liveClasses.size(), mContext.getString(R.string.you_currently_have_no_live_classes));
        });
    }

    public static void populateExpired() {
        LISTTYPE = EXPIRED;
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            expiredClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (!realmEnrolment.getPrice().equals("0.00") && !realmEnrolment.isActivelysubscribed()) {
                    expiredClasses.add(realmEnrolment);
                }
            }
            noDataCheck(expiredClasses.size(), mContext.getString(R.string.no_expired_subscriptions));
        });
    }

    public static void populateActive() {
        LISTTYPE = ACTIVE;
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            activeClasses.clear();
            for (RealmEnrolment realmEnrolment : allClasses) {
                if (realmEnrolment.isActivelysubscribed() || realmEnrolment.getPrice().equals("0.00")) {
                    activeClasses. add(realmEnrolment);
                }
            }
            noDataCheck(activeClasses.size(), mContext.getString(R.string.no_active_subscriptions));
        });
    }

    public static void initRealTimetables() {
        LISTTYPE = ALL;

        HashMap<String, String> weeks = new HashMap<String, String>();
        weeks.put("Monday", "1");
        weeks.put("Tuesday", "2");
        weeks.put("Wednesday", "3");
        weeks.put("Thursday", "4");
        weeks.put("Friday", "5");
        weeks.put("Saturday", "6");
        weeks.put("Sunday", "7");
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmTimetable> timetables = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmTimetable.class).findAll();
            for (RealmTimetable realmTimetable : timetables) {
                realmTimetable.setDownum(Integer.parseInt(weeks.get(realmTimetable.getDow())));
            }
        });
    }

    public static void initRealPeriods() {
        Realm.init(mContext);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmPeriod> realmPeriods = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmPeriod.class).findAll();
            for (RealmPeriod realmPeriod : realmPeriods) {
                String[] split = realmPeriod.getStarttime().split(":");
                int order = 0;
                for (String value : split) {
                    order += Integer.parseInt(value);
                }
                realmPeriod.setOrder(order);
            }
        });
    }

    private static void noDataCheck(int size, String nodatatext) {
        nodatatextview.setText(nodatatext);
        if (size > 0) {
            nodatatextview.setVisibility(View.GONE);
            enrolments_recyclerview.setVisibility(View.VISIBLE);
        } else {
            nodatatextview.setVisibility(View.VISIBLE);
            enrolments_recyclerview.setVisibility(View.GONE);
        }
    }

    public void refresh() {
        try {
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getString(R.string.refreshing_enrolment_status));
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "enrolment-refreshVideos-data",
                    null,
                    responseJson -> {
                        if (responseJson != null) {
                            dialog.dismiss();
                            Realm.init(getContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        RealmResults<RealmCourse> realmCourses = realm.where(RealmCourse.class).findAll();
                                        realmCourses.deleteAllFromRealm();

                                        RealmResults<RealmEnrolment> realmEnrolments = realm.where(RealmEnrolment.class).findAll();
                                        realmEnrolments.deleteAllFromRealm();

                                        RealmResults<RealmTimetable> realmTimetables = realm.where(RealmTimetable.class).findAll();
                                        realmTimetables.deleteAllFromRealm();

                                        RealmResults<RealmInstructor> realmInstructors = realm.where(RealmInstructor.class).findAll();
                                        realmInstructors.deleteAllFromRealm();

                                        RealmResults<RealmInstructorCourse> realmInstructorCourses = realm.where(RealmInstructorCourse.class).findAll();
                                        realmInstructorCourses.deleteAllFromRealm();

                                        RealmResults<RealmPayment> realmPayments = realm.where(RealmPayment.class).findAll();
                                        realmPayments.deleteAllFromRealm();

                                        RealmResults<RealmInstructorCourseRating> realmInstructorCourseRatings = realm.where(RealmInstructorCourseRating.class).findAll();
                                        realmInstructorCourseRatings.deleteAllFromRealm();

                                        realm.createOrUpdateAllFromJson(RealmCourse.class, responseJson.getJSONArray("courses"));
                                        realm.createOrUpdateAllFromJson(RealmEnrolment.class, responseJson.getJSONArray("enrolments"));
                                        realm.createOrUpdateAllFromJson(RealmTimetable.class, responseJson.getJSONArray("timetables"));
                                        realm.createOrUpdateAllFromJson(RealmInstructor.class, responseJson.getJSONArray("instructors"));
                                        realm.createOrUpdateAllFromJson(RealmInstructorCourse.class, responseJson.getJSONArray("instructor_courses"));
                                        realm.createOrUpdateAllFromJson(RealmPayment.class, responseJson.getJSONArray("payments"));
                                        realm.createOrUpdateAllFromJson(RealmInstructorCourseRating.class, responseJson.getJSONArray("instructor_course_ratings"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            enrolments.clear();
                            switch (LISTTYPE) {
                                case ALL:
                                    populateAllClasses();
                                    enrolments.addAll(allClasses);
                                    break;
                                case UPCOMING:
                                    populateUpcomingClasses();
                                    enrolments.addAll(upcomingClasses);
                                    break;
                                case LIVE:
                                    populateLiveClasses();
                                    enrolments.addAll(liveClasses);
                                    break;
                                case EXPIRED:
                                    populateExpired();
                                    enrolments.addAll(expiredClasses);
                                    break;
                                case ACTIVE:
                                    populateActive();
                                    enrolments.addAll(activeClasses);
                                    break;
                            }
                            enrolmentFragmentAdapter.notifyDataSetChanged();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        dialog.dismiss();
                        myVolleyError(getContext(), error);
                    }
            ) {
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getContext()).getString(APITOKEN, ""));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonArrayRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initEnrolmentsFragment() {
        enrolments.clear();
        switch (LISTTYPE) {
            case ALL:
                populateAllClasses();
                enrolments.addAll(allClasses);
                break;
            case UPCOMING:
                populateUpcomingClasses();
                enrolments.addAll(upcomingClasses);
                break;
            case LIVE:
                populateLiveClasses();
                enrolments.addAll(liveClasses);
                break;
            case EXPIRED:
                populateExpired();
                enrolments.addAll(expiredClasses);
                break;
            case ACTIVE:
                populateActive();
                enrolments.addAll(activeClasses);
                break;
        }
        enrolmentFragmentAdapter.notifyDataSetChanged();
    }
}
