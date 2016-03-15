package devspark.com.doorbell;

import android.app.Application;
import android.graphics.Typeface;

import com.crittercism.app.Crittercism;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DevsparkApp extends Application {

    private static DevsparkApp instance;
    private Typeface mTypeface;

    public static DevsparkApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Crittercism.initialize(getApplicationContext(), "56a6271cb23c2c0f00588696");
        mTypeface = Typeface.createFromAsset(getAssets(), "varela-round-regular.ttf");
    }

    public Typeface getTypeface() {
        return mTypeface;
    }
}
