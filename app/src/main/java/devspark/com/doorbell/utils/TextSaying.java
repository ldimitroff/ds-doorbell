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
        return getRndString(context, R.array.greetings_array).replace("$$", SPHelper.get().getUserFirstName());
    }

    public static String getOpeningMsg(Context context) {
        return getRndString(context, R.array.opening_msg_array).replace("$$", SPHelper.get().getUserFirstName());
    }

    public static String getErrorText(DoorOpenResult result, Context context) {
        return getRndString(context, result == DoorOpenResult.FALSE ? R.array.error_array : R.array.busy_error_array).replace("$$", SPHelper.get().getUserFirstName());
    }

    private static String getRndString(Context context, int id) {
        Resources res = context.getResources();
        String[] stringArray = res.getStringArray(id);
        Random random = new Random();
        int rnd = random.nextInt(stringArray.length - 1);
        return stringArray[rnd];
    }
}
