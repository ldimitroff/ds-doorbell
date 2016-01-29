package devspark.com.doorbellwear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.dd.CircularProgressButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

import devspark.com.doorbellcommons.Utils;

public class WearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ImageView mLockImageView;
    private TransitionDrawable mTransitionDrawable;
    private CircularProgressButton mCircularProgressButton;
    private boolean mIsDoorOpening = false;
    private GoogleApiClient mGoogleApiClient;
    private boolean mFromNotification = false;
    private boolean mIsDoorOpenRequested = false;
    private Timer mTimer;

    private BroadcastReceiver mDoorRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WearConstants.DOOR_OPEN_REQUEST_RESULT_MESSAGE, false);

            if (mFromNotification && success){
                NotificationManagerCompat man = NotificationManagerCompat.from(DevsparkWearApp.getContext());
                man.cancel(0);
                WearActivity.this.finish();
            }

            cancelDefaultTimerTask();
            updateCircularBtnStatus(success);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFromNotification = mIsDoorOpenRequested = getIntent().getBooleanExtra(WearConstants.NOTIFICATION_BTN_CLICK_EXTRA, false);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                showBallView(mIsDoorOpenRequested);
                initViews();
                connectGoogleApiClient();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mDoorRequestReceiver),
                new IntentFilter(WearConstants.DOOR_OPEN_REQUEST_RESULT_INTENT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDoorRequestReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        cancelDefaultTimerTask();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initViews() {
        mLockImageView = (ImageView) findViewById(R.id.lockImageView);

        mTransitionDrawable = new TransitionDrawable(new Drawable[]{
                getResources().getDrawable(R.drawable.lock_closed),
                getResources().getDrawable(R.drawable.lock_open)
        });
        mTransitionDrawable.setCrossFadeEnabled(true);

        mLockImageView.setImageDrawable(mTransitionDrawable);
        mLockImageView.bringToFront();


        mCircularProgressButton = (CircularProgressButton) findViewById(R.id.btnWithText);
        mCircularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsDoorOpening || mIsDoorOpenRequested) {
                    return;
                }
                showBallView(true);
                mIsDoorOpenRequested = mIsDoorOpening = true;
                sendOpenDoorMessage();
                startDefaultTimerTask();
            }
        });
    }

    private void startDefaultTimerTask() {
        cancelDefaultTimerTask();
        mTimer = new Timer();
        MyTimerTask myTimerTask = new MyTimerTask();
        mTimer.schedule(myTimerTask, WearConstants.TIMER_TASK_DEFAULT);
    }

    private void cancelDefaultTimerTask(){
        if (mTimer !=null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void showBallView(boolean show) {
        if (show) {
            findViewById(R.id.lockImageView).animate().alpha(0.0f);
            findViewById(R.id.ballView).animate().alpha(1.0f);
        } else {
            findViewById(R.id.ballView).animate().alpha(0.0f);
            findViewById(R.id.lockImageView).animate().alpha(1.0f);
        }
    }

    private void updateCircularBtnStatus(boolean success) {
        if (success) {
            startProgressCircularBtn();
        } else {
            showBallView(false);
            mCircularProgressButton.setProgress(-1);
            resetProgressButtonDelayed();
        }
        Utils.vibrate(WearActivity.this, success);
    }

    private void startProgressCircularBtn() {
        CountDownTimer mCountDownTimer = new CountDownTimer(WearConstants.COUNTDOWN_TIMER_TIME, WearConstants.COUNTDOWN_TIMER_STEP) {
            int totalTime = WearConstants.COUNTDOWN_TIMER_TIME;
            int mCountDown = 0;
            boolean transitionStarted = false;

            @Override
            public void onTick(long millisUntilFinished) {
                mCountDown = (int) ((totalTime - millisUntilFinished) * 100 / totalTime);
                mCircularProgressButton.setProgress(mCountDown);
                if (mCountDown > 10 && !transitionStarted) {
                    showBallView(false);
                    mTransitionDrawable.startTransition(WearConstants.TRANSITION_DRAWABLE_TIME);
                    mLockImageView.bringToFront();
                    transitionStarted = true;
                }
            }

            @Override
            public void onFinish() {
                //Do what you want
                mCircularProgressButton.setProgress(100);
                mTransitionDrawable.reverseTransition(WearConstants.TRANSITION_DRAWABLE_TIME);
                mLockImageView.bringToFront();
                resetProgressButtonDelayed();
            }
        };
        mCountDownTimer.start();
    }

    private void resetProgressButtonDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                mCircularProgressButton.setProgress(0);
                mIsDoorOpening = false;
            }
        }, WearConstants.TIMER_RESET_PB);
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(WearActivity.this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(WearActivity.this)
                .addOnConnectionFailedListener(WearActivity.this)
                .build();
        mGoogleApiClient.connect();
    }

    private void sendOpenDoorMessage() {
        if (mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), WearConstants.PATH_OPEN_DOOR_MESSAGE, "".getBytes()).await();
                        if (!result.getStatus().isSuccess()) {
                            Log.e("test", "error");
                        } else {
                            Log.i("test", "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            }).start();

        } else {
            cancelDefaultTimerTask();
            updateCircularBtnStatus(false);
            Log.e("test", "not connected");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("test", "onConnected");
        if (mIsDoorOpenRequested) {
            mIsDoorOpenRequested = false;
            mCircularProgressButton.performClick();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("test", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("test", "onConnectionFailed");
        Log.e("test", connectionResult.toString());
    }

    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                // On UI thread. Only Run this UI stuff on main thread. Otherwise it went to background
                updateCircularBtnStatus(false);
            }
        }
    }

}