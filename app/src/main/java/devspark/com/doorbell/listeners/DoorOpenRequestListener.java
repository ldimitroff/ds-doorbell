package devspark.com.doorbell.listeners;

import android.content.Context;

import devspark.com.doorbell.utils.DoorOpenResult;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public interface DoorOpenRequestListener {

    void onRequestResult(Context context, DoorOpenResult result);

}
