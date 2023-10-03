package com.cropestate.fielduser.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cropestate.fielduser.R;
import com.cropestate.fielduser.adapter.AssignmentAdapter;
import com.cropestate.fielduser.realm.RealmAssignment;
import com.cropestate.fielduser.realm.RealmCourse;
import com.cropestate.fielduser.realm.RealmEnrolment;
import com.cropestate.fielduser.realm.RealmInstructorCourse;
import com.cropestate.fielduser.realm.RealmSubmittedAssignment;
import com.cropestate.fielduser.util.RealmUtility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by Nana on 11/26/2017.
 */

public class CompleteFragment extends Fragment {

    static ArrayList<RealmAssignment> completedAssignments = new ArrayList<>();

    RecyclerView recyclerView;
    TextView nodatatext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_assignment, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerview);
        nodatatext = rootView.findViewById(R.id.nodatatext);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    public void populateCompletedAssignments(Context context) {

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig()).executeTransaction(realm -> {
            RealmResults<RealmAssignment> results = realm.where(RealmAssignment.class).findAll();

            completedAssignments.clear();
            for (RealmAssignment realmAssignment : results) {
                String enrolmentid = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmEnrolment.class).equalTo("instructorcourseid", realmAssignment.getInstructorcourseid()).findFirst().getEnrolmentid();
                realmAssignment.setEnrolmentid(enrolmentid);
                RealmInstructorCourse realmInstructorCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmInstructorCourse.class).equalTo("instructorcourseid", realmAssignment.getInstructorcourseid()).findFirst();
                RealmCourse realmCourse = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmCourse.class).equalTo("courseid", realmInstructorCourse.getCourseid()).findFirst();
                realmAssignment.setCoursepath(realmCourse.getCoursepath());
                RealmSubmittedAssignment realmSubmittedAssignment = Realm.getInstance(RealmUtility.getDefaultConfig()).where(RealmSubmittedAssignment.class).equalTo("assignmentid", realmAssignment.getAssignmentid()).findFirst();
                realmAssignment.setSubmitted(realmSubmittedAssignment == null ? 0 : 1);

                if (realmAssignment.getSubmitted() == 1) {
                    realmAssignment.setSubmittedurl(realmSubmittedAssignment.getUrl());
                    realmAssignment.setSubmittedtitle(realmSubmittedAssignment.getTitle());
                    realmAssignment.setScore(realmSubmittedAssignment.getPercentagescore());
                    completedAssignments.add(realmAssignment);
                }
            }

        });

    }

    private void init() {
        populateCompletedAssignments(getContext());
        if (completedAssignments.size() == 0) {
            nodatatext.setVisibility(View.VISIBLE);
            nodatatext.setText(getString(R.string.no_submitted_assignments));
        } else {
            nodatatext.setVisibility(View.GONE);
        }
        GridLayoutManager layoutManager
                = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
        AssignmentAdapter assignmentAdapter = new AssignmentAdapter(completedAssignments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(assignmentAdapter);
    }
}
