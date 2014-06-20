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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * TODO Removing folders from file list
 */
public class Recorder
{
    private static final String TAG = "AudioRecordTest";


    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private MediaPlayer   mPlayer = null;
    private MediaRecorder mRecorder = null;
    File target = null;
    ArrayList<File> files = null;
    // only below the ROOT and 1 level folders only
    ArrayList<File> directories = null;
    //Full path of file
    //TODO This value needs to be returned by method rather than completed in every different places.
    private String currentFileFullpath = null;
    //Only file name
    private String currentFileName = "No File loaded";
    MainActivity mainActivity = null;
    private int currentFileIndex = 0;
    private int currentDirectoryIndex = 0;


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
        // Initiate folder array
        this.readDirectories();

        // Update current file status, fileName and fileIndex
        if(files.size() > 0)
            this.currentFileName = files.get(currentFileIndex).getName();

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
        // If there is no files, just do nothing.
        // What will be better way than this ... way?
        if(files.size() == 0)
            return;

        //Log.i(TAG,"playing?:"+currentFileFullpath);
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
        // If there is no files, just do nothing.
        // TODO What will be better way than this ... way?
        if(files.size() == 0)
            return;


        if(currentFileIndex == files.size() - 1) {
            currentFileIndex = 0;
        } else {
            this.currentFileIndex = this.currentFileIndex + 1;
        }

        //TODO need something better
        while(files.get(this.currentFileIndex).isDirectory())
            this.currentFileIndex = this.currentFileIndex + 1;


        currentFileName = files.get(this.currentFileIndex).getName();
        currentFileFullpath = target.getAbsolutePath() + "/"+currentFileName;
        Log.i(TAG,"currentFileIndex:"+currentFileIndex+":"+currentFileFullpath);
    }

    public String getCurrentFileName(){

        return this.currentFileName;
    }

    public void previousSong(){
        // If there is no files, just do nothing.
        // What will be better way than this ... way?
        if(files.size() == 0)
            return;


        if(currentFileIndex == 0) {
            currentFileIndex = files.size() - 1;
        } else {
            this.currentFileIndex = this.currentFileIndex - 1;
        }

        //TODO need something better
        while(files.get(this.currentFileIndex).isDirectory()) {
            this.currentFileIndex = this.currentFileIndex - 1;
        }

        currentFileName = files.get(this.currentFileIndex).getName();
        currentFileFullpath = target.getAbsolutePath() + "/"+currentFileName;
        Log.i(TAG,"currentFileIndex:"+currentFileIndex+":"+currentFileFullpath);

    }

    /**
     * Build FolderList when only the app start. As there is no function of creating Folder, there is no need to checking everytime of launch.
     * Add the ROOT folder but exclude '0' file folder.
     */
    public void readDirectories(){
        directories = new ArrayList<File>();

        //Add the ROOT folder as '0' index
        directories.add(target);
        for(int i = 0 ; i < files.size() ; i++){
            if(files.get(i).isDirectory() && files.get(i).listFiles().length > 0) {
                directories.add(files.get(i));
            }
        }
        Log.i(TAG,"Directory count:" + directories.size());
    }

    /**
     * Move to the next folder
     * 1) list files 2) pick folders 3) get Array(folder[]) 4) set this.target = Array[selected] and initateFolder() 5) Same
     */

    public void nextFolder(){
        //Reset index to the first file when the folder changed
        this.currentFileIndex = 0;
        if(currentDirectoryIndex == this.directories.size()-1)
            currentDirectoryIndex = 0;
        else
            this.currentDirectoryIndex = currentDirectoryIndex + 1;

        this.target = this.directories.get(this.currentDirectoryIndex);
        Log.i(TAG,"currentDirectoryIndex:"+this.currentDirectoryIndex+":"+target.getName());

    }

    /**
     * Move to the previous folder
     */
    public void previousFolder(){
        //Reset index to the first file when the folder changed
        this.currentFileIndex = 0;
        if(currentDirectoryIndex == 0)
            currentDirectoryIndex = this.directories.size()-1;
        else
            this.currentDirectoryIndex = currentDirectoryIndex - 1;
        this.target = this.directories.get(this.currentDirectoryIndex);
        Log.i(TAG,"currentDirectoryIndex:"+this.currentDirectoryIndex+":"+target.getName());

    }

    /**
     * Load default directory to read any new files
     * And set the first file
     */
    public void initiateFolder(){
        this.files = new ArrayList(Arrays.asList(target.listFiles()));
        this.currentFileIndex = 0;
        if(files.size() > 0) {
            currentFileFullpath = target.getAbsolutePath() + "/" + files.get(0).getName();
        }
    }

    public void startRecording() {
        try {
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
            // In case setting has an option for storage, this part needs to be updated
            String newFileName = target.getAbsolutePath();
            newFileName += "/" + this.createFileName() + ".3gp";
            //Log.i(TAG, "NewFileName:"+newFileName);
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
     * Current code is not returning Removable SD card, instead, it returns the biggest Free space one.
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
     * TODO Don't know what else needs to be added more
     */
    public void stopRecording() {
        try {
//            Log.i(TAG, Boolean.toString(this.isRecording) + ":" + Boolean.toString(this.isPlaying));

            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            this.isRecording = false;
            initiateFolder();
            //Move to the last file which just recorded.
            this.currentFileIndex = this.files.size() - 1;
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
     * Return current folder name to let user know where it is.
     */
    public String getCurrentDirectoryName(){
        return this.target.getName();
    }


}
