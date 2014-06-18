package com.example.android.basicgesturedetect;

/**
 * Created by jongyeong on 6/15/14.
 */
import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * TODO Removing 'extends Activity' which is only for testing purpose
 */
public class Recorder extends Activity
{
    private static final String TAG = "AudioRecordTest";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    private boolean isRecording = false;
    private boolean isPlaying = false;

    MainActivity mainActivity = null;

    /**
     * TODO: Is this correct way to pass Activity to another class?
     * @param ma
     */
    public Recorder(MainActivity ma) {
        this.mainActivity = ma;
    }

    /**
     * TODO: Need to make a decision if it's really need to configurable in Settings.
     * Default value is YYYY-MM-DD-HH-MM (or +LOCATION)
     * @return
     */
    public String createFileName(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd-yyyy-HHmmss");
        String strDate = sdf.format(c.getTime());
        return strDate;
    }

    public boolean isPlaying(){
        return this.isPlaying;
    }

    public void isPlaying(boolean i){
        this.isPlaying = i;
    }

    public boolean isRecording(){
        return this.isRecording;
    }

    public void isRecording(boolean i){
        this.isRecording = i;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    /**
     * Playing from the latest file to old
     */
    public void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            this.isPlaying = true;
        } catch (IOException e) {
            Log.i(TAG, "prepare() failed");
            this.isPlaying = false;
        }
    }

    public void stopPlaying() {
        this.isPlaying = false;
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean startRecording() {
        try {
            this.isRecording = true;
            File target = getExternalSDCardDirectory();
            // Creating folder in case there is not.
            if(!target.exists())
                target.mkdir();
            // In case setting has an option for storage, this part needs to be updated
            mFileName = target.getAbsolutePath();
            mFileName += "/" + this.createFileName() + ".3gp";
            Log.i(TAG, mFileName);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // SamplingRate needs to be moved into Settings
            if (Build.VERSION.SDK_INT >= 10) {
                mRecorder.setAudioSamplingRate(16000);
                mRecorder.setAudioEncodingBitRate(96000);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            } else {
                // older version of Android, use crappy sounding voice codec
                // Not that neccessary code here. Or something inside of Exception part.
                mRecorder.setAudioSamplingRate(8000);
                mRecorder.setAudioEncodingBitRate(12200);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            mRecorder.setOutputFile(mFileName);
            mRecorder.prepare();
            mRecorder.start();
            return true;
        } catch (IOException e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
            // Any exception sets it to False
            // Need to show why it fails.
            this.isRecording = false;
        } finally {
            // Any exception, it returns false;
            return false;
        }

    }

    /**
     * Current code is not returning Removable SD card, instead, it returns the biggist Free space one.
     * @return File
     */
    public File getExternalSDCardDirectory()
    {
        File dir[] = ContextCompat.getExternalFilesDirs((Context)this.mainActivity,Environment.DIRECTORY_MUSIC);
        File targetFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);;
        //Pick the largest free space
        long freeSize = 0;
        for(int i = 0; i < dir.length ; i++ ){
            if (dir[i].getFreeSpace() > freeSize) {
                targetFile = dir[i];
                freeSize = dir[i].getFreeSpace();
            }
        }
        //Log.i(this.TAG, "targetFile:" + targetFile.getAbsolutePath().toString() + ":" + targetFile.getFreeSpace());
        return   targetFile;
    }

    /**
     * TODO Don't know whatelse needs to be added more
     */
    public void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            this.isRecording = false;
        } catch (Exception e){ //No idea what kind of Exception is coming...
            e.printStackTrace();
            this.isRecording = false;
        }
    }

    /**
     * Checks if external storage is available for read and write
     * Not being used now. 
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * only for testing...
     */

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }



    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        setContentView(ll);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
