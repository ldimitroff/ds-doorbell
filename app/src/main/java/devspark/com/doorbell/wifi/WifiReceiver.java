package devspark.com.doorbell.wifi;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import devspark.com.doorbell.notification.NotificationBuilderHelper;
import devspark.com.doorbell.utils.GoogleApiHelper;
import devspark.com.doorbell.utils.SPHelper;
import devspark.com.doorbellcommons.Constants;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (this) { // blocks "this" from here ....
            boolean notificationEnabled = SPHelper.get().isNotificationEnabled();

            if (notificationEnabled) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null && info.isConnected()) {

                    if (WIfiHelper.isDSWifiConnected(context)) {
                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        boolean vibrationEnabled = SPHelper.get().isVibrationEnabled();
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(0, NotificationBuilderHelper.getNotification(context, vibrationEnabled).build());
                        new GoogleApiHelper().sendMessage(context, Constants.PATH_NOTIFICATION, "");
                    }
                }
            }
        }  // to here...

    }

}