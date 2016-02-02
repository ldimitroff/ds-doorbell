package devspark.com.doorbell;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DevsparkApp extends Application {

    private static final String MY_FLURRY_APIKEY = "594RRXH6RDC66Q97S38N";
    private static DevsparkApp instance;

    public static DevsparkApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Crittercism.initialize(getApplicationContext(), "56a6271cb23c2c0f00588696");

        // configure Flurry
        FlurryAgent.setLogEnabled(false);

        // init Flurry
        FlurryAgent.init(this, MY_FLURRY_APIKEY);
    }
}
