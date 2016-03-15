package devspark.com.doorbell.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import devspark.com.doorbell.DevsparkApp;
import devspark.com.doorbell.R;
import devspark.com.doorbell.listeners.DoorOpenRequestListener;
import devspark.com.doorbell.listeners.OnMainFragmentListener;
import devspark.com.doorbell.requests.DoorOpenRequestTask;
import devspark.com.doorbell.utils.DoorOpenResult;
import devspark.com.doorbell.utils.PhoneConstants;
import devspark.com.doorbell.utils.SPHelper;
import devspark.com.doorbell.utils.TextSaying;
import devspark.com.doorbellcommons.views.CircularExpandingView;
import devspark.com.doorbell.view.MyTextView;
import devspark.com.doorbellcommons.views.WaveHelper;
import devspark.com.doorbellcommons.views.WaveView;
import devspark.com.doorbell.wifi.WIfiHelper;
import devspark.com.doorbellcommons.Utils;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class MainFragment extends Fragment implements DoorOpenRequestListener {

    private CircleImageView mProfileImage;
    private CircularExpandingView mCircularExpandingView;
    private Button mButton;
    private Button mHamburgerButton;
    private LinearLayout mButtonLayout;
    private WaveHelper mWaveHelper;
    private WaveView mWaveView;
    private TextSwitcher mSwitcher;
    private TextView mButtonTextView;
    private Animation mPulseAnimation;
    private OnMainFragmentListener mCallback;
    private Handler mResetHandler;

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!WIfiHelper.isDSWifiConnected(getContext())) {
                circuleButtonError(DoorOpenResult.NO_WIFI);
                return;
            }
            new DoorOpenRequestTask(MainFragment.this, getContext()).execute();
            setCircularExpandingBounds(v);
            v.startAnimation(mPulseAnimation);
        }
    };
    private View.OnClickListener mHamburgerClkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onHamburgerClick();
        }
    };

    private Runnable mResetRunnable = new Runnable() {
        public void run() {
            // acciones que se ejecutan tras los milisegundos
            mButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_button));
            mButton.setEnabled(true);
            mButtonTextView.setText(getString(R.string.tap_to_open));
            mSwitcher.setText(TextSaying.getGreetingMsg(getContext()));
            mResetHandler = null;
        }
    };

    public static MainFragment getInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mProfileImage = (CircleImageView) v.findViewById(R.id.profile_image);
        mButton = (Button) v.findViewById(R.id.button);

        mHamburgerButton = (Button) v.findViewById(R.id.hamburgerBtn);

        mButtonTextView = (TextView) v.findViewById(R.id.button_text_view);

        mCircularExpandingView = (CircularExpandingView) v.findViewById(R.id.circularExpandingView);
        mCircularExpandingView.setColor(ContextCompat.getColor(getContext(), R.color.colorGreen));

        mButtonLayout = (LinearLayout) v.findViewById(R.id.buttonLayout);

        mWaveView = (WaveView) v.findViewById(R.id.wave_view);

        mPulseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);

        initTextSwitcher(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null) {
            try {
                mCallback = (OnMainFragmentListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnMainFragmentListener");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwitcher.setText(TextSaying.getGreetingMsg(getContext()));
        mButton.setOnClickListener(mButtonClickListener);
        mHamburgerButton.setOnClickListener(mHamburgerClkListener);
        String photoURl = SPHelper.get().getUserPhotoURL();
        Glide.with(getContext())
                .load(photoURl)
                .fitCenter()
                .error(R.mipmap.ic_launcher)
                .into(mProfileImage);
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
                mSwitcher.setText(TextSaying.getOpeningMsg(getContext()));
                mButtonLayout.setVisibility(View.INVISIBLE);
                mCircularExpandingView.setVisibility(View.INVISIBLE);

                mWaveView.setVisibility(View.VISIBLE);
                mWaveView.setWaveColor(Color.TRANSPARENT, ContextCompat.getColor(getContext(), R.color.colorGreen));
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
                        mButtonLayout.setVisibility(View.VISIBLE);
                        mButton.setEnabled(true);
                        mSwitcher.setText(TextSaying.getGreetingMsg(getContext()));
                    }
                });

                mWaveHelper.start();
            }
        });
        expandAnimator.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWaveHelper != null) {
            mWaveHelper.addListener(null);
        }
        if (mWaveView != null) {
            mWaveView.clearAnimation();
        }
        if (mButton != null) {
            mButton.clearAnimation();
        }
        if (mResetHandler != null) {
            mResetHandler.removeCallbacks(mResetRunnable);
            mResetHandler = null;
        }
    }

    private void circuleButtonError(DoorOpenResult result) {
        mPulseAnimation.cancel();
        mButton.setEnabled(false);
        mButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_button_error));
        mButtonTextView.setText(R.string.error_message);
        mSwitcher.setText(TextSaying.getErrorText(result, getContext()));
        resetDoorOpenBtnDelayed();
    }

    private void setCircularExpandingBounds(View v) {
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        mCircularExpandingView.setBounds(loc[0] + v.getWidth() / 2, loc[1] + v.getWidth() / 2);
    }

    private void initTextSwitcher(View v) {
        mSwitcher = (TextSwitcher) v.findViewById(R.id.textSwitcher);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                // create new textView and set the properties like clolr, size etc
                MyTextView myText = new MyTextView(getContext());
                myText.setGravity(Gravity.CENTER_VERTICAL);
                myText.setTextSize(TypedValue.COMPLEX_UNIT_PX, DevsparkApp.getInstance().getResources().getDimensionPixelSize(R.dimen.common_title_size));
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });
        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        // set the animation type of textSwitcher
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);
    }

    @Override
    public void onRequestResult(Context context, DoorOpenResult result) {
        Utils.vibrate(context, result == DoorOpenResult.TRUE);
        switch (result) {
            case TRUE:
                expandCircleButton();
                break;
            case FALSE:
            case BUSY:
                circuleButtonError(result);
                break;
        }
    }

    private void resetDoorOpenBtnDelayed() {
        mResetHandler = new Handler();
        mResetHandler.postDelayed(mResetRunnable, PhoneConstants.TIMER_RESET_PB);
    }
}
