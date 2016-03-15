package devspark.com.doorbell.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.Random;

import devspark.com.doorbell.R;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class TextSaying {

    public static String getGreetingMsg(Context context) {
        return getRndString(context, R.array.greetings_array);
    }

    public static String getOpeningMsg(Context context) {
        return getRndString(context, R.array.opening_msg_array);
    }

    public static String getErrorText(DoorOpenResult result, Context context) {
        int id;
        switch (result) {
            case FALSE:
                id = R.array.error_array;
                break;
            case BUSY:
                id = R.array.busy_error_array;
                break;
            case NO_WIFI:
                id = R.array.no_wifi_error_array;
                break;
            default:
                id = R.array.error_array;
                break;
        }
        return getRndString(context, id);
    }

    private static String getRndString(Context context, int id) {
        if (context == null){
            return "";
        }
        Resources res = context.getResources();
        String[] stringArray = res.getStringArray(id);
        Random random = new Random();
        int rnd = random.nextInt(stringArray.length - 1);
        return stringArray[rnd].replace("$$", SPHelper.get().getUserFirstName());
    }
}
