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
    //public String ttsString = "Hello Sangjoon, welcome to SJ recorder";
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
                    ttobj.setLanguage(Locale.US);
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
//
//    public void setTtsString(String str){
//        this.ttsString = str;
//    }

    /**
     * Let TTS speaks
     * @param w
     */
    public void speak(String w) {
        //TODO Chang QUEUE_ADD to QUEUE_FLUSH
        ttobj.speak(w, TextToSpeech.QUEUE_FLUSH, null);
        //textView.append("Command: " + w + "\n");
    }

    public void vibrate(){
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(70);
    }

    /**
     * TODO Needs to be graphical effect to make it prettier.
     * @param w
     */
    public void displayText(String w){
        textView.setText(w);
    }

    /**
     * Command
     * var c = Command.{CONSTANT}
     */
    public boolean cmd(int c){
        String cmdStr = "Invalid command";
        vibrate();
        //Stop speaking when new action is coming
        speak("");

        //Log.i(TAG,"cmd:"+c);

        // If it's on recording, Speak "On air now and QUIT"
        // TODO Would like to consider to STOP by any touch events to make it easier.
        if(recorder.isRecording()) {
            if (c == Command.ONETOUCH || c == Command.STOP_RECORD) {
                recorder.stopRecording();
                cmdStr = getResources().getString(R.string.STOP_RECORD);
            }else {
                cmdStr = getResources().getString(R.string.ALREADY_RECORD);
            }
            displayText(cmdStr);
        } else {
            switch (c) {
                case Command.START_RECORD:
                    if(!recorder.isPaused()){
                        recorder.pause();
                    }
                    recorder.startRecording();
                    cmdStr = getResources().getString(R.string.START_RECORD);
                    this.displayText(cmdStr);
                    break;
                case Command.ONETOUCH:
                    Log.i(TAG, "isPaused:"+recorder.isPaused()+":isPlaying:"+recorder.isPlaying());
                    if (recorder.isPlaying() && !recorder.isPaused()) {
                        recorder.pause();
                        cmdStr = getResources().getString(R.string.PAUSE);
                    } else if(!recorder.isPlaying() && recorder.isPaused()) {
                            recorder.resume();
                            cmdStr = getResources().getString(R.string.RESUME);
                    } else if(!recorder.isPlaying() && !recorder.isPaused()) {
                        recorder.startPlaying();
                        cmdStr = getResources().getString(R.string.START_PLAYBACK);
                    }
                    this.displayText(cmdStr + ": " + recorder.getCurrentFileName());
                    break;
                case Command.NEXT_SONG:
                    cmdStr = getResources().getString(R.string.NEXT_SONG);
                    recorder.stopPlaying();
                    recorder.nextSong();
                    recorder.startPlaying();
                    this.displayText(cmdStr + ": " + recorder.getCurrentFileName());
                    break;
                case Command.PREVIOUS_SONG:
                    cmdStr = getResources().getString(R.string.PREVIOUS_SONG);
                    recorder.stopPlaying();
                    recorder.previousSong();
                    recorder.startPlaying();
                    this.displayText(cmdStr + ": " + recorder.getCurrentFileName());
                    break;
                case Command.NEXT_FOLDER:
                    recorder.stopPlaying();
                    recorder.nextFolder();
                    recorder.initiateFolder();
                    cmdStr = getResources().getString(R.string.NEXT_FOLDER);
                    this.displayText(cmdStr + ": " + recorder.getCurrentDirectoryName());
                    speak("Folder, " + recorder.getCurrentDirectoryName());
                    break;
                case Command.PREVIOUS_FOLDER:
                    recorder.stopPlaying();
                    recorder.previousFolder();
                    recorder.initiateFolder();
                    cmdStr = getResources().getString(R.string.PREVIOUS_FOLDER);
                    this.displayText(cmdStr + ": " + recorder.getCurrentDirectoryName());
                    speak("Folder, " + recorder.getCurrentDirectoryName());
                    break;
                case Command.FAST_FORWARD_2X:
                    cmdStr = getResources().getString(R.string.FAST_FORWARD_2X);
                    break;
                case Command.FAST_BACKWARD_2X:
                    cmdStr = getResources().getString(R.string.FAST_BACKWARD_2X);
                    break;
                case Command.SPEAK_FILE_INFO:
                    cmdStr = getResources().getString(R.string.SPEAK_FILE_INFO);
                    String state = "";
                    if(recorder.isPlaying())
                        state = "Playing '";
                    else
                        state = "Paused '";
                    this.speak(state + recorder.getCurrentFileName() + "'");
                    break;
                case Command.NOTHING:
                    cmdStr = "SangJoon, SON!!!";
                    break;
            }
        }
        //speak(cmdStr);
        return true;
    }
}
