package devspark.com.doorbell.requests;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class OkHttpHelper {

    private static final int TIMEOUT_VALUE_SECONDS = 5;

    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_VALUE_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_VALUE_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_VALUE_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public static Request getOkHttpRequest(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

}
