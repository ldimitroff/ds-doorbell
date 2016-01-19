package devspark.com.doorbell.requests;

import android.content.Context;
import android.os.AsyncTask;

import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.utils.Constants;
import devspark.com.doorbell.utils.SPHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class DoorOpenRequestTask extends AsyncTask<Void, Integer, Boolean> {

    private static final String OPEN_DOOR_URL = "red/prende?token=";
    private final DoorOpenRequestListener listener;
    private final Context context;

    public DoorOpenRequestTask(DoorOpenRequestListener callback, Context c) {
        this.context = c;
        this.listener = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!SPHelper.get().isUserSignedIn()) {
            return false;
        }

        String token = SPHelper.get().getUserToken();
        String result;
        try {
            OkHttpClient client = OkHttpHelper.getOkHttpClient();
            Request request = OkHttpHelper.getOkHttpRequest(Constants.BASE_URL + OPEN_DOOR_URL + token);
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