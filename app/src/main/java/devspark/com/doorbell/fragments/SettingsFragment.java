package devspark.com.doorbell.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import devspark.com.doorbell.R;
import devspark.com.doorbell.activities.NewUIActivity;
import devspark.com.doorbell.utils.SPHelper;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class SettingsFragment extends Fragment {

    private NewUIActivity mActivity;
    private TextView mNickTv;
    private LinearLayout mNickLL;
    private TextView mNotificationTV;
    private LinearLayout mNotificationLL;
    private LinearLayout mVibrationLL;
    private TextView mVibrationTV;
    private Button mBackBtn;

    public SettingsFragment() {
    }

    public static SettingsFragment getInstance(NewUIActivity newUIActivity) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.mActivity = newUIActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        mNickLL = (LinearLayout) v.findViewById(R.id.nickLL);
        mNickTv = (TextView) v.findViewById(R.id.nickTv);
        mNickTv.setText(SPHelper.get().getUserNick());

        mNotificationLL = (LinearLayout) v.findViewById(R.id.notificationLL);
        mNotificationTV = (TextView) v.findViewById(R.id.notificationTV);
        setTextAndColor(mNotificationTV, SPHelper.get().isNotificationEnabled());

        mVibrationLL = (LinearLayout) v.findViewById(R.id.vibrationLL);
        mVibrationTV = (TextView) v.findViewById(R.id.vibrationTV);
        setTextAndColor(mVibrationTV, SPHelper.get().isVibrationEnabled());

        mVibrationLL.setVisibility(SPHelper.get().isNotificationEnabled() ? View.VISIBLE : View.INVISIBLE);

        mBackBtn = (Button) v.findViewById(R.id.backBtn);

        return v;
    }

    private void setTextAndColor(TextView tv, boolean checked) {
        if (checked) {
            tv.setText("SI");
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
        } else {
            tv.setText("NO");
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mNickLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected;
                input.setText(SPHelper.get().getUserNick());
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                new AlertDialog.Builder(getContext())
                        .setView(input)
                        .setTitle(getString(R.string.dialog_change_nick_title))
                        .setPositiveButton(getString(R.string.dialog_accept), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = input.getText().toString();
                                if (!text.isEmpty() || !TextUtils.isEmpty(text)) {
                                    SPHelper.get().setUserNick(text);
                                    mNickTv.setText(SPHelper.get().getUserNick());
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), null)
                        .show();
            }
        });
        mNotificationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !SPHelper.get().isNotificationEnabled();
                SPHelper.get().setNotificationEnabled(enabled);
                setTextAndColor(mNotificationTV, enabled);
                mVibrationLL.setVisibility(SPHelper.get().isNotificationEnabled() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        mVibrationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !SPHelper.get().isVibrationEnabled();
                SPHelper.get().setVibrationEnabled(enabled);
                setTextAndColor(mVibrationTV, enabled);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
}
