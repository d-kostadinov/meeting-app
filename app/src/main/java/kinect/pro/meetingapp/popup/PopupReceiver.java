package kinect.pro.meetingapp.popup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import javax.inject.Inject;

import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.activity.PopupActivity;
import kinect.pro.meetingapp.firebase.DatabaseManager;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.model.Participant;
import kinect.pro.meetingapp.other.Constants;

import static kinect.pro.meetingapp.other.Constants.POPUP_EXTRA;

public class PopupReceiver extends BroadcastReceiver {

    private static final String TAG = PopupReceiver.class.getSimpleName();

    @Inject
    DatabaseManager databaseManager;

    public void onReceive(Context context, Intent intent) {
        ((App)context.getApplicationContext()).getAppComponent().inject(this);
        if (intent != null && intent.getAction() != null && intent.getAction().equals(Constants.POPUP_RECEIVER_ACTION)) {
            String meetingUrl = intent.getStringExtra(Constants.POPUP_MEETING_URL);
            if (!TextUtils.isEmpty(meetingUrl)) {
                DatabaseReference meetingReference = databaseManager.getCurrentMeetingReference(meetingUrl);
                meetingReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange popupReceiver");
                        MeetingModels meetingToCheck = dataSnapshot.getValue(MeetingModels.class);
                        if (meetingToCheck != null) {
                            List<Participant> listOfParticipant = meetingToCheck.getParticipants();
                            if (listOfParticipant != null && !listOfParticipant.isEmpty()) {
                                boolean isAllConfirmed = false;
                                for (Participant participant : listOfParticipant) {
                                    if (participant.getStatus().equals(Constants.STATUS_CONFIDMED)) {
                                        isAllConfirmed = true;
                                    } else {
                                        isAllConfirmed = false;
                                    }
                                }
                                if (isAllConfirmed) {
                                    wakeUpAndShowPopup(context, intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled popupReceiver");
                        databaseError.toException().printStackTrace();
                    }
                });

            }

        }
    }


    private void wakeUpAndShowPopup(Context context, Intent intent) {
        PowerManager powerManager = ((PowerManager) context.getSystemService(Context.POWER_SERVICE));
        intent.getStringExtra(POPUP_EXTRA);
        if (powerManager != null) {
            PowerManager.WakeLock wake = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            wake.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
        Intent popupIntent = new Intent(context, PopupActivity.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        popupIntent.putExtra(POPUP_EXTRA, intent.getStringExtra(POPUP_EXTRA));
        context.startActivity(popupIntent);
    }
}