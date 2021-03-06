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

package com.jonlee.android.SJplayer;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jonlee.android.common.logger.Log;

public class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    public static final String TAG = "GestureListener";
    public MainActivity activity;
    private static int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    public int touchCnt = 0;

    private boolean isFired = false;

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

    /**
     * Vairables for Wheel jog controller
     * start
     */
    private float startPoint;
    private float movePoint;
    private float startDir = -1;
    private float moveDir;

    public GestureListener(MainActivity a){
        this.activity = a;
    }

    // BEGIN_INCLUDE(init_gestureListener)
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // Up motion completing a single tap occurred.
        //Log.i(TAG, "Single Tap Up: ");
        return true;
    }

    /**
     * Long press is disabled to handle onTouch event
     * @param e
     */
    @Override
    public void onLongPress(MotionEvent e) {
        // Touch has been long enough to indicate a long press.
        // Does not indicate motion is complete yet (no up event necessarily)
        // activity.cmd(Command.SPEAK_FILE_INFO);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * Define the step angle in degrees for which the
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
     * Compute a touch angle in degrees from center
     * North = 0, East = 90, West = -90, South = +/-180
     * @param touchX : X position of the finger in this view
     * @param touchY : Y position of the finger in this view
     * @return angle
     */
    private float touchAngle(float touchX, float touchY) {
        float dX = touchX - centerX;
        float dY = centerY - touchY;
        return (float) (270 - Math.toDegrees(Math.atan2(dY, dX))) % 360 - 180;
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

        if(isFired)
            return true;

        this.touchCnt = e2.getPointerCount();
        float d = this.getDegreeFromCartesian(e1.getX(),e1.getY(),e2.getX(),e2.getY());
        //float ta = touchAngle(e1.getX(),e1.getY(),e2.getX(),e2.getY());
        int dir = getDirection(d);
//        Log.i(TAG, "LOG:"+touchCnt+": degree:" + dir);

        if (touchCnt > 2){
            cmd(Command.START_RECORD);
            return true;
        }

        /**
         * Fling cases
         */
        if (Math.abs(distanceX) > SWIPE_MIN_DISTANCE || Math.abs(distanceY) > SWIPE_MIN_DISTANCE) {
            switch (getDirection(d)) {
                case BOTTOM_TOP:
                    cmd(Command.PREVIOUS_FOLDER);
                    break;
                case UP_RIGHT:
                    cmd(Command.NOTHING);
                    break;
                case LEFT_RIGHT:
                    if (touchCnt == 2)
                        cmd(Command.FAST_FORWARD_2X);
                    else if (touchCnt == 1)
                        cmd(Command.NEXT_SONG);
                    break;
                case BOTTOM_RIGHT:
                    cmd(Command.NOTHING);
                    break;
                case TOP_BOTTOM:
                    //From Top to Bottom
                    if (touchCnt == 2)
                        cmd(Command.STOP_RECORD);
                    else if ((touchCnt == 1))
                        cmd(Command.NEXT_FOLDER);
                    break;
                case BOTTOM_LEFT:
                    cmd(Command.NOTHING);
                    break;
                case RIGHT_LEFT:
                    if (touchCnt == 2)
                        cmd(Command.FAST_BACKWARD_2X);
                    else if (touchCnt == 1)
                        cmd(Command.PREVIOUS_SONG);
                    break;
                case UP_LEFT:
                    cmd(Command.NOTHING);
                    break;

            }
            /**
             * Wheel controller mode. 어렵네... 우찌하노?
             * DOWN UP -> FF
             * UP DOWN -> RE
             */
        } else {
            float startAngle = touchAngle(e1.getX(), e1.getY());
            float touchAngle = touchAngle(e2.getX(), e2.getY());

            float deltaAngle = (360 + d - startAngle + 180) % 360 - 180;
//            Log.i(TAG, "TouchDegree:"+d+" : deltaAngle:" + deltaAngle+" : startAngle:" + startAngle+" : touchAngle:" + touchAngle);
//            if(startDir == -1)
//                startDir = getHexDirection(d);


//            //Log.i(TAG, "startDir:" + startDir + ":endDir:" + getHexDirection(d));

            float x1 = e1.getX();
            float x2 = e2.getX();
            float y1 = e1.getY();
            float y2 = e2.getY();


            // TODO... real bad code. In case 8 -> 1?
            if ((x1 < x2 && y1 > y2) ||
                    (x1 < x2 && y1 < y2) ||
                    (x1 > x2 && y1 < y2) ||
                    (x1 < x2 && y1 > y2))
            {
                Log.i(TAG, "Moving clockwise...");
            } else if ((x1 > x2 && y1 < y2) ||
                    (x1 > x2 && y1 > y2) ||
                    (x1 < x2 && y1 > y2) ||
                    (x1 > x2 && y1 < y2)){
                Log.i(TAG, "Moving Anti-clockwise...");
                Log.i(TAG, x1 + ":" + x2 + ":" + y1 + ":" + y2 + ":D:" + getDirection(d));
            }
//            startDir = getHexDirection(d);

//            switch (getDirection(d)) {
//                case BOTTOM_TOP:
//                case UP_RIGHT:
//                case LEFT_RIGHT:
//                    Log.i(TAG, "FF...");
//                    break;
//                case BOTTOM_RIGHT:
//                case TOP_BOTTOM:
//                case BOTTOM_LEFT:
//                case RIGHT_LEFT:
//                    Log.i(TAG, "Rewind...");
//                    break;
//                case UP_LEFT:
//            }
            //Log.i(TAG,"Too low speed to move enough distance:" + distanceX + ":" +distanceY);
        }



        return true;
    }

    private void cmd(int c){
        this.isFired = true;
        activity.cmd(c);
    }

    private void cmd(int c, boolean isFired){
        if (!isFired)
            activity.cmd(c);
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
     * TODO: Consider better flexibility by different direction number.
     * TODO: Now, it implements only 4 direction controls
     */
    private int getDirection(float angle){
        // n = 45 in case 4 direction
        double n = 45;
        if (angle > 360-n || angle < n){
            return this.BOTTOM_TOP;
        } else if (angle > n && angle < n*3){
            return this.LEFT_RIGHT;
        } else if (angle > n*3 && angle < n*5){
            return this.TOP_BOTTOM;
        } else if (angle > n*5 && angle < n*7){
            return this.RIGHT_LEFT;
        } else{
            return this.NO_DIRECTION;
        }

        // n = 22.5 in case 8 direction
//        double n = 22.5;
//        if (angle > 360-n || angle < n){
//            return this.BOTTOM_TOP;
//        } else if (angle > n && angle < n*3){
//            return this.UP_RIGHT;
//        } else if (angle > n*3 && angle < n*5){
//            return this.LEFT_RIGHT;
//        } else if (angle > n*5 && angle < n*7){
//            return this.BOTTOM_RIGHT;
//        } else if (angle > n*7 && angle < n*9){
//            return this.TOP_BOTTOM;
//        } else if (angle > n*9 && angle < n*11){
//            return this.BOTTOM_LEFT;
//        } else if (angle > n*11 && angle < n*13){
//            return this.RIGHT_LEFT;
//        } else if (angle > n*13 && angle < n*15){
//            return this.UP_LEFT;
//        } else{
//            return this.NO_DIRECTION;
//        }
    }

    private int getHexDirection(float angle){
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
        activity.cmd(Command.NOTHING);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // "Down" event - User touched the screen.
        this.SWIPE_MIN_DISTANCE = 50;
        Log.i(TAG,"Down: ");
        this.isFired = false;
        //Down reset the start Jog event.
        this.startDir = -1;
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // User tapped the screen twice.
        //Log.i(TAG, "Double tap: " + e.getPointerCount());
        activity.cmd(Command.SPEAK_FILE_INFO);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Since double-tap is actually several events which are considered one aggregate
        // gesture, there's a separate callback for an individual event within the doubletap
        // occurring.  This occurs for down, up, and move.
        //Log.i(TAG, "Event within double tap");
//        cmd(T"Event within double tap");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // A confirmed single-tap event has occurred.  Only called when the detector has
        // determined that the first tap stands alone, and is not part of a double tap.
        //Log.i(TAG, "onSingleTapConfirmed");
        activity.cmd(Command.ONETOUCH);
        return true;
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
