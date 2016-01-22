package devspark.com.doorbell.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.dd.CircularProgressButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import de.hdodenhof.circleimageview.CircleImageView;
import devspark.com.doorbell.R;
import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.listeners.LoginRequestListener;
import devspark.com.doorbell.notification.NotificationBuilderHelper;
import devspark.com.doorbell.requests.DoorOpenRequestTask;
import devspark.com.doorbell.requests.LoginRequestTask;
import devspark.com.doorbell.utils.GoogleApiHelper;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbell.utils.SPHelper;
import devspark.com.doorbell.wifi.WIfiHelper;
import devspark.com.doorbellcommons.Utils;

public class Main2Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, DoorOpenRequestListener {

    private static final int RC_SIGN_IN = 9001;

    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mAjusteVibrarText;
    private CheckBox mAjusteVibrarCheck;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton mSignInButton;
    private CircleImageView mProfileImage;
    private TextView mUserNameTextField;
    private ProgressDialog mProgressDialog = null;
    private CircularProgressButton circularProgressButton;
    private TextSwitcher mSwitcher;
    private boolean mIsDoorOpening = false;
    private TransitionDrawable transitionDrawable;
    private DrawerLayout mNavDrawerLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private Snackbar mConnectionSnackBar = null;
    private LinearLayout mNavSignOut;
    private ImageView mLockImageView;
    private Handler mSnackBarHandler;

    private Runnable mSnackBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            showSnackBar(!WIfiHelper.isDSWifiConnected(Main2Activity.this));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavDrawerItems();
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mNavDrawerLayout,                    /* DrawerLayout object */
                toolbar,                            /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        mNavDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        initTextSwitcher();

        mLockImageView = (ImageView) findViewById(R.id.lockImageView);

        transitionDrawable = new TransitionDrawable(new Drawable[]{
                getResources().getDrawable(R.drawable.lock_closed),
                getResources().getDrawable(R.drawable.lock_open)
        });
        transitionDrawable.setCrossFadeEnabled(true);

        mLockImageView.setImageDrawable(transitionDrawable);
        mLockImageView.bringToFront();


        circularProgressButton = (CircularProgressButton) findViewById(R.id.btnWithText);
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPHelper.get().isUserSignedIn()) {
                    showLoginDialog();
                    return;
                }
                if (mIsDoorOpening || (!WIfiHelper.isDSWifiConnected(Main2Activity.this))) {
                    return;
                }
                mIsDoorOpening = true;
                mSwitcher.setText(getString(R.string.door_status_opening));
                showBallView(true);
                new DoorOpenRequestTask(Main2Activity.this, Main2Activity.this).execute();
            }
        });
    }

    private void showBallView(boolean show) {
        if (show) {
            mLockImageView.animate().alpha(0.0f);
            findViewById(R.id.ballView).animate().alpha(1.0f);
        } else {
            findViewById(R.id.ballView).animate().alpha(0.0f);
            mLockImageView.animate().alpha(1.0f);
        }
    }

    private void initTextSwitcher() {
        mSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher);

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(Main2Activity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(25);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        // set the animation type of textSwitcher
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);
        mSwitcher.setText(getString(R.string.door_status_closed));
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(Main2Activity.this)
                .setIcon(R.drawable.key_icon)
                .setTitle(getString(R.string.dialog_login_title))
                .setMessage(getString(R.string.dialog_login_now))
                .setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signIn();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative), null)
                .show();
    }

    private void initNavDrawerItems() {
        mAjusteVibrarText = (TextView) findViewById(R.id.settings_vibrate_text);
        mAjusteVibrarCheck = (CheckBox) findViewById(R.id.settings_vibrate_checkbox);

        mAjusteVibrarCheck.setChecked(SPHelper.get().isVibrationEnabled());
        mAjusteVibrarCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPHelper.get().setVibrationEnabled(isChecked);
            }
        });

        CheckBox ajusteNotificacionActiva = (CheckBox) findViewById(R.id.settings_notification_active);

        boolean notificationEnabled = SPHelper.get().isNotificationEnabled();
        ajusteNotificacionActiva.setChecked(notificationEnabled);
        refrescarNotificacionViews(notificationEnabled);

        ajusteNotificacionActiva.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPHelper.get().setNotificationEnabled(isChecked);
                refrescarNotificacionViews(isChecked);
            }
        });

        LinearLayout mostrarNotificacion = (LinearLayout) findViewById(R.id.showNotification);
        mostrarNotificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WIfiHelper.isDSWifiConnected(Main2Activity.this)) {
                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    mNotifyMgr.notify(0, NotificationBuilderHelper.getNotification(Main2Activity.this, false).build());
                    new GoogleApiHelper().sendMessage(Main2Activity.this, PhoneConstants.PATH_NOTIFICATION, "");
                } else {
                    Toast.makeText(Main2Activity.this, R.string.must_be_on_ds_network, Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout editNick = (LinearLayout) findViewById(R.id.editNick);
        editNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set up the input
                final EditText input = new EditText(Main2Activity.this);
                // Specify the type of input expected;
                input.setText(SPHelper.get().getUserNick());
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                new AlertDialog.Builder(Main2Activity.this)
                        .setView(input)
                        .setTitle(getString(R.string.dialog_change_nick_title))
                        .setPositiveButton(getString(R.string.dialog_accept), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = input.getText().toString();
                                if (!text.isEmpty() || !TextUtils.isEmpty(text)) {
                                    SPHelper.get().setUserNick(text);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), null)
                        .show();
            }
        });

        startGmailSingIn();
    }

    /**
     * @param isChecked
     */
    private void refrescarNotificacionViews(boolean isChecked) {
        mAjusteVibrarCheck.setEnabled(isChecked);
        mAjusteVibrarText.setEnabled(isChecked);
    }

    private void startGmailSingIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, Main2Activity.this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mSignInButton.setScopes(gso.getScopeArray());

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        mUserNameTextField = (TextView) findViewById(R.id.userNameText);

        final boolean isUserSignedIn = SPHelper.get().isUserSignedIn();

        updateUI(isUserSignedIn);
        mNavSignOut = (LinearLayout) findViewById(R.id.nav_sign_out);
        mNavSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SPHelper.get().isUserSignedIn()) {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setIcon(R.drawable.question_icon)
                            .setTitle(getString(R.string.dialog_logout_title))
                            .setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signOut();
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_negative), null)
                            .show();
                }
            }
        });
    }

    private void updateUI(boolean signedIn) {
        hideProgressDialog();
        if (signedIn) {
            mSignInButton.setVisibility(View.INVISIBLE);
            String userName = SPHelper.get().getUserName();
            String photoURl = SPHelper.get().getUserPhotoURL();
            findViewById(R.id.nav_sign_out).setVisibility(View.VISIBLE);
            findViewById(R.id.userProfileLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.editNick).setVisibility(View.VISIBLE);
            Glide.with(Main2Activity.this)
                    .load(photoURl)
                    .fitCenter()
                    .error(R.mipmap.ic_launcher)
                    .into(mProfileImage);
            mUserNameTextField.setText(userName);
        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            findViewById(R.id.userProfileLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.nav_sign_out).setVisibility(View.INVISIBLE);
            findViewById(R.id.editNick).setVisibility(View.INVISIBLE);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(Main2Activity.this);
            mProgressDialog.setMessage(getString(R.string.dialog_signing_in));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        revokeAccess();
                        SPHelper.get().setUserSignedIn(false);
                        SPHelper.get().setUserName("");
                        SPHelper.get().setUserPhotoURL("");
                        SPHelper.get().setUserToken("");
                        SPHelper.get().setUserNick("");
                        updateUI(false);
                    }
                });

    }

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
    }
    // [END revokeAccess]

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess() && result.getSignInAccount() != null) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct.getEmail() != null && !acct.getEmail().trim().contains("@devspark.com")) {
                Snackbar.make(findViewById(R.id.mainactivitycontent), R.string.sign_in_account_not_able, Snackbar.LENGTH_SHORT).show();
                signOut();
                return;
            }
            SPHelper.get().setUserName(acct.getDisplayName());
            SPHelper.get().setUserPhotoURL(acct.getPhotoUrl().toString());
            showProgressDialog();
            new LoginRequestTask(acct, new LoginRequestListener() {
                @Override
                public void onRequestResult(String token) {
                    if (token != null && !token.isEmpty() && !token.equalsIgnoreCase("{mensaje:'Usuario incorrecto'}")) {
                        SPHelper.get().setUserToken(token);
                        SPHelper.get().setUserSignedIn(true);
                        Snackbar.make(findViewById(R.id.mainactivitycontent), Main2Activity.this.getString(R.string.toast_login_success, SPHelper.get().getUserName()), Snackbar.LENGTH_SHORT).show();
                        updateUI(true);
                    } else {
                        Snackbar.make(findViewById(R.id.mainactivitycontent), R.string.toast_login_error, Snackbar.LENGTH_SHORT).show();
                        signOut();
                    }
                    hideProgressDialog();
                }
            }).execute();
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
            Snackbar.make(findViewById(R.id.mainactivitycontent), R.string.toast_login_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestResult(Context context, Boolean success) {

        Utils.vibrate(context, success);

        if (!success) {
            circularProgressButton.setProgress(-1);
            mSwitcher.setText(getString(R.string.door_status_closed));
            showBallView(false);
            resetProgressButtonDelayed();
            Toast.makeText(Main2Activity.this, R.string.toast_request_error, Toast.LENGTH_LONG).show();
            return;
        }
        mSwitcher.setText(getString(R.string.door_status_open));
        CountDownTimer mCountDownTimer = new CountDownTimer(PhoneConstants.COUNTDOWN_TIMER_TIME, PhoneConstants.COUNTDOWN_TIMER_STEP) {
            int totalTime = PhoneConstants.COUNTDOWN_TIMER_TIME;
            int mCountDown = 0;
            boolean transitionStarted = false;

            @Override
            public void onTick(long millisUntilFinished) {
                mCountDown = (int) ((totalTime - millisUntilFinished) * 100 / totalTime);
                circularProgressButton.setProgress(mCountDown);
                if (mCountDown > 10 && !transitionStarted) {
                    showBallView(false);
                    transitionDrawable.startTransition(PhoneConstants.TRANSITION_DRAWABLE_TIME);
                    mLockImageView.bringToFront();
                    transitionStarted = true;
                }
            }

            @Override
            public void onFinish() {
                //Do what you want
                circularProgressButton.setProgress(100);
                transitionDrawable.reverseTransition(PhoneConstants.TRANSITION_DRAWABLE_TIME);
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
                circularProgressButton.setProgress(0);
                mSwitcher.setText(getString(R.string.door_status_closed));
                mIsDoorOpening = false;
            }
        }, PhoneConstants.TIMER_RESET_PB);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSnackBar(!WIfiHelper.isDSWifiConnected(Main2Activity.this));
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mSnackBarHandler != null) {
                    mSnackBarHandler.removeCallbacksAndMessages(null);
                }
                mSnackBarHandler = new Handler();
                mSnackBarHandler.postDelayed(mSnackBarUpdateRunnable, PhoneConstants.TIMER_RESET_PB);
            }
        }

        ;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        mConnectionSnackBar = null;
        super.onPause();
    }

    private Snackbar getSnackBar() {
        if (mConnectionSnackBar == null) {
            mConnectionSnackBar = Snackbar
                    .make(findViewById(R.id.mainactivitycontent), R.string.must_be_on_ds_network, Snackbar.LENGTH_INDEFINITE);
        }
        return mConnectionSnackBar;
    }

    private void dismissSnackBar() {
        if (mConnectionSnackBar != null) {
            mConnectionSnackBar.dismiss();
            mConnectionSnackBar = null;
        }
    }

    private void showSnackBar(boolean show) {
        Log.d("main2activity", "showSnackbar: " + show);
        if (show) {
            if (!getSnackBar().isShownOrQueued())
                getSnackBar().show();
        } else {
            dismissSnackBar();
        }
    }
}
