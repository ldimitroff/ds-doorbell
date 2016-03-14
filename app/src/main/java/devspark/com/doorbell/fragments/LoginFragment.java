package devspark.com.doorbell.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import devspark.com.doorbell.R;
import devspark.com.doorbell.activities.NewUIActivity;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class LoginFragment extends Fragment {

    private NewUIActivity mActivity;
    private SignInButton mSignInButton;

    public LoginFragment() {
    }

    public static LoginFragment getInstance(NewUIActivity newUIActivity) {
        LoginFragment fragment = new LoginFragment();
        fragment.mActivity = newUIActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mSignInButton = (SignInButton) v.findViewById(R.id.sign_in_button);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setScopes(mActivity.getScopeArray());
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.signIn();
            }
        });
    }

    public void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }
}
