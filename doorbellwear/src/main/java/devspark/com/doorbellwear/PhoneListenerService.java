package devspark.com.doorbellwear;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import devspark.com.doorbellcommons.Constants;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class PhoneListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("test", messageEvent.getPath() + "  " + messageEvent.getData().toString());
        if (messageEvent.getPath().equals(Constants.PATH_REQUEST_RESULT)) {
            final String message = new String(messageEvent.getData());
            boolean success = message.equalsIgnoreCase(Constants.PATH_REQUEST_RESULT_SUCCESS);

            Intent intent = new Intent(Constants.DOOR_OPEN_REQUEST_RESULT_INTENT);
            intent.putExtra(Constants.DOOR_OPEN_REQUEST_RESULT_MESSAGE, success);
            LocalBroadcastManager.getInstance(DevsparkWearApp.getContext()).sendBroadcast(intent);
        } else if (messageEvent.getPath().equals(Constants.PATH_NOTIFICATION)) {
            createNotification();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    public static void createNotification() {

        Intent notificationIntent = new Intent(DevsparkWearApp.getContext(), WearActivity.class);
        notificationIntent.putExtra(Constants.NOTIFICATION_BTN_CLICK_EXTRA, true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(DevsparkWearApp.getContext(), 0,
                notificationIntent, 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(DevsparkWearApp.getContext());
        b.setContentText("Abrir Puerta");
        b.setSmallIcon(R.mipmap.ic_launcher);
        b.setContentTitle("DevSpark");
        b.setContentIntent(pendingIntent);
        b.setVibrate(new long[]{200, 300});
        b.setLocalOnly(true);
        b.setPriority(Notification.PRIORITY_MAX);
//        b.addAction(R.drawable.ic_devspark, "Abrir Puerta", pendingIntent);
        b.extend(new NotificationCompat.WearableExtender()
                .setContentAction(0)
                .addAction(new NotificationCompat.Action(R.drawable.ic_devspark, "Abrir Puerta", pendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.ic_devspark, "Abrir Puerta", pendingIntent))
                .setHintShowBackgroundOnly(true)
                .setHintHideIcon(true)
                .setBackground(BitmapFactory.decodeResource(DevsparkWearApp.getContext().getResources(), R.drawable.devspark_logo))
        );
        NotificationManagerCompat man = NotificationManagerCompat.from(DevsparkWearApp.getContext());
        man.notify(0, b.build());
    }
}
