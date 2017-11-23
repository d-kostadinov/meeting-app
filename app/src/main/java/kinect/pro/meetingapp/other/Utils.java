package kinect.pro.meetingapp.other;


import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.model.MeetingModels;

import static kinect.pro.meetingapp.other.Constants.STATUS_CANCELED;
import static kinect.pro.meetingapp.other.Constants.STATUS_CONFIDMED;
import static kinect.pro.meetingapp.popup.PopupAlarmManager.scheduleNotification;


public class Utils {

    public static boolean isNetworkOnline(Context context) {
        ConnectivityManager cManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }


    public static int getMeetingColor(Context context, MeetingModels meetingModels) {
        int count = 0;
        for (int i = 0; i < meetingModels.getParticipants().size(); i++) {
            if (meetingModels.getParticipants().get(i).getStatus().equals(STATUS_CANCELED))
                return R.color.colorDarksalmon;
            if (meetingModels.getParticipants().get(i).getStatus().equals(STATUS_CONFIDMED))
                count++;
        }
        if (meetingModels.getParticipants().size() == count) {
            return R.color.colorSilver;
        }
        return R.color.colorKhaki;
    }
}
