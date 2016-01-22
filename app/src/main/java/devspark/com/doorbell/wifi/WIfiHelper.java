package devspark.com.doorbell.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class WIfiHelper {

    private static final String NETWORK_NAME_1 = "DevSpark";
    private static final String NETWORK_NAME_2 = "DevSpark-Mobile";

    /**
     * Check wether is "DevSpark" wifi connected
     *
     * @return
     */
    public static boolean isDSWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID().replace("\"", "");
            if (ssid.equalsIgnoreCase(NETWORK_NAME_1) || ssid.equalsIgnoreCase(NETWORK_NAME_2)) {
                return true;
            }
        }
        return false;
    }
}
