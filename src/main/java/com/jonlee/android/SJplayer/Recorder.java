package com.jonlee.android.SJplayer;

/**
 * Created by jongyeong on 6/15/14.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * TODO Adding "Cloud folder" of Playing back YouTube bookamark
 * TODO Adding "Cloud folder" of
 */
public class Recorder {
    private static final String TAG = "Recorder";


    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    private MusicRetriever musicRetriever;

    private File rootFolder;
    // The folder current cursor is on.
    private File currentFolder;
    // MMMM-yyyy under the root folder
    private File recordTargetFolder;
    private File publicMusicFolder;
    private File folderForAllMusicByMediaScanner;
    private ArrayList<File> audibleFiles;
    // only below the ROOT and 1 level folders only
    private ArrayList<File> directories = null;

    private MainActivity mainActivity = null;
    private int currentFileIndex = 0;
    private int currentDirectoryIndex = 0;

    //Total file duration
    private int audioFileDuration = 0;

    //Manual file name
    private final String manualFile = "WELCOME.txt";
    private final String folderNameForAllMusicByMediaScanner = "All-Music-By-MediaScanner";


    /**
     * TODO: Is this correct way to pass Activity to another class?
     *
     * @param ma: MainActivity
     *            Starting from
     */
    public Recorder(MainActivity ma) {
        this.mainActivity = ma;
        // Load files in default folder
        rootFolder = getExternalSDCardDirectory();
        // Creating root_folder in case there is not.
        if (!rootFolder.exists()) {
            rootFolder.mkdir();
        }

        // Creating a virtual directory to play all musics provided by MediaScanner
        // TODO Create a file to explain that this folder won't be scanned
        folderForAllMusicByMediaScanner = new File(rootFolder.getAbsolutePath()+"/"+folderNameForAllMusicByMediaScanner);
        if(!folderForAllMusicByMediaScanner.exists())
            folderForAllMusicByMediaScanner.mkdir();

        // Creating the folder new recoding files are stored.
        recordTargetFolder = new File(rootFolder.getAbsolutePath()+"/" + this.getRecordingFolderName());
        if(!recordTargetFolder.exists()) {
            recordTargetFolder.mkdir();
        }

        if (rootFolder.listFiles().length == 0){
            String string = ma.getString(R.string.MANUAL);
            try {
                // Creating a manual
                File file = new File(rootFolder.getAbsolutePath() + File.separator + manualFile);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(string.getBytes());
                fos.flush();
                fos.close();
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

        }

        // Starting from RootFolder.
        this.currentFolder = this.rootFolder;

        // After confirming folder is in place
        this.initiateFolder();
        // Initiate folder array, files
        this.readDirectories();
        // Initiate MusicRetriever for MediaScanned files
        this.musicRetriever = new MusicRetriever(this.mainActivity.getContentResolver());
        musicRetriever.prepare();


        // InternalSDCard/Public/Music folder scanning
        // TODO Need to support by default.
        //directories.add( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

    }

    /**
     * TODO: Need to make a decision if it's really need to configurable in Settings.
     * Default value is YYYY-MM-DD-HH-MM (or +LOCATION)
     *
     * @return
     */
    public String createFileName() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd-yyyy-HHmmss", Locale.US);
        return sdf.format(c.getTime());
    }

    /**
     * Get folder name by MMMM-YYYY
     * @return
     */
    public String getRecordingFolderName() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM-yyyy",Locale.US);
        return sdf.format(c.getTime());
    }


    public int getCurrentFileIndex() {
        return this.currentFileIndex;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void isPlaying(boolean i) {
        this.isPlaying = i;
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void isRecording(boolean i) {
        this.isRecording = i;
    }

    /**
     * FF
     * seekTo between 0~mPlayer.getDuration
     * Once it's called, it's +10 on current index.
     */
    public void seek(int d) {
        //When Folder is changed, mPlayer will be NULL
        if(mPlayer != null)
            mPlayer.seekTo(mPlayer.getCurrentPosition()+d);
    }

    /**
     * Playing from the latest file to old
     */
    public void startPlaying() {
        //Log.i(TAG+"startPlaying()",currentFileIndex + ":" + files.size());

        // If there is no files, just do nothing.
        // What will be better way than this ... way?
        if(audibleFiles.size() == 0)
            return;

        Log.i(TAG,"playing?:"+getCurrentFileFullPath());
        this.isPlaying = true;



        try {
            if (!isPaused || mPlayer == null){ // Resume does not need to initiate Instance
                mPlayer = new MediaPlayer();
                //Log.i(TAG, "getCurrentFileFullPath():" + getCurrentFileFullPath());
                if(this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)) {
                    mPlayer.setDataSource(mainActivity, musicRetriever.getCurrentUri());
                }else {
                    mPlayer.setDataSource(getCurrentFileFullPath());
                }
                mPlayer.prepare();
                mPlayer.setVolume(1, 1);
                mPlayer.start();
                //Set the duration of file to use in SEEK()
                this.audioFileDuration = mPlayer.getDuration();
            } else {
                mPlayer.start();
                this.isPaused = false;
            }
        } catch (IOException e) {
            //Log.i(TAG, "prepare() failed");
            if(this.getCurrentFileName().startsWith("WELCOME.txt"))
                this.mainActivity.speak(mainActivity.getResources().getString(R.string.WELCOME));
            else
                this.mainActivity.speak(mainActivity.getResources().getString(R.string.FAIL_TO_PLAY));
            e.printStackTrace();
            this.isPlaying = false;
        }
    }

    /**
     * TODO PUBLIC_MUSIC is included by default, then the path might
     * @return
     */
    public String getCurrentFileFullPath(){
        return currentFolder.getAbsolutePath() + "/"+this.getCurrentFileName();
    }

    public void resume(){
        Log.i(TAG+"resume()",currentFileIndex + ":" + audibleFiles.size());

        if(mPlayer != null && !mPlayer.isPlaying()){
            mPlayer.start();
            this.isPlaying = true;
            this.isPaused = false;
        }
    }

    public void pause(){
        Log.i(TAG+"pause()",currentFileIndex + ":" + audibleFiles.size());

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

    public String getCurrentFileName(){
        if(this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)) {
            MusicRetriever.Item item = musicRetriever.getCurrentItem();
            return item.getArtist() + " - " + item.getTitle();
        }

        if(audibleFiles.size() > 0)
            return audibleFiles.get(this.currentFileIndex).getName();
        else
            return mainActivity.getResources().getString(R.string.NO_AUDIBLE_FILE_EXIST);
    }

    /**
     * This does not play audio, just set the currentFileIndex to the next one.
     */
    public boolean nextSong(){
        if(this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)) {
            musicRetriever.next();
            return true;
        }

//        for(int i = 0;i < audibleFiles.size() ; i++)
//            Log.i(TAG,">>>" + audibleFiles.get(i).getAbsolutePath());

        if (audibleFiles.size() == 0)
            return false;

        //If it's the end of files, it moves to the first one.
        //Turn around!
        if (currentFileIndex == audibleFiles.size() - 1)
            currentFileIndex = 0;
        else
            currentFileIndex = currentFileIndex+1;

        return true;
    }


    public boolean previousSong(){
        if(this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)) {
            musicRetriever.previous();
            return true;
        }
        //Log.i(TAG + ">>>1 previousSong", currentFileIndex + ":" + audibleFiles.size());
        if (audibleFiles.size() == 0)
            return false;

        // Turn-around when it's at the first of files.
        if (currentFileIndex == 0)
            currentFileIndex = audibleFiles.size() - 1;
        else
            currentFileIndex = currentFileIndex - 1;


        return true;
    }

    /**
     * Build FolderList when only the app start. As there is no function of creating Folder, there is no need to checking everytime of launch.
     * Add the ROOT folder but exclude '0' file folder.
     */
    public void readDirectories(){
        directories = new ArrayList<File>();

        //Add the ROOT folder as '0' index
        //This reads only the first level of folders
        directories.add(rootFolder);
        //Public Music directory.
        //directories.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        for(int i = 0 ; i < rootFolder.listFiles().length ; i++){
            if(rootFolder.listFiles()[i].isDirectory()) {
                directories.add(rootFolder.listFiles()[i]);
            }
        }
        // Adding the virtual folder at the end of Array
        directories.add(this.folderForAllMusicByMediaScanner);
        Log.i(TAG,"Directory count:" + directories.size());
    }

    /**
     * Move to the next folder
     * 1) list files 2) pick folders 3) get Array(folder[]) 4) set this.target = Array[selected] and initateFolder() 5) Same
     */

    public boolean nextFolder(){
        try {
            //Reset index to the first file when the folder changed
            this.currentFileIndex = 0;
            if (currentDirectoryIndex == this.directories.size() - 1)
                currentDirectoryIndex = 0;
            else
                this.currentDirectoryIndex = currentDirectoryIndex + 1;

            this.currentFolder = this.directories.get(this.currentDirectoryIndex);
            // Read folder to get Files when it's not a VirtualFolder
            if(!this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)){
                initiateFolder();
//                ((DialView) mainActivity.findViewById(R.id.dial_view)).setDirMode(DialView.DIR_MODE_4);
            } else {
//                ((DialView) mainActivity.findViewById(R.id.dial_view)).setDirMode(DialView.DIR_MODE_8);
            }

            Log.i(TAG, "currentDirectoryIndex:" + this.currentDirectoryIndex + ":Audible file size: " + audibleFiles.size()+ currentFolder.getAbsolutePath());
            return true;
        }catch(Exception e){
            return false;
        }

    }

    /**
     * Move to the previous folder
     */
    public boolean previousFolder(){
        try {
            //Reset index to the first file when the folder changed
            this.currentFileIndex = 0;
            if (currentDirectoryIndex == 0)
                currentDirectoryIndex = this.directories.size() - 1;
            else
                this.currentDirectoryIndex = currentDirectoryIndex - 1;
            this.currentFolder = this.directories.get(this.currentDirectoryIndex);

            // Read folder to get Files when it's not a VirtualFolder
            if(!this.getCurrentDirectoryName().startsWith(this.folderNameForAllMusicByMediaScanner)){
                initiateFolder();
            }

            Log.i(TAG, "currentDirectoryIndex:" + this.currentDirectoryIndex + ":Audible file size: " + audibleFiles.size()+ currentFolder.getAbsolutePath());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 1) Read only audible files(AudibleFileFilter) from current directory
     * 2) Filter out un-audible files
     * 3) Always set current Index to 0
     * 4) This method is called when -nextFolder, previousFolder
     */
    public void initiateFolder(){
        //Adding only Audible Files
        FileFilter aFilter = new AudibleFileFilter();
        this.audibleFiles = new ArrayList(Arrays.asList(this.currentFolder.listFiles(aFilter)));
        this.currentFileIndex = 0;
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
            String newFileName = recordTargetFolder.getAbsolutePath();
            newFileName += "/" + this.createFileName() + ".3gp";

            Log.i(TAG, "NewFileName:"+newFileName);
            //Log.i(TAG, "currentFileIndex:" + currentFileIndex + ": size" + this.files.size());
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
        File dir[] = ContextCompat.getExternalFilesDirs((Context) this.mainActivity, Environment.DIRECTORY_MUSIC);
//
//        Log.i(TAG, "getExternalStoragePublicDirectory:"+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
//        Log.i(TAG, "getDataDirectory:"+ Environment.getDataDirectory().getAbsolutePath());

        File targetFile = null;
        //Pick the largest free space
        long freeSize = 0;

        /**
         * Interestingly dir[] has null even without removable sdcard. This may be an issue of samsung phone.
         * TODO Test with another device and report Samsugn.
         * Tested in HTC M8 and same result...
         */

        for(int i = 0; i < dir.length && dir[i] != null ; i++ ){
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
            isRecording = false;
            // change current Folder and File index to the just saved file.
            moveToLatestRecordedLocation();
        } catch (Exception e){ //No idea what kind of Exception is coming...
            e.printStackTrace();
            this.isRecording = false;
        }

    }

    /**
     * change current Folder and File index to the just saved file.
     */
    public void moveToLatestRecordedLocation(){
       // TargetFolder is already created and existing in the Directory index because it's always checked when Recoder is instant'ed.
        for(int i=0; i < directories.size() ; i++){
            if(directories.get(i).equals(this.recordTargetFolder) ) {
                Log.i(TAG, directories.get(i).getName() + ":" + i);
                this.currentDirectoryIndex = i;
                // Set current target Folder.
                this.currentFolder = directories.get(i);
                this.initiateFolder();
                //TODO Maybe... comparing the exact file name is better way to find the index.
                this.currentFileIndex = recordTargetFolder.list().length - 1;
                return;
            }
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
        return this.currentFolder.getName();
    }


}
