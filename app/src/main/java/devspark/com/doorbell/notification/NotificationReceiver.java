package devspark.com.doorbell.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import devspark.com.doorbell.R;
import devspark.com.doorbell.utils.Constants;
import devspark.com.doorbell.wifi.WifiReceiver;
import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.requests.DoorOpenRequestTask;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class NotificationReceiver extends BroadcastReceiver implements DoorOpenRequestListener {

    private Toast mToast = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiReceiver.YES_ACTION.equalsIgnoreCase(action)) {
            new DoorOpenRequestTask(NotificationReceiver.this, context).execute();
        } else if (WifiReceiver.NO_ACTION.equalsIgnoreCase(action)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(0);
        }
    }

    @Override
    public void onRequestResult(Context context, Boolean success) {
        if (success) {
            startNotificationToast(context);
        } else {
            Toast.makeText(context, R.string.toast_request_error, Toast.LENGTH_LONG).show();
        }
    }

    private void startNotificationToast(final Context context) {
        CountDownTimer mCountDownTimer = new CountDownTimer(Constants.COUNTDOWN_TIMER_TIME, 950) {
            int mCountDown = Constants.COUNTDOWN_TIMER_TIME / 1000;

            @Override
            public void onTick(long millisUntilFinished) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(context, context.getString(R.string.toast_notification_time_remaining) + String.valueOf(mCountDown), Toast.LENGTH_SHORT);
                mToast.show();

                mCountDown--;
            }

            @Override
            public void onFinish() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(context, R.string.toast_notification_time_completed, Toast.LENGTH_SHORT);
                mToast.show();
            }
        };
        mCountDownTimer.start();
    }
}
