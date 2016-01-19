package devspark.com.doorbell.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import devspark.com.doorbell.R;
import devspark.com.doorbell.utils.Constants;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class NotificationBuilderHelper {

    public static NotificationCompat.Builder getNotification(Context context, boolean vibrate) {

        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction(Constants.NOTIFICATION_YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 0, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        //No intent
        Intent noReceive = new Intent();
        noReceive.setAction(Constants.NOTIFICATION_NO_ACTION);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 0, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_content_text))
                .setContentIntent(pendingIntentYes)
                .addAction(R.drawable.ic_lock_open_black_24dp, context.getString(R.string.notification_open_door), pendingIntentYes)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.notification_discard), pendingIntentNo)
                .setVibrate(vibrate ? new long[]{500, 500} : new long[]{0, 0})
                .setPriority(Notification.PRIORITY_MAX)
                .extend(new NotificationCompat.WearableExtender().
                        setHintHideIcon(true).
                        setContentAction(0));
        return mBuilder;
    }

}
