package devspark.com.doorbellcommons;

import android.content.Context;
import android.os.Vibrator;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class Utils {

    public static void vibrate(Context c, Boolean success) {
        long[] pattern;
        if (success) {
            pattern = new long[]{0, 300};
        } else {
            pattern = new long[]{0, 200, 200, 200};
        }
        ((Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, -1);
    }
}
