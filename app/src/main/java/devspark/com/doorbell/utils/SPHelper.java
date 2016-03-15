package devspark.com.doorbell.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import devspark.com.doorbell.DevsparkApp;
import devspark.com.doorbell.R;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class SPHelper {

    private static SPHelper instance;
    private final Context context;
    private SharedPreferences mSharedPreferences;

    public static SPHelper get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized SPHelper getSync() {
        if (instance == null) instance = new SPHelper();
        return instance;
    }

    private SPHelper() {
        // here you can directly access the Application context calling
        context = DevsparkApp.getInstance().getApplicationContext();
        mSharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedpreferences_name), Context.MODE_PRIVATE);
    }

    private SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    private SharedPreferences.Editor getSPEditor() {
        return getSharedPreferences().edit();
    }

    public boolean isVibrationEnabled() {
        return getSharedPreferences().getBoolean(context.getString(R.string.devspark_notification_vibrate), true);
    }

    public void setVibrationEnabled(boolean enabled) {
        getSPEditor().putBoolean(context.getString(R.string.devspark_notification_vibrate), enabled).apply();
    }

    public boolean isNotificationEnabled() {
        return getSharedPreferences().getBoolean(context.getString(R.string.devspark_notification_active), true);
    }

    public void setNotificationEnabled(boolean isChecked) {
        getSPEditor().putBoolean(context.getString(R.string.devspark_notification_active), isChecked).apply();
    }

    public boolean isUserSignedIn() {
        return getSharedPreferences().getBoolean(context.getString(R.string.devspark_user_signed_in), false);
    }

    public void setUserSignedIn(boolean signedIn) {
        getSPEditor().putBoolean(context.getString(R.string.devspark_user_signed_in), signedIn).apply();
    }

    public String getUserToken() {
        return getSharedPreferences().getString(context.getString(R.string.devspark_user_token), "");
    }

    public void setUserToken(String token) {
        getSPEditor().putString(context.getString(R.string.devspark_user_token), token).apply();
    }

    public String getUserName() {
        return getSharedPreferences().getString(context.getString(R.string.devspark_user_full_name), "");
    }

    public void setUserName(String name) {
        getSPEditor().putString(context.getString(R.string.devspark_user_full_name), name).apply();
    }

    public String getUserPhotoURL() {
        return getSharedPreferences().getString(context.getString(R.string.devspark_user_photo_url), "");
    }

    public void setUserPhotoURL(String url) {
        getSPEditor().putString(context.getString(R.string.devspark_user_photo_url), url).apply();
    }

    public String getUserNick() {
        String userNick = getSharedPreferences().getString(context.getString(R.string.devspark_user_nick), "");
        if (TextUtils.isEmpty(userNick)) {
            return getUserName().split(" ")[0];
        }
        return userNick;
    }

    public String getUserFirstName() {
        return getUserName().split(" ")[0];
    }

    public void setUserNick(String nick) {
        getSPEditor().putString(context.getString(R.string.devspark_user_nick), nick).apply();
    }
}
