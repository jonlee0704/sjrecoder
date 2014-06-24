package com.example.android.basicgesturedetect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.common.logger.Log;

/**
 * Created by jongyeong on 6/14/14.
 */
public abstract class DialView extends View {

    private float centerX;
    private float centerY;
    private float minCircle;
    private float maxCircle;
    private float stepAngle = 1;

    public static final String TAG = "DialView.OnTouchListener";


    public DialView(Context context) {

        super(context);
        stepAngle = 1;

        /**
         * TODO Considering to implement a specific evenListner for dialer and gesture
         */
        setOnTouchListener(new OnTouchListener() {

            private float startAngle;
            private boolean isDragging;
            private int touchCnt;

            public static final String TAG = "GestureListener";
            public MainActivity activity = (MainActivity)getContext();

            //TODO Need to optimize THRESHOLD numbers to tell single tap or drag and so on.
            private int SWIPE_MIN_DISTANCE = 25;
            private static final int LONGPRESS_THRESHOLD = 170; //millie seconds

            private boolean isFired = false;
            private boolean isLongpress = false;

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

            // For ACTION_UP
            float startX = 0;
            float startY = 0;
            // To check long-press to turn on Dial Mode
            long startAction = 0;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // For ACTION_MOVE
                float touchX1 = event.getX();
                float touchY1 = event.getY();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // Checking direction in ACTION_UP
                        startX = event.getX();
                        startY = event.getY();
                        startAngle = touchAngle(touchX1, touchY1);
                        isDragging = isInDiscArea(touchX1, touchY1);

                        // Start time of TouchPress without moving
                        this.startAction = System.currentTimeMillis();

                        // ACTION_DOWN and ACTION_UP isFired.?
                        isFired = false;

                        // Initiate Longpress mode
                        isLongpress = false;

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        this.touchCnt = event.getPointerCount();
//                        if(isFired)
//                            return false;
                        //Long Press!!! Not enough movement during threshold time.
                        // Checking only when iLongpress is false.
                        if (!isLongpress && (System.currentTimeMillis() - startAction) > LONGPRESS_THRESHOLD &&
                                (Math.abs(startX-event.getX()) < SWIPE_MIN_DISTANCE && Math.abs(startY-event.getY()) < SWIPE_MIN_DISTANCE)) {
                            //Log.i(TAG, "Touch duration: " + (System.currentTimeMillis() - startAction) +":"+Math.abs(startX-event.getX())+":"+Math.abs(startY-event.getY()));
                            this.isLongpress = true;
                            //To make vibration when Longpress is recognized.
                            cmd(Command.LONG_PRESS);
                        }

                        if (isDragging && isLongpress ) {
                            float touchAngle = touchAngle(touchX1, touchY1);
                            float deltaAngle = (360 + touchAngle - startAngle + 180) % 360 - 180;
                            //Log.i(TAG,"touchAngle:"+touchAngle+":startAngle:"+startAngle);
                            if (Math.abs(deltaAngle) > stepAngle) {
                                int offset = (int) deltaAngle / (int) stepAngle;
                                startAngle = touchAngle;
                                onRotate(offset);
//                                Log.i(TAG, "ACTION_MOVE>>touchAngle:"+touchAngle+":startAngle:"+startAngle+":"+event.getPointerCount());
                            }
                            isFired = true;
                            return true;
                        } else{
                            return false;
                        }
                    case MotionEvent.ACTION_SCROLL:
                    case MotionEvent.ACTION_UP:
                        // If event is already fired, it skips.
                        if(isFired)
                            return false;

                        Log.i(TAG,"duration:" + (System.currentTimeMillis() - startAction));

                        // If it's too short from DOWN to UP with not enough distance, it's SINGLE TAP.
                        if ((System.currentTimeMillis() - startAction) < LONGPRESS_THRESHOLD  &&
                                (Math.abs(startX-event.getX()) < SWIPE_MIN_DISTANCE && Math.abs(startY-event.getY()) < SWIPE_MIN_DISTANCE)) {
                            activity.cmd(Command.ONETOUCH);
                            return true;
                        }

                        if((Math.abs(startX-event.getX()) > SWIPE_MIN_DISTANCE && Math.abs(startY-event.getY()) > SWIPE_MIN_DISTANCE)){

                            int dir = getDirection(getDegreeFromCartesian(startX,startY,event.getX(),event.getY()));
                            switch (dir) {
                                case BOTTOM_TOP:
                                    if (touchCnt >= 2)
                                        cmd(Command.START_RECORD);
                                    else
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
                            Log.i(TAG,"Direction:" + dir);
                            return true;
                        }
                    case MotionEvent.ACTION_CANCEL:
                        isDragging = false;
                        isFired = false;
                        isLongpress = false;
                        break;
                }
                return false;
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
                //Log.i(TAG, "X1/Y1:"+nowX+"/"+centerX+" Y1/Y2:" + nowY + "/" + centerY);
                float angle = (float) Math.atan2((centerX - nowX), (centerY-nowY));
                float angleindegree = (float) (angle * 180/Math.PI);

                return 180-angleindegree;

            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        centerX = getMeasuredWidth() / 2f;
        centerY = getMeasuredHeight() / 2f;
        super.onLayout(changed, l, t, r, b);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        float radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2f;
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFFFFFF);
        paint.setXfermode(null);
        LinearGradient linearGradient = new LinearGradient(
                radius, 0, radius, radius, 0xFFFFFFFF, 0xFFEAEAEA, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawCircle(centerX, centerY, maxCircle * radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawCircle(centerX, centerY, minCircle * radius, paint);
        paint.setXfermode(null);
        paint.setShader(null);
        paint.setColor(0x15000000);
        for (int i = 0, n =  360 / (int) stepAngle; i < n; i++) {
            double rad = Math.toRadians((int) stepAngle * i);
            int startX = (int) (centerX + minCircle * radius * Math.cos(rad));
            int startY = (int) (centerY + minCircle * radius * Math.sin(rad));
            int stopX = (int) (centerX + maxCircle * radius * Math.cos(rad));
            int stopY = (int) (centerY + maxCircle * radius * Math.sin(rad));
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
        super.onDraw(canvas);
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

    protected abstract void onRotate(int offset);

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