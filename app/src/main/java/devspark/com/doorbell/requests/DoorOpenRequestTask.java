package devspark.com.doorbell.requests;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.URLEncoder;

import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbell.utils.SPHelper;
import devspark.com.doorbell.wifi.WIfiHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DoorOpenRequestTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String OPEN_DOOR_URL = "red/prende";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String JSON_FIELD_TOKEN = "token";
    private static final String JSON_FIELD_TEXTO = "texto";

    private final DoorOpenRequestListener listener;
    private final Context context;

    public DoorOpenRequestTask(DoorOpenRequestListener callback, Context c) {
        this.context = c;
        this.listener = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!SPHelper.get().isUserSignedIn() || !WIfiHelper.isDSWifiConnected(context)) {
            return false;
        }

        String token = SPHelper.get().getUserToken();
        String result;
        try {
            OkHttpClient client = OkHttpHelper.getOkHttpClient();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_TOKEN, token);
            jsonObject.put(JSON_FIELD_TEXTO, URLEncoder.encode(SPHelper.get().getUserNick(), PhoneConstants.CHARSET));

            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url(PhoneConstants.BASE_URL + OPEN_DOOR_URL)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            result = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return !(!result.isEmpty() && !result.equalsIgnoreCase("true"));
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onRequestResult(context, success);
    }
}