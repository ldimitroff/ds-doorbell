package devspark.com.doorbell.requests;

import android.os.AsyncTask;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.net.URLEncoder;

import devspark.com.doorbell.listeners.LoginRequestListener;
import devspark.com.doorbell.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class LoginRequestTask extends AsyncTask<Void, Integer, String> {

    //nombre=Lucas%20Dimitroff&email=ldimitroff@devspark.com&id=103265991727012315028
    private static final String OPEN_DOOR_URL = "red/login?";
    private static final String SEPARATOR = "&";
    private static final String CHARSET = "UTF-8";
    private final LoginRequestListener listener;
    private final GoogleSignInAccount mGoogleSingInAccount;

    public LoginRequestTask(GoogleSignInAccount acct, LoginRequestListener callback) {
        this.listener = callback;
        this.mGoogleSingInAccount = acct;
    }

    @Override
    protected String doInBackground(Void... params) {

        String result;
        try {
            String urlParams = "nombre=" + URLEncoder.encode(mGoogleSingInAccount.getDisplayName(), CHARSET) + SEPARATOR +
                    "email=" + mGoogleSingInAccount.getEmail() + SEPARATOR +
                    "userId=" + mGoogleSingInAccount.getId();

            OkHttpClient client = OkHttpHelper.getOkHttpClient();
            Request request = OkHttpHelper.getOkHttpRequest(Constants.BASE_URL + OPEN_DOOR_URL + urlParams);
            Response response = client.newCall(request).execute();
            result = response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(String token) {
        listener.onRequestResult(token);
    }
}