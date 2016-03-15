package devspark.com.doorbell.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import devspark.com.doorbell.R;
import devspark.com.doorbell.fragments.LoginFragment;
import devspark.com.doorbell.fragments.MainFragment;
import devspark.com.doorbell.fragments.SettingsFragment;
import devspark.com.doorbell.fragments.SplashFragment;
import devspark.com.doorbell.listeners.LoginRequestListener;
import devspark.com.doorbell.listeners.OnMainFragmentListener;
import devspark.com.doorbell.requests.LoginRequestTask;
import devspark.com.doorbell.utils.SPHelper;

public class NewUIActivity extends FragmentActivity implements OnMainFragmentListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;
    private GoogleSignInOptions mGso;
    private LoginFragment mLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startGmailSingIn();
        setContentView(R.layout.activity_new_ui_activity);
        if (findViewById(R.id.fragment_container) != null) {
            SplashFragment fragment = SplashFragment.getInstance(this);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    public void showMainFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.fragment_container, MainFragment.getInstance());
        ft.commit();
    }

    public void showLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        mLoginFragment = LoginFragment.getInstance(NewUIActivity.this);
        ft.replace(R.id.fragment_container, mLoginFragment);
        ft.commit();
    }

    @Override
    public void onHamburgerClick() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.add(R.id.fragment_container, SettingsFragment.getInstance());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void startGmailSingIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, NewUIActivity.this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
                .build();
    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        if (result != null && result.isSuccess() && result.getSignInAccount() != null) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct.getEmail() != null && !acct.getEmail().trim().contains("@devspark.com")) {
                if (mLoginFragment != null) {
                    mLoginFragment.showToast(getString(R.string.must_use_ds_account));
                }
                signOut();
                return;
            }
            SPHelper.get().setUserName(acct.getDisplayName());
            try {
                SPHelper.get().setUserPhotoURL(acct.getPhotoUrl().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            showProgressDialog();
            new LoginRequestTask(acct, new LoginRequestListener() {
                @Override
                public void onRequestResult(String token) {
                    if (token != null && !token.isEmpty() && !token.equalsIgnoreCase("{mensaje:'Usuario incorrecto'}")) {
                        SPHelper.get().setUserToken(token);
                        SPHelper.get().setUserSignedIn(true);
                        showMainFragment();
                    } else {
                        if (mLoginFragment != null) {
                            mLoginFragment.showToast(getString(R.string.login_error_toast));
                        }
                        signOut();
                    }
                    hideProgressDialog();
                }
            }).execute();
        } else {
            signOut();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(NewUIActivity.this);
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public Scope[] getScopeArray() {
        return mGso.getScopeArray();
    }
}
