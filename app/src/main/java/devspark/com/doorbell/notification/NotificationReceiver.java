package devspark.com.doorbell.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import devspark.com.doorbell.R;
import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.requests.DoorOpenRequestTask;
import devspark.com.doorbell.utils.DoorOpenResult;
import devspark.com.doorbell.utils.FlurryAnalyticHelper;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbellcommons.Utils;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class NotificationReceiver extends BroadcastReceiver implements DoorOpenRequestListener {

    private Toast mToast = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (PhoneConstants.NOTIFICATION_YES_ACTION.equalsIgnoreCase(action)) {
            new DoorOpenRequestTask(NotificationReceiver.this, context).execute();
            FlurryAnalyticHelper.logDoorOpenEvent(FlurryAnalyticHelper.FROM_NOTIFICATION);
        } else if (PhoneConstants.NOTIFICATION_NO_ACTION.equalsIgnoreCase(action)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(0);
        }
    }

    @Override
    public void onRequestResult(Context context, DoorOpenResult result) {
        if (result == DoorOpenResult.TRUE) {
            startNotificationToast(context);
        } else {
            String errorMsg;
            if (result == DoorOpenResult.BUSY) {
                errorMsg = context.getResources().getString(R.string.toast_request_door_busy);
            } else {
                errorMsg = context.getResources().getString(R.string.toast_request_error);
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
        Utils.vibrate(context, result == DoorOpenResult.TRUE);
    }

    private void startNotificationToast(final Context context) {
        CountDownTimer mCountDownTimer = new CountDownTimer(PhoneConstants.COUNTDOWN_TIMER_TIME, 950) {
            int mCountDown = PhoneConstants.COUNTDOWN_TIMER_TIME / 1000;

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
