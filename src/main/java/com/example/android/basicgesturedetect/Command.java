package com.example.android.basicgesturedetect;

/**
 * Created by jongyeong on 6/17/14.
 */
public class Command {
    public final static int START_RECORD = 1;
    public final static int STOP_RECORD = 2;
    public final static int START_PLAYBACK = 3;
    public final static int STOP_PLAYBACK = 4;
    public final static int FAST_FORWARD_2X = 5;
    public final static int FAST_BACKWARD_2X = 6;
    public final static int JOG_CLOCK_WISE = 7;
    public final static int JOG_ANTI_CLOCK_WISE = 8;
    public final static int NEXT_SONG = 9;
    public final static int PREVIOUS_SONG = 10;
    public final static int NEXT_FOLDER = 11;
    public final static int PREVIOUS_FOLDER = 12;
    public final static int SPEAK_FILE_INFO = 13;
    public final static int ONETOUCH = 14;
    public final static int FAST_FORWARD_3X = 15;
    public final static int FAST_BACKWARD_3X = 16;
    public final static int NEXT_DAY = 17;
    public final static int PREVIOUS_DAY = 18;
    public final static int PAUSE = 19;
    public final static int NOTHING = 100;


    public Command(){}

    /**
     * TODO Is this good idea to implement String resource here? Or "res/String" might be better?
     * @param c
     * @return
     */
//    public static String getString(int c){
//        switch(c) {
//            case START_RECORD:
//        }
//    }
}
