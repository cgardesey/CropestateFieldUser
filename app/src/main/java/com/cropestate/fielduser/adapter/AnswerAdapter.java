package com.cropestate.fielduser.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.cropestate.fielduser.fragment.AnswerFragment;
import com.cropestate.fielduser.pojo.Question;

import java.util.ArrayList;

/**
 * Created by Nana on 10/23/2017.
 */

public class AnswerAdapter extends FragmentStatePagerAdapter {

    protected Context mContext;

    ArrayList<Question>questionArrayList;



    public AnswerAdapter(FragmentManager fm, Context context, ArrayList<Question>questionArrayList1) {
        super(fm);
        mContext = context;
       this.questionArrayList = questionArrayList1;
    }

    @Override

    public Fragment getItem(int position) {
        Fragment fragment = new AnswerFragment();
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);
        args.putString("questionid", questionArrayList.get(position).getQuestionid());
        args.putString("question", questionArrayList.get(position).getQuestion());
        args.putString("url", questionArrayList.get(position).getUrl());
        args.putString("answer", questionArrayList.get(position).getAnswer());
        args.putString("optionA", questionArrayList.get(position).getOptionA());
        args.putString("optionB", questionArrayList.get(position).getOptionB());
        args.putString("optionC", questionArrayList.get(position).getOptionC());
        args.putString("optionD", questionArrayList.get(position).getOptionD());
        args.putString("optionE", questionArrayList.get(position).getOptionE());
        args.putString("optionC", questionArrayList.get(position).getOptionC());
       Log.d("qus",questionArrayList.get(position).toString());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return questionArrayList.size();
    }
}