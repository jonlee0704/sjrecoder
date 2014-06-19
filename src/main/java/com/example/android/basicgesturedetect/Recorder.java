package com.example.android.basicgesturedetect;

/**
 * Created by jongyeong on 6/15/14.
 */
import android.app.Activity;
import android.os.Build;
import android.os.Vibrator;
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


    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private MediaPlayer   mPlayer = null;
    private MediaRecorder mRecorder = null;
    File target = null;
    File[] files = null;
    //Full path of file
    private String currentFileFullpath = null;
    //Only file name
    private String currentFileName = null;
    MainActivity mainActivity = null;
    private int currentFileIndex = 0;


    /**
     * TODO: Is this correct way to pass Activity to another class?
     * @param ma: MainActivity
     * Starting from
     */
    public Recorder(MainActivity ma) {
        this.mainActivity = ma;
        // Load files in default folder
        target = getExternalSDCardDirectory();

        // Creating root_folder in case there is not.
        if(!target.exists())
            target.mkdir();

        // After confirming folder is in place
        this.initiateFolder();

        //Initiate MediaRecorder
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
    }

    /**
     * TODO: Need to make a decision if it's really need to configurable in Settings.
     * Default value is YYYY-MM-DD-HH-MM (or +LOCATION)
     * @return
     */
    public String createFileName(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd-yyyy-HHmmss");
        return sdf.format(c.getTime());
    }

    public int getCurrentFileIndex(){
        return this.currentFileIndex;
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

    /**
     * Playing from the latest file to old
     */
    public void startPlaying() {
        //Log.i(TAG,"playing?:"+mPlayer.isPlaying()+":"+currentFileFullpath);
        this.isPlaying = true;

        try {
            if (!isPaused || mPlayer == null){ // Resume does not need to initiate Instance
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(currentFileFullpath);
                mPlayer.prepare();
                mPlayer.setVolume(1, 1);
                mPlayer.start();
            } else {
                mPlayer.start();
                this.isPaused = false;
            }
        } catch (IOException e) {
            Log.i(TAG, "prepare() failed");
            //e.printStackTrace();
            this.isPlaying = false;
        }
    }

    public void resume(){
        if(mPlayer != null && !mPlayer.isPlaying()){
            mPlayer.start();
            this.isPlaying = true;
            this.isPaused = false;
        }
    }

    public void pause(){
        if(mPlayer != null && this.isPlaying) {
            mPlayer.pause();
            this.isPlaying = false;
            this.isPaused = true;
        }
    }

    public void stopPlaying() {
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            this.isPlaying = false;
            this.isPaused = false;
        }
    }

    public boolean isPaused(){
        return this.isPaused;
    }

    /**
     * This does not play audio, just set the currentFileIndex to the next one.
     */
    public void nextSong(){
        if(currentFileIndex == files.length-1) {
            currentFileIndex = 0;
        } else {
            this.currentFileIndex = this.currentFileIndex + 1;
        }
        currentFileName = files[this.currentFileIndex].getName();
        currentFileFullpath = target.getAbsolutePath() + "/"+currentFileName;
        Log.i(TAG,"currentFileIndex:"+currentFileIndex+":"+currentFileFullpath);
    }

    public String getCurrentFileName(){
        return this.currentFileName;
    }

    public void previousSong(){
        if(currentFileIndex == 0) {
            currentFileIndex = files.length - 1;
        } else {
            this.currentFileIndex = this.currentFileIndex - 1;
        }
        currentFileName = files[this.currentFileIndex].getName();
        currentFileFullpath = target.getAbsolutePath() + "/"+currentFileName;
        Log.i(TAG,"currentFileIndex:"+currentFileIndex+":"+currentFileFullpath);

    }

    /**
     * Load default directory to read any new files
     * And set the first file
     */
    public void initiateFolder(){
        this.files = target.listFiles();
        this.currentFileIndex = 0;
        if(files.length > 0)
            currentFileFullpath = target.getAbsolutePath() + "/"+files[0].getName();
    }

    public void startRecording() {
        try {
            // In case setting has an option for storage, this part needs to be updated
            String newFileName = target.getAbsolutePath();
            newFileName += "/" + this.createFileName() + ".3gp";
            Log.i(TAG, newFileName);

            mRecorder.setOutputFile(newFileName);
            mRecorder.prepare();
            mRecorder.start();
            this.isRecording = true;
        } catch (IOException e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
            // Any exception sets it to False
            // Need to show why it fails.
            this.isRecording = false;
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
//            Log.i(TAG, Boolean.toString(this.isRecording) + ":" + Boolean.toString(this.isPlaying));

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

}
