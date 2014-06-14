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

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.example.android.common.logger.Log;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    public static final String TAG = "GestureListener";
    public MainActivity activity;
    private static final int SWIPE_MIN_DISTANCE = 30;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    public int touchCnt = 0;

    public GestureListener(MainActivity a){
        this.activity = a;
    }

    // BEGIN_INCLUDE(init_gestureListener)
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // Up motion completing a single tap occurred.
        Log.i(TAG, "Single Tap Up: " + e.getPointerCount());
        return true;
    }


    @Override
    public void onLongPress(MotionEvent e) {
        // Touch has been long enough to indicate a long press.
        // Does not indicate motion is complete yet (no up event necessarily)
        speak(TAG, "Read details", e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        this.touchCnt = e2.getPointerCount();


        float d = this.getDegreeFromCartesian(e1.getX(),e1.getY(),e2.getX(),e2.getY());
//        Log.i(TAG,"distanceX:"+distanceX);
//        Log.i(TAG,"distanceY:"+distanceY);


        if ((d > 225 && d < 315) && Math.abs(distanceX) > SWIPE_MIN_DISTANCE) {
            //From Right to Left
            if(touchCnt == 2)
                speak(TAG,"Rewind 2x",e1,e2);
            else if(touchCnt == 3)
                speak(TAG,"Rewind 3x",e1,e2);
            else if(touchCnt ==1 )
                speak(TAG,"Previous song",e1,e2);
            Log.i(TAG, "check#1");
            return true;
        } else if ((d > 45 && d < 135) && Math.abs(distanceX) > SWIPE_MIN_DISTANCE) {
            //From Left to Right
            if(touchCnt == 2)
                speak(TAG,"Fast forward 2x",e1,e2);
            else if(touchCnt == 3)
                speak(TAG,"Fast forward 3x",e1,e2);
            else if(touchCnt ==1 )
                speak(TAG,"Next song",e1,e2);
            return true;
        }

        if ((d > 315 || d < 45)  && Math.abs(distanceY) > SWIPE_MIN_DISTANCE) {
            //From Bottom to Top
            if(touchCnt == 3)
                speak(TAG,"Start recording",e1,e2);
            else if ((touchCnt == 1))
                speak(TAG,"Previous folder",e1,e2);
            return true;
        } else if ((d > 135 && d < 225)  && Math.abs(distanceY) > SWIPE_MIN_DISTANCE) {
            //From Top to Bottom
            if(touchCnt == 3)
                speak(TAG,"Stop recording",e1,e2);
            else if ((touchCnt == 1))
                speak(TAG,"Next folder",e1,e2);
            return true;
        }

        Log.i(TAG, "check#2");
        return false;

    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
        // Does nothing
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // User performed a down event, and hasn't moved yet.
        Log.i(TAG, "Show Press: " + e.getPointerCount());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // "Down" event - User touched the screen.
        Log.i(TAG,"Down: " + e.getPointerCount());

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // User tapped the screen twice.
        Log.i(TAG, "Double tap: " + e.getPointerCount());
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Since double-tap is actually several events which are considered one aggregate
        // gesture, there's a separate callback for an individual event within the doubletap
        // occurring.  This occurs for down, up, and move.
        Log.i(TAG, "Event within double tap");
//        speak(TAG, "Event within double tap");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // A confirmed single-tap event has occurred.  Only called when the detector has
        // determined that the first tap stands alone, and is not part of a double tap.
        speak(TAG, "Start and Stop", e);
        return true;
    }


    // END_INCLUDE(init_gestureListener)

    public void speak(String t, String w){
        activity.speak(w);
    }

    public void speak(String t, String w, MotionEvent e){
        activity.speak(w);
        Log.i(TAG, "[" + w + "]" + e.getPointerCount());

    }

    public void speak(String t, String w, MotionEvent e1, MotionEvent e2){
        activity.speak(w);
        Log.i(TAG, "[" + w + "]" + e1.getPointerCount() + ":" + e2.getPointerCount());

    }

    /**
     * Return degree
     * @param nowX
     * @param nowY
     * @param centerX
     * @param centerY
     * @return
     */
    private float getDegreeFromCartesian(float nowX, float nowY, float centerX, float centerY)
    {

        float angle = (float) Math.atan2((centerX - nowX), (centerY-nowY));
        float angleindegree = (float) (angle * 180/Math.PI);

        return 180-angleindegree;

    }
}
