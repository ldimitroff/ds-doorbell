package devspark.com.doorbellwear;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Timer;
import java.util.TimerTask;

import devspark.com.doorbellcommons.Utils;
import devspark.com.doorbellcommons.views.CircularExpandingView;
import devspark.com.doorbellcommons.views.WaveHelper;
import devspark.com.doorbellcommons.views.WaveView;

public class WearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private boolean mIsDoorOpening = false;
    private GoogleApiClient mGoogleApiClient;
    private boolean mFromNotification = false;
    private boolean mIsDoorOpenRequested = false;
    private Timer mTimer;

    private BroadcastReceiver mDoorRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WearConstants.DOOR_OPEN_REQUEST_RESULT_MESSAGE, false);

            if (mFromNotification && success) {
                NotificationManagerCompat man = NotificationManagerCompat.from(DevsparkWearApp.getContext());
                man.cancel(0);
                WearActivity.this.finish();
            }

            cancelDefaultTimerTask();
            updateCircularBtnStatus(success);
        }
    };


    private Button mButton;
    private CircularExpandingView mCircularExpandingView;
    private Animation mPulseAnimation;
    private WaveView mWaveView;
    private WaveHelper mWaveHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFromNotification = mIsDoorOpenRequested = getIntent().getBooleanExtra(WearConstants.NOTIFICATION_BTN_CLICK_EXTRA, false);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                initViews();
                showPulseAnimation(mIsDoorOpenRequested);
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

        mButton = (Button) findViewById(R.id.button);
        mCircularExpandingView = (CircularExpandingView) findViewById(R.id.circularExpandingView);
        mCircularExpandingView.setColor(ContextCompat.getColor(WearActivity.this, R.color.colorGreen));
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mCircularExpandingView.setBounds(displaymetrics.widthPixels / 2, displaymetrics.heightPixels / 2);

        mWaveView = (WaveView) findViewById(R.id.wave_view);
        mPulseAnimation = AnimationUtils.loadAnimation(WearActivity.this, R.anim.pulse);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsDoorOpening || mIsDoorOpenRequested) {
                    return;
                }
                showPulseAnimation(true);
                mButton.setEnabled(false);
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

    private void cancelDefaultTimerTask() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void showPulseAnimation(boolean show) {
        if (show) {
            mButton.startAnimation(mPulseAnimation);
        } else {
            mButton.clearAnimation();
            mPulseAnimation.cancel();
        }
    }

    private void updateCircularBtnStatus(boolean success) {
        if (success) {
            expandCircleButton();
        } else {
            mPulseAnimation.cancel();
            mButton.setEnabled(false);
            mButton.setBackground(ContextCompat.getDrawable(WearActivity.this, R.drawable.round_button_error));
            resetDoorOpenBtnDelayed();
        }
        Utils.vibrate(WearActivity.this, success);
    }

    private void expandCircleButton() {
        mButton.clearAnimation();
        mPulseAnimation.cancel();
        mCircularExpandingView.setVisibility(View.VISIBLE);
        Animator expandAnimator = mCircularExpandingView.expand();
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mButton.setVisibility(View.INVISIBLE);
                mCircularExpandingView.setVisibility(View.INVISIBLE);

                mWaveView.setVisibility(View.VISIBLE);
                mWaveView.setWaveColor(Color.TRANSPARENT, ContextCompat.getColor(WearActivity.this, R.color.colorGreen));
                int mBorderColor = Color.parseColor("#44FFFFFF");
                mWaveView.setBorder(1, mBorderColor);
                mWaveView.setShapeType(WaveView.ShapeType.SQUARE);
                mWaveHelper = new WaveHelper(mWaveView);

                mWaveHelper.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWaveHelper.cancel();
                        mWaveView.setVisibility(View.INVISIBLE);
                        mButton.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mIsDoorOpenRequested = mIsDoorOpening = false;
                    }
                });

                mWaveHelper.start();
            }
        });
        expandAnimator.start();
    }

    private void resetDoorOpenBtnDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                mButton.setBackground(ContextCompat.getDrawable(WearActivity.this, R.drawable.round_button));
                mButton.setEnabled(true);
                mIsDoorOpenRequested = mIsDoorOpening = false;
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
            mButton.performClick();
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

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                // On UI thread. Only Run this UI stuff on main thread. Otherwise it went to background
                updateCircularBtnStatus(false);
            }
        }
    }

}