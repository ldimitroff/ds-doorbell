package devspark.com.doorbell;

import android.app.Application;

import com.crittercism.app.Crittercism;

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
        Crittercism.initialize(getApplicationContext(), "56a6271cb23c2c0f00588696");
    }
}
