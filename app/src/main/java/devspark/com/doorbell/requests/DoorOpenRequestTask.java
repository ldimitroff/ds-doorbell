package devspark.com.doorbell.requests;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.URLEncoder;

import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.utils.DoorOpenResult;
import devspark.com.doorbell.utils.FlurryAnalyticHelper;
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
public class DoorOpenRequestTask extends AsyncTask<Void, Integer, DoorOpenResult> {

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
    protected DoorOpenResult doInBackground(Void... params) {
        if (!SPHelper.get().isUserSignedIn() || !WIfiHelper.isDSWifiConnected(context)) {
            return DoorOpenResult.FALSE;
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
            return DoorOpenResult.FALSE;
        }
        if (!result.trim().isEmpty()) {
            if (result.equalsIgnoreCase("true")) {
                return DoorOpenResult.TRUE;
            } else if (result.equalsIgnoreCase("busy")) {
                return DoorOpenResult.BUSY;
            } else if (result.equalsIgnoreCase("false")) {
                return DoorOpenResult.FALSE;
            }
        }
        return DoorOpenResult.FALSE;
    }

    @Override
    protected void onPostExecute(DoorOpenResult result) {
        FlurryAnalyticHelper.logDoorOpenRequest(result.toString());
        listener.onRequestResult(context, result);
    }
}