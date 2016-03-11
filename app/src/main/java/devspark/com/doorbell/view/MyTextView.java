package devspark.com.doorbell.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import devspark.com.doorbell.DevsparkApp;


/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class MyTextView extends TextView {

    public MyTextView(Context context) {
        super(context);
        init();
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(DevsparkApp.getInstance().getTypeface());
    }
}
