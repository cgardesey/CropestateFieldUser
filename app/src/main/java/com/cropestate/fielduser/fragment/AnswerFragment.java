package com.cropestate.fielduser.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cropestate.fielduser.R;
import com.cropestate.fielduser.pojo.Question;

import static android.content.Context.MODE_PRIVATE;
import static com.cropestate.fielduser.activity.QuizActivity.questionArrayList;


/**
 * Created by Nana on 11/26/2017.
 */

public class AnswerFragment extends Fragment {




   TextView questionnumber,questionview;
   EditText correctanswerView;
   RadioButton answer1View,answer3View,answer4View,
    answer2View,answer5View;
   ImageView imageurlView;
    private String api_token;
    String questionid;
    NestedScrollView parentLayout;
    private static final String MY_LOGIN_ID =  "MY_LOGIN_ID";
    RadioGroup radiopurpose;

    private static RecyclerView.LayoutManager layoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.answerfragment, container, false);
        questionnumber = rootView.findViewById(R.id.questionnumber);
        answer1View = rootView.findViewById(R.id.answer1View);
        questionview = rootView.findViewById(R.id.questionview);
        correctanswerView = rootView.findViewById(R.id.correctanswerView);
        answer3View = rootView.findViewById(R.id.answer3View);
        answer4View = rootView.findViewById(R.id.answer4View);
        answer2View = rootView.findViewById(R.id.answer2View);
        answer5View = rootView.findViewById(R.id.answer5View);
        imageurlView = rootView.findViewById(R.id.imageurlView);
        parentLayout = rootView.findViewById(R.id.parentLayout);
        radiopurpose = rootView.findViewById(R.id.radiopurpose);
        SharedPreferences prefs =getActivity().getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);
        String userid = prefs.getString("userid", "No name defined");//"No name defined" is the default value.
        api_token = prefs.getString("api_token", "");
        Bundle args = getArguments();
        questionid = args.getString("questionid");
        questionnumber.setText(String.valueOf(args.getInt("page_position")));
        questionview.setText(args.getString("question"));
       // correctanswerView.setText(args.getString("answer"));
        answer1View.setText(args.getString("optionA"));
        answer2View.setText(args.getString("optionB"));
        answer3View.setText(args.getString("optionC"));
        answer4View.setText(args.getString("optionD"));
        answer5View.setText(args.getString("optionE"));

        radiopurpose.setOnCheckedChangeListener((group, checkedId) -> {
            int radiobuttonid = radiopurpose.getCheckedRadioButtonId();
            RadioButton rb = radiopurpose.findViewById(radiobuttonid);
            Question question = questionArrayList.get(args.getInt("page_position") - 1);
            question.setCorrectans(rb.getText().toString().equals(args.getString("answer")));
        });

        if(!args.getString("url").isEmpty())
        {
            Glide.with(getActivity()).load(args.getString("url")).apply( new RequestOptions().centerCrop()).into(imageurlView);

        }
        else {
            imageurlView.setVisibility(View.GONE);
        }
        return rootView;
    }


}
