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
import com.cropestate.fielduser.realm.RealmQuiz;
import com.cropestate.fielduser.realm.RealmSubmittedQuiz;
import com.cropestate.fielduser.util.RealmUtility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by Nana on 11/26/2017.
 */

public class SubmittedQuizzesFragment extends Fragment {

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
    static TextView nosubmittedquizzestext;
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
        nosubmittedquizzestext = rootView.findViewById(R.id.nosubmittedquizzestext);
        realmQuizzes = new ArrayList<>();
        recyclerview_quizzes = rootView.findViewById(R.id.recyclerview_quizzes);
        backbtn = rootView.findViewById(R.id.search);
        refresh = rootView.findViewById(R.id.refresh);
        initSubmittedQuizzes();

        return rootView;
    }

    public static void initSubmittedQuizzes() {
        populateSubmittedQuizzes();
        quizAdapter = new QuizzesAdapter(realmQuizzes);
        recyclerview_quizzes.setLayoutManager(new LinearLayoutManager(context));
        recyclerview_quizzes.setHasFixedSize(true);
        recyclerview_quizzes.setNestedScrollingEnabled(false);
        recyclerview_quizzes.setItemAnimator(new DefaultItemAnimator());
        recyclerview_quizzes.setAdapter(quizAdapter);
    }

    public static void populateSubmittedQuizzes() {
        LISTTYPE = ALL;
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {

            RealmResults<RealmQuiz> results = realm.where(RealmQuiz.class).equalTo("instructorcourseid", QuizzesActivity.instructorcourseid).findAll();
            nosubmittedquizzestext.setVisibility(View.VISIBLE);
            nosubmittedquizzestext.setText(context.getString(R.string.you_currently_have_no_submitted_quizzes_for_this_course));
            recyclerview_quizzes.setVisibility(View.GONE);
            realmQuizzes.clear();
            for (RealmQuiz realmQuiz : results) {
                RealmSubmittedQuiz realmSubmittedQuiz = realm.where(RealmSubmittedQuiz.class).equalTo("quizid", realmQuiz.getQuizid()).findFirst();
                if (realmSubmittedQuiz != null) {
                    realmQuiz.setPercentagescore(String.valueOf(realmSubmittedQuiz.getPercentagescore()) + "%");
                    realmQuizzes.add(realmQuiz);
                    nosubmittedquizzestext.setVisibility(View.GONE);
                    recyclerview_quizzes.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
