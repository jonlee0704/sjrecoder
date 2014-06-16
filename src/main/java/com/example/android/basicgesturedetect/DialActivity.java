package com.example.android.basicgesturedetect;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

/**
 * Created by jongyeong on 6/14/14.
 */
public class DialActivity  extends SampleActivityBase {

    @Override
    protected void onCreate(Bundle state) {
        setContentView(new RelativeLayout(this) {
            private int value = 0;
            private TextView textView;
            {
                addView(new DialView(getContext()) {
                    {
                        // a step every 20°
                        setStepAngle(20f);
                        // area from 30% to 90%
                        setDiscArea(.30f, .90f);
                    }
                    @Override
                    protected void onRotate(int offset) {
                        textView.setText(String.valueOf(value += offset));
                    }
                }, new RelativeLayout.LayoutParams(0, 0) {
                    {
                        width = MATCH_PARENT;
                        height = MATCH_PARENT;
                        addRule(RelativeLayout.CENTER_IN_PARENT);
                    }
                });
                addView(textView = new TextView(getContext()) {
                    {
                        setText(Integer.toString(value));
                        setTextColor(Color.WHITE);
                        setTextSize(30);
                    }
                }, new RelativeLayout.LayoutParams(0, 0) {
                    {
                        width = WRAP_CONTENT;
                        height = WRAP_CONTENT;
                        addRule(RelativeLayout.CENTER_IN_PARENT);
                    }
                });
            }
        });
        super.onCreate(state);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
//        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.log_fragment);
//        msgFilter.setNext(logFragment.getLogView());
//        logFragment.getLogView().setTextAppearance(this, R.style.Log);
//        logFragment.getLogView().setBackgroundColor(Color.WHITE);
//
//        Log.i(TAG, "Ready");

    }

}