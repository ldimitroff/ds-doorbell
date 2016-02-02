package devspark.com.doorbell.utils;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lucas Dimitroff <ldimitroff@devspark.com>
 */
public class FlurryAnalyticHelper {

    public static final String FROM_WEAR = "Wear";
    public static final String FROM_ACTIVITY = "Activity";
    public static final String FROM_NOTIFICATION = "Notification";

    public static void logDoorOpenEvent(String fromParam) {
        // Capture author info & user status
        Map<String, String> eventParams = new HashMap<>();

        //param keys and values have to be of String type
        eventParams.put("Usuario", SPHelper.get().getUserName());
        eventParams.put("From", fromParam);

        //up to 10 params can be logged with each event
        FlurryAgent.logEvent("DoorOpen", eventParams);
    }

    public static void logDoorOpenRequest(String param) {
        // Capture author info & user status
        Map<String, String> eventParams = new HashMap<>();

        //param keys and values have to be of String type
        eventParams.put("Usuario", SPHelper.get().getUserName());
        eventParams.put("Request Success", param);

        //up to 10 params can be logged with each event
        FlurryAgent.logEvent("DoorOpenRequest", eventParams);
    }
}
