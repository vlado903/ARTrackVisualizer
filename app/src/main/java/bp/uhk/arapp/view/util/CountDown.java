package bp.uhk.arapp.view.util;

import android.os.CountDownTimer;

/**
 * Created by vlado on 13.04.2016.
 */
public class CountDown extends CountDownTimer
{

    CountDownCallback callback;

    public CountDown(long millis, long interval, CountDownCallback callback)
    {
        super(millis, interval);
        this.callback = callback;
    }

    @Override
    public void onTick(long remaining)
    {
        callback.countDownTick((int) (remaining / 1000));
    }

    @Override
    public void onFinish()
    {
        callback.countDownFinish();
    }
}
