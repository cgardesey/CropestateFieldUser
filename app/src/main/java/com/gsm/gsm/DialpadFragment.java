package com.gsm.gsm;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.dialer.dialpadview.DialpadView;
import com.android.dialer.dialpadview.DigitsEditText;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.cropestate.fielduser.R;

public class DialpadFragment extends Fragment {
    public final static String EXTRA_REGION_CODE = "EXTRA_REGION_CODE";
    public final static String EXTRA_FORMAT_AS_YOU_TYPE = "EXTRA_FORMAT_AS_YOU_TYPE";
    public final static String EXTRA_ENABLE_STAR = "EXTRA_ENABLE_STAR";
    public final static String EXTRA_ENABLE_POUND = "EXTRA_ENABLE_POUND";
    public final static String EXTRA_ENABLE_PLUS = "EXTRA_ENABLE_PLUS";
    public final static String EXTRA_CURSOR_VISIBLE = "EXTRA_CURSOR_VISIBLE";

    private final static String DEFAULT_REGION_CODE = "US";

    private DigitsEditText digits;
    private AsYouTypeFormatter formatter;
    private String input = "";
    private Callback callback;
    private String regionCode = DEFAULT_REGION_CODE;
    private boolean formatAsYouType = true;
    private boolean enableStar = true;
    private boolean enablePound = true;
    private boolean enablePlus = true;
    private boolean cursorVisible = false;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Bundle arguments = savedInstanceState;
        if (arguments == null) {
            arguments = getArguments();
        }
        if (arguments != null) {
            regionCode = arguments.getString(EXTRA_REGION_CODE, DEFAULT_REGION_CODE);
            formatAsYouType = arguments.getBoolean(EXTRA_FORMAT_AS_YOU_TYPE, formatAsYouType);
            enableStar = arguments.getBoolean(EXTRA_ENABLE_STAR, enableStar);
            enablePound = arguments.getBoolean(EXTRA_ENABLE_POUND, enablePound);
            enablePlus = arguments.getBoolean(EXTRA_ENABLE_PLUS, enablePlus);
            cursorVisible = arguments.getBoolean(EXTRA_CURSOR_VISIBLE, cursorVisible);
        }

        View view = inflater.inflate(R.layout.dialpad_fragment, container, false);
        DialpadView dialpadView = view.findViewById(R.id.dialpad_view);
        dialpadView.setShowVoicemailButton(false);

        digits = (DigitsEditText) dialpadView.getDigits();
        digits.setCursorVisible(cursorVisible);
        dialpadView.findViewById(R.id.zero).setOnClickListener(view1 -> append('0'));
        if (enablePlus) {
            dialpadView.findViewById(R.id.zero).setOnLongClickListener(view12 -> {
                append('+');
                return true;
            });
        }
        dialpadView.findViewById(R.id.one).setOnClickListener(view13 -> append('1'));
        dialpadView.findViewById(R.id.two).setOnClickListener(view15 -> append('2'));
        dialpadView.findViewById(R.id.three).setOnClickListener(view14 -> append('3'));
        dialpadView.findViewById(R.id.four).setOnClickListener(view16 -> append('4'));
        dialpadView.findViewById(R.id.four).setOnClickListener(view18 -> append('4'));
        dialpadView.findViewById(R.id.five).setOnClickListener(view17 -> append('5'));
        dialpadView.findViewById(R.id.six).setOnClickListener(view19 -> append('6'));
        dialpadView.findViewById(R.id.seven).setOnClickListener(view110 -> append('7'));
        dialpadView.findViewById(R.id.eight).setOnClickListener(view111 -> append('8'));
        dialpadView.findViewById(R.id.nine).setOnClickListener(view112 -> append('9'));
        if (enableStar) {
            dialpadView.findViewById(R.id.star).setOnClickListener(view113 -> append('*'));
        } else {
            dialpadView.findViewById(R.id.star).setVisibility(View.GONE);
        }
        if (enablePound) {
            dialpadView.findViewById(R.id.pound).setOnClickListener(view114 -> append('#'));
        } else {
            dialpadView.findViewById(R.id.pound).setVisibility(View.GONE);
        }
        dialpadView.getDeleteButton().setOnClickListener(view115 -> poll());
        dialpadView.getDeleteButton().setOnLongClickListener(view116 -> {
            clear();
            return true;
        });

        // if region code is null, no formatting is performed
        formatter = PhoneNumberUtil.getInstance()
                .getAsYouTypeFormatter(formatAsYouType ? regionCode : "");

//        view.findViewById(R.id.fab_ok).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (callback != null) {
//                    callback.ok(digits.getText().toString(), input);
//                }
//            }
//        });

        digits.setOnTextContextMenuClickListener(
                id -> {
                    String string = digits.getText().toString();
                    clear();
                    for (int i = 0; i < string.length(); i++) {
                        append(string.charAt(i));
                    }
                });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_REGION_CODE, regionCode);
        outState.putBoolean(EXTRA_FORMAT_AS_YOU_TYPE, formatAsYouType);
        outState.putBoolean(EXTRA_ENABLE_STAR, enableStar);
        outState.putBoolean(EXTRA_ENABLE_POUND, enablePound);
        outState.putBoolean(EXTRA_ENABLE_PLUS, enablePlus);
        outState.putBoolean(EXTRA_CURSOR_VISIBLE, cursorVisible);
    }

    private void poll() {
        if (!input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionCode);
            if (formatAsYouType) {
                digits.setText("");
                for (char c : input.toCharArray()) {
                    digits.setText(formatter.inputDigit(c));
                }
            } else {
                digits.setText(input);
            }
        }
    }

    private void clear() {
        formatter.clear();
        digits.setText("");
        input = "";
    }

    private void append(char c) {
        input += c;
        if (formatAsYouType) {
            digits.setText(formatter.inputDigit(c));
        } else {
            digits.setText(input);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callback) {
            callback = (Callback) context;
        }
    }

    public interface Callback {
        void ok(String formatted, String raw);
    }
}
