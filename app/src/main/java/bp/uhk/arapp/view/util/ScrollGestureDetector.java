package bp.uhk.arapp.view.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;


/**
 * Created by vlado on 13.04.2016.
 */
public class ScrollGestureDetector implements GestureDetector.OnGestureListener
{

    private static final int SWIPE_THRESHOLD_DISTANCE = 3;

    ScrollGestureListener listener;
    GestureDetector gestureDetector;

    public ScrollGestureDetector(Context context, ScrollGestureListener listener)
    {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, this);
    }

    public void onTouchEvent(MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {

        float xDiff = e1.getX() - e2.getX();
        float yDiff = e1.getY() - e2.getY();

        if (Math.abs(distanceX) > SWIPE_THRESHOLD_DISTANCE)
        {

            if (Math.abs(xDiff) > Math.abs(yDiff))
            {
                if (xDiff > 0)
                {
                    listener.onFlingLeft();
                }
                else
                {
                    listener.onFlingRight();
                }
                return false;
            }
        }

        if (Math.abs(distanceY) > SWIPE_THRESHOLD_DISTANCE)
        {

            if (yDiff > 0)
            {
                listener.onFlingUp();
                return false;
            }
            else
            {
                listener.onFlingDown();
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {

    }
}
