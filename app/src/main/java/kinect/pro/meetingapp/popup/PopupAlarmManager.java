package kinect.pro.meetingapp.popup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import kinect.pro.meetingapp.other.Constants;

public class PopupAlarmManager {

    private static final String TAG = PopupAlarmManager.class.getSimpleName();

    public static void scheduleNotification(Context context, String scheduledAt, String urlOfTopic, long startTime, long delay) {
        Log.d("+", "scheduleNotification");
        Intent notificationIntent = new Intent(context, PopupReceiver.class);
        notificationIntent.putExtra(Constants.POPUP_EXTRA, scheduledAt);
        notificationIntent.putExtra(Constants.POPUP_MEETING_URL, urlOfTopic);
        notificationIntent.setAction(Constants.POPUP_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = startTime - TimeUnit.MINUTES.toMillis(delay);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
        } else {
            Log.e(TAG, "ALARM MANAGER NULL");
        }
    }

}
