/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/




package com.example.android.basicgesturedetect;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.util.Locale;

import android.view.View;
import android.widget.TextView;
import android.os.Vibrator;
import android.content.Context;
import android.view.WindowManager;
import android.view.Window;



/**
 * A simple launcher activity containing a summary sample description
 * and a few action bar buttons.
 */
public class MainActivity extends SampleActivityBase{

    public static final String TAG = "MainActivity";
    public static final String FRAGTAG = "BasicGestureDetectFragment";
    public TextToSpeech ttobj = null;
    public String ttsString = "Hello Sangjoon, welcome to SJ recorder";
    public TextView textView = null;
    public View dialView = null;
    public Recorder recorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.sample_output);
        dialView = (View) findViewById(R.id.dial_view);

        if (getSupportFragmentManager().findFragmentByTag(FRAGTAG) == null ) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BasicGestureDetectFragment fragment = new BasicGestureDetectFragment();
            fragment.setActivity(this);
            transaction.add(fragment, FRAGTAG);
            transaction.commit();
        }

        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    ttobj.setLanguage(Locale.UK);
                }

            }
        }
        );

        // Create Recorder
        recorder = new Recorder(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    public void setTtsString(String str){
        this.ttsString = str;
    }

    /**
     * Let TTS speaks
     * @param w
     */
    public void speak(String w) {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(100);

        this.ttsString = w;
        ttobj.speak(ttsString, TextToSpeech.QUEUE_FLUSH, null);

        //TextView textView = (TextView) findViewById(R.id.sample_output);
        textView.setText("Command: " + w);
        // TESTING...
        if (w.startsWith("Start recording")) {
            if (!recorder.isRecording())
                recorder.startRecording();
        } else if (w.startsWith("Start and Stop")) {
            if (recorder.isRecording())
                recorder.stopRecording();
            else if (recorder.isPlaying())
                recorder.stopPlaying();
        } else if (w.startsWith("Start and Stop")) {
            recorder.startPlaying();
        }
    }

    /**
     * Command
     * var c = Command.{CONSTANT}
     */
    public boolean cmd(int c){
        switch( c ){
            case Command.START_RECORD:
                if (!recorder.isRecording())
                    recorder.startRecording();
            case Command.STOP:
                if (recorder.isRecording())
                    recorder.stopRecording();
                else if (recorder.isPlaying())
                    recorder.stopPlaying();
            case Command.NEXT_SONG:
            case Command.PREVIOUS_SONG:
            case Command.NEXT_FOLDER:
            case Command.PREVIOUS_FOLDER:
            case Command.FAST_FORWARD_2X:
            case Command.FAST_BACKWARD_2X:
            case Command.SPEAK_FILE_INFO:
        }
        return false;
    }
}
