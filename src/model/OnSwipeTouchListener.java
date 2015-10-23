package model;


import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {
    private Context context;
    private float previousX;
    private float firstX;
    private static int SWIPE_THRESHOLD;
    private float slop;

    public OnSwipeTouchListener(Context ctx, int swipeThreshold){
        context = ctx;
        SWIPE_THRESHOLD = swipeThreshold;
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();

        float dx;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                dx = x - firstX;
                if (Math.abs(dx) > slop) {
                    if (dx > 0 && Math.abs(dx) < SWIPE_THRESHOLD)
                        onSwipeRight(dx);
                    else if(dx < 0 && Math.abs(dx) > SWIPE_THRESHOLD)
                        onSwipeLeft(dx);
                }
                break;
            case MotionEvent.ACTION_UP:
                dx = x - firstX;
                if(Math.abs(dx) > SWIPE_THRESHOLD) {
                    if(dx > 0)
                        onSwipeRightRelease();
                    else
                        onSwipeLeftRelease();
                }
                else
                    onAbortAction();
                break;
            case MotionEvent.ACTION_CANCEL:
                onAbortAction();
                break;
            default: return false;
        }
        previousX = x;
        return true;
    }

    public abstract void onAbortAction();

    public abstract void onSwipeRight(float offset);

    public abstract void onSwipeLeft(float offset);

    public abstract void onSwipeRightRelease();

    public abstract void onSwipeLeftRelease();

}
