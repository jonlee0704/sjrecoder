package com.example.android.basicgesturedetect;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by jongyeong on 6/14/14.
 */
public class DialActivity extends Activity {

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

}