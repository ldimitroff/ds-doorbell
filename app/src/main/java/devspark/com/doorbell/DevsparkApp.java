package devspark.com.doorbell;

import android.app.Application;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DevsparkApp extends Application {

    private static DevsparkApp instance;

    public static DevsparkApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
