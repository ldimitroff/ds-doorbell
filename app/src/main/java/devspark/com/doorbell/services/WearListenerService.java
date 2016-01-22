package devspark.com.doorbell.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import devspark.com.doorbell.DevsparkApp;
import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.requests.DoorOpenRequestTask;
import devspark.com.doorbell.utils.GoogleApiHelper;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbellcommons.Constants;

import static devspark.com.doorbellcommons.Constants.PATH_REQUEST_RESULT_FAILED;
import static devspark.com.doorbellcommons.Constants.PATH_REQUEST_RESULT_SUCCESS;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class WearListenerService extends WearableListenerService implements DoorOpenRequestListener {


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("test", "onMessageReceived()");
        if (messageEvent.getPath().equals(PhoneConstants.PATH_OPEN_DOOR_MESSAGE)) {
            new DoorOpenRequestTask(this, getContext()).execute();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private Context getContext() {
        return DevsparkApp.getInstance().getApplicationContext();
    }

    @Override
    public void onRequestResult(Context context, Boolean success) {
        Log.i("test", "onRequestResult()");
        String message = success ? PhoneConstants.PATH_REQUEST_RESULT_SUCCESS : PATH_REQUEST_RESULT_FAILED;
        new GoogleApiHelper().sendMessage(context, PhoneConstants.PATH_REQUEST_RESULT, message);
    }
}
