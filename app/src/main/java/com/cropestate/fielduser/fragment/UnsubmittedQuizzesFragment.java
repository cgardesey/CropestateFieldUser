package com.cropestate.fielduser.fragment;

        import android.app.Activity;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.DefaultItemAnimator;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.cropestate.fielduser.R;
        import com.cropestate.fielduser.activity.QuizzesActivity;
        import com.cropestate.fielduser.adapter.QuizzesAdapter;
        import com.cropestate.fielduser.constants.Const;
        import com.cropestate.fielduser.realm.RealmQuiz;
        import com.cropestate.fielduser.realm.RealmSubmittedQuiz;
        import com.cropestate.fielduser.util.RealmUtility;

        import java.text.ParseException;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;

        import io.realm.Realm;
        import io.realm.RealmResults;


/**
 * Created by Nana on 11/26/2017.
 */

public class UnsubmittedQuizzesFragment extends Fragment {

    static final int
            ALL = 1,
            PENDING = 2,
            PAST = 3;

    static int LISTTYPE = ALL;
    static QuizzesAdapter quizAdapter;
    static ArrayList<RealmQuiz> realmQuizzes = new ArrayList<>();
    ArrayList<RealmQuiz> allquizzes = new ArrayList<>();
    ArrayList<RealmQuiz> pendingPayments = new ArrayList<>();
    ArrayList<RealmQuiz> pastquizzes = new ArrayList<>();
    static RecyclerView recyclerview_quizzes;
    ImageView backbtn, refresh;
    static TextView noquizzestext;
    ProgressDialog dialog;
    public static String enrolmentid;
    static Context context;
    static Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_quizzes, container, false);

        activity = getActivity();
        context = getContext();
        enrolmentid = getActivity().getIntent().getStringExtra("ENROLMENTID");
        noquizzestext = rootView.findViewById(R.id.nosubmittedquizzestext);
        realmQuizzes = new ArrayList<>();
        recyclerview_quizzes = rootView.findViewById(R.id.recyclerview_quizzes);
        backbtn = rootView.findViewById(R.id.search);
        refresh = rootView.findViewById(R.id.refresh);
        initUnsubmittedQuizzes();

        return rootView;
    }

    public static  void initUnsubmittedQuizzes() {
        populateUnsubmittedQuizzes();
        quizAdapter = new QuizzesAdapter(realmQuizzes);
        recyclerview_quizzes.setLayoutManager(new LinearLayoutManager(context));
        recyclerview_quizzes.setHasFixedSize(true);
        recyclerview_quizzes.setNestedScrollingEnabled(false);
        recyclerview_quizzes.setItemAnimator(new DefaultItemAnimator());
        recyclerview_quizzes.setAdapter(quizAdapter);
    }

    public static void populateUnsubmittedQuizzes() {
        LISTTYPE = ALL;
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", QuizzesActivity.instructorcourseid).findAll();
            noquizzestext.setVisibility(View.VISIBLE);
            recyclerview_quizzes.setVisibility(View.GONE);
            realmQuizzes.clear();
            for (RealmQuiz RealmQuiz : results) {
                RealmSubmittedQuiz realmSubmittedQuiz = realm.where(RealmSubmittedQuiz.class).equalTo("quizid", RealmQuiz.getQuizid()).findFirst();
                if (realmSubmittedQuiz == null) {
                    realmQuizzes.add(RealmQuiz);
                    noquizzestext.setVisibility(View.GONE);
                    recyclerview_quizzes.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void populatePendingQuizzes() {
        LISTTYPE = PENDING;
        Realm.init(getContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", QuizzesActivity.instructorcourseid).findAll();
            pendingPayments.clear();

            Date dateNow = Calendar.getInstance().getTime();
            for (RealmQuiz realmQuiz : results) {
                try {
                    Date quizDate = Const.dateFormat.parse(realmQuiz.getDate());
                    if (quizDate.after(dateNow)) {
                        pendingPayments.add(realmQuiz);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void populatePastQuizzes() {
        LISTTYPE = PAST;
        Realm.init(getContext());
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", QuizzesActivity.instructorcourseid).findAll();
            pendingPayments.clear();

            Date dateNow = Calendar.getInstance().getTime();
            for (RealmQuiz realmQuiz : results) {
                try {
                    Date quizDate = Const.dateFormat.parse(realmQuiz.getDate());
                    if (quizDate.before(dateNow)) {
                        pendingPayments.add(realmQuiz);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
