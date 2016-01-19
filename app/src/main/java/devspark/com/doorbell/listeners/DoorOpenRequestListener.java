package devspark.com.doorbell.listeners;

import android.content.Context;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public interface DoorOpenRequestListener {

    void onRequestResult(Context context, Boolean success);

}
