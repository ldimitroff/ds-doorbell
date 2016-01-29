package devspark.com.doorbellwear;

import android.app.Application;
import android.content.Context;


/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DevsparkWearApp extends Application {

    private static DevsparkWearApp instance;

    public static DevsparkWearApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
