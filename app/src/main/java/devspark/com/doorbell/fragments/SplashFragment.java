package devspark.com.doorbell.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

import devspark.com.doorbell.R;
import devspark.com.doorbell.activities.NewUIActivity;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbell.utils.SPHelper;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class SplashFragment extends Fragment {

    private NewUIActivity mActivity;

    public SplashFragment() {
    }

    public static SplashFragment getInstance(NewUIActivity newUIActivity){
        SplashFragment fragment = new SplashFragment();
        fragment.mActivity = newUIActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onStart() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (SPHelper.get().isUserSignedIn()){
                    mActivity.showMainFragment();
                }else{
                    mActivity.showLoginFragment();
                }
            }
        }, PhoneConstants.SPLASH_DISPLAY_LENGTH);


        super.onStart();
    }
}
