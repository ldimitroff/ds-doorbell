package devspark.com.doorbell.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String message;
    private String path;
    private GoogleApiClient mGoogleApiClient;

    public void sendMessage(Context context, String path, String message) {
        this.path = path;
        this.message = message;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.
                            sendMessage(mGoogleApiClient, node.getId(), path,
                                    message.getBytes())
                            .await();
                    if (!result.getStatus().isSuccess()) {
                        Log.e("test", "error");
                    } else {
                        Log.i("test", "success!! sent to: " + node.getDisplayName() );
                        break;
                    }
                }
                mGoogleApiClient.disconnect();
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
