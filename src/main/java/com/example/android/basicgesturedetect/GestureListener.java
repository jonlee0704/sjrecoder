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
import android.view.View;

import com.example.android.common.logger.Log;

public class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static final String TAG = "GestureListener";
    public MainActivity activity;
    private static int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    public int touchCnt = 0;



    private float centerX;
    private float centerY;
    private float minCircle;
    private float maxCircle;
    private float stepAngle;
    private float startAngle;
    private boolean isDragging;

    /**
     * n=8 directions control pad
     * 360-(360/n)/2~(360/n)/2
     * 1 = Bottom -> Top
     * 2 = Upper right
     * 3 = Left -> Right
     * 4 = Bottom right
     * 5 = Top -> Bottom
     * 6 = Bottom left
     * 7 = Right -> Left
     * 8 = Upper left
     */
    private final int NO_DIRECTION = 0;
    private final int BOTTOM_TOP = 1;
    private final int UP_RIGHT = 2;
    private final int LEFT_RIGHT = 3;
    private final int BOTTOM_RIGHT = 4;
    private final int TOP_BOTTOM = 5;
    private final int BOTTOM_LEFT = 6;
    private final int RIGHT_LEFT = 7;
    private final int UP_LEFT = 8;

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
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * Define the step angle in degrees for which the
     * dial will call {@link #onRotate(int)} event
     * @param angle : angle between each position
     */
    public void setStepAngle(float angle) {
        stepAngle = Math.abs(angle % 360);
    }

    /**
     * Define the draggable disc area with relative circle radius
     * based on min(width, height) dimension (0 = center, 1 = border)
     * @param radius1 : internal or external circle radius
     * @param radius2 : internal or external circle radius
     */
    public void setDiscArea(float radius1, float radius2) {
        radius1 = Math.max(0, Math.min(1, radius1));
        radius2 = Math.max(0, Math.min(1, radius2));
        minCircle = Math.min(radius1, radius2);
        maxCircle = Math.max(radius1, radius2);
    }

    /**
     * Check if touch event is located in disc area
     * @param touchX : X position of the finger in this view
     * @param touchY : Y position of the finger in this view
     */
    private boolean isInDiscArea(float touchX, float touchY) {
        float dX2 = (float) Math.pow(centerX - touchX, 2);
        float dY2 = (float) Math.pow(centerY - touchY, 2);
        float distToCenter = (float) Math.sqrt(dX2 + dY2);
        float baseDist = Math.min(centerX, centerY);
        float minDistToCenter = minCircle * baseDist;
        float maxDistToCenter = maxCircle * baseDist;
        return distToCenter >= minDistToCenter && distToCenter <= maxDistToCenter;
    }



    /**
     * TODO: Need to design 4 direction vs 8 direction command
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        this.touchCnt = e2.getPointerCount();
        float d = this.getDegreeFromCartesian(e1.getX(),e1.getY(),e2.getX(),e2.getY());
        float ta = touchAngle(e1.getX(),e1.getY(),e2.getX(),e2.getY());
        int dir = getDirection(d);
        Log.i(TAG, "LOG:"+touchCnt+": degree:"+d+": touchAngle: " + ta + ":distanceXY:"+distanceX+":"+distanceY);

        /**
         * Fling cases
         */
        if (Math.abs(distanceX) > SWIPE_MIN_DISTANCE || Math.abs(distanceY) > SWIPE_MIN_DISTANCE) {
            switch (getDirection(d)) {
                case BOTTOM_TOP:
                    if (touchCnt == 3)
                        speak(TAG, "Start recording", e1, e2);
                    else if ((touchCnt == 1))
                        speak(TAG, "Previous folder", e1, e2);
                    return true;
                case UP_RIGHT:
                    break;
                case LEFT_RIGHT:
                    if (touchCnt == 2)
                        speak(TAG, "Fast forward 2x", e1, e2);
                    else if (touchCnt == 3)
                        speak(TAG, "Fast forward 3x", e1, e2);
                    else if (touchCnt == 1)
                        speak(TAG, "Next song", e1, e2);
                    return true;
                case BOTTOM_RIGHT:
                    break;
                case TOP_BOTTOM:
                    //From Top to Bottom
                    if (touchCnt == 3)
                        speak(TAG, "Stop recording", e1, e2);
                    else if ((touchCnt == 1))
                        speak(TAG, "Next folder", e1, e2);
                    return true;
                case BOTTOM_LEFT:
                    break;
                case RIGHT_LEFT:
                    if (touchCnt == 2)
                        speak(TAG, "Rewind 2x", e1, e2);
                    else if (touchCnt == 3)
                        speak(TAG, "Rewind 3x", e1, e2);
                    else if (touchCnt == 1)
                        speak(TAG, "Previous song", e1, e2);
                    return true;
                case UP_LEFT:
                    break;
            }
        } else{
            switch (getDirection(d)) {
                case BOTTOM_TOP:
                    if (touchCnt == 3)
                        speak(TAG, "Start recording", e1, e2);
                    return true;
                case UP_RIGHT:
                case LEFT_RIGHT:

                case BOTTOM_RIGHT:
                case TOP_BOTTOM:
                case BOTTOM_LEFT:
                case RIGHT_LEFT:
                case UP_LEFT:
            }
        }

        return false;

    }


    /**
     * n=8 directions control pad
     * 360-(360/n)/2~(360/n)/2
     * 1 = Down -> Up
     * 2 = Upper right
     * 3 = Left -> Right
     * 4 = Down right
     * 5 = Up -> Down
     * 6 = Down left
     * 7 = Right -> Left
     * 8 = Upper left
     * TODO: Consider better flexility modifiable by different direction number.
     */
    private int getDirection(float angle){
        // n = 22.5 in case 8 direction
        double n = 22.5;
        if (angle > 360-n || angle < n){
            return this.BOTTOM_TOP;
        } else if (angle > n && angle < n*3){
            return this.UP_RIGHT;
        } else if (angle > n*3 && angle < n*5){
            return this.LEFT_RIGHT;
        } else if (angle > n*5 && angle < n*7){
            return this.BOTTOM_RIGHT;
        } else if (angle > n*7 && angle < n*9){
            return this.TOP_BOTTOM;
        } else if (angle > n*9 && angle < n*11){
            return this.BOTTOM_LEFT;
        } else if (angle > n*11 && angle < n*13){
            return this.RIGHT_LEFT;
        } else if (angle > n*13 && angle < n*15){
            return this.UP_LEFT;
        } else{
            return this.NO_DIRECTION;
        }
    }


    @Override
    public void onShowPress(MotionEvent e) {
        // User performed a down event, and hasn't moved yet.
        // Set the threshold not to get gesture event.
        this.SWIPE_MIN_DISTANCE = 1000;
        speak(TAG,"Start to move",e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // "Down" event - User touched the screen.
        this.SWIPE_MIN_DISTANCE = 50;
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
        return true;
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

    /**
     * Compute a touch angle in degrees from center
     * North = 0, East = 90, West = -90, South = +/-180
     * @param touchX : X position of the finger in this view
     * @param touchY : Y position of the finger in this view
     * @return angle
     */
    private float touchAngle(float touchX, float touchY, float centerX, float centerY) {
        float dX = touchX - centerX;
        float dY = centerY - touchY;
        return (float) (270 - Math.toDegrees(Math.atan2(dY, dX))) % 360 - 180;
    }


}
