package kinect.pro.meetingapp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.other.Constants;

import static kinect.pro.meetingapp.other.Constants.DATE_FORMAT;
import static kinect.pro.meetingapp.other.Constants.DEFAULT;
import static kinect.pro.meetingapp.other.Constants.KEY_INFO_EVENT;
import static kinect.pro.meetingapp.other.Constants.KEY_PHONE;
import static kinect.pro.meetingapp.other.Constants.MEETING;
import static kinect.pro.meetingapp.other.Constants.STATUS_CANCELED;
import static kinect.pro.meetingapp.other.Constants.STATUS_CONFIDMED;
import static kinect.pro.meetingapp.other.Constants.URL_MEETING;
import static kinect.pro.meetingapp.popup.PopupAlarmManager.scheduleNotification;

public class InfoMeetingActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "InfoMeetingActivity ->";

    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.tvMeetingTopic)
    TextView tvMeetingTopic;
    @BindView(R.id.tvParticipant)
    TextView tvParticipant;
    @BindView(R.id.tvStartMeeting)
    TextView tvStartMeeting;
    @BindView(R.id.tvStopMeeting)
    TextView tvStopMeeting;
    @BindView(R.id.tvLocations)
    TextView tvLocations;
    @BindView(R.id.mapView)
    LinearLayout linearLayoutMap;
    @BindView(R.id.layoutBtn)
    LinearLayout linearLayoutBtn;

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private MeetingModels mMeetingModels;
    private GoogleMap mMap;
    private String mMyPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_meeting);
        ButterKnife.bind(this);
        ((App) getApplication()).getAppComponent().inject(this);
        mDatabase = FirebaseDatabase.getInstance();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mapFragment.getView() != null) {
            mapFragment.getView().setClickable(false);
        }

        initFirebase(sharedPreferences.getString(KEY_INFO_EVENT, null));

        mMyPhoneNumber = sharedPreferences.getString(KEY_PHONE, DEFAULT);
    }

    private void initFirebase(String event) {
        mDatabaseReference = mDatabase.getReference(URL_MEETING);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    MeetingModels mModels = noteDataSnapshot.getValue(MeetingModels.class);
                    if (mModels != null) {
                        if (mModels.getTopic().equals(event)) {
                            if (mModels.getParticipants().get(0).getStatus() != null)
                                mMeetingModels = mModels;
                            initView(mMeetingModels);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    private void initView(MeetingModels meetingModels) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < meetingModels.getParticipants().size(); i++) {
            stringBuilder.append(meetingModels.getParticipants().get(i).getMember())
                    .append(" - ").append(meetingModels.getParticipants().get(i).getStatus())
                    .append("\n");
        }
        tvParticipant.setText(stringBuilder);
        tvMeetingTopic.setText(meetingModels.getTopic());
        tvStartMeeting.setText(String.format("%s - %s", getResources().getText(R.string.start_time), dateFormat.format(meetingModels.getDate())));
        tvStopMeeting.setText(String.format("%s - %s", getResources().getText(R.string.stop_time), dateFormat.format(meetingModels.getDuration())));
        tvLocations.setText(meetingModels.getLocation());
        initMap(meetingModels.getLatitude(), meetingModels.getLongitude());
        initBtnBottom(meetingModels);
    }

    public void initBtnBottom(MeetingModels meetingModels){
        for (int i = 0; i < meetingModels.getParticipants().size(); i++) {
            if (meetingModels.getParticipants().get(i).getMember()
                    .equals(mMyPhoneNumber)){
                if (!meetingModels.getParticipants().get(i).getStatus().equals(Constants.STATUS_PENDING)) {
                   linearLayoutBtn.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initMap(float latitude, float longitude) {
        if (latitude != 0L && longitude != 0L) {
            LatLng meetingMarker = new LatLng(mMeetingModels.getLatitude(), mMeetingModels.getLongitude());
            mMap.addMarker(new MarkerOptions().position(meetingMarker).title(MEETING));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(meetingMarker, 15));
        } else {
            linearLayoutMap.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @OnClick(R.id.btnCanceled)
    public void btnCanceled(View view) {
        for (int i = 0; i < mMeetingModels.getParticipants().size(); i++) {
            if (mMeetingModels.getParticipants().get(i).getMember().equals(mMyPhoneNumber)) {
                String nameMeeting = mMeetingModels.getTopic().replaceAll("\\W", "");
                mDatabaseReference = mDatabase.getReference(URL_MEETING + nameMeeting);
                mMeetingModels.getParticipants().get(i).setStatus(STATUS_CANCELED);
                mDatabaseReference.setValue(mMeetingModels);
                finish();
            }
        }
    }

    @OnClick(R.id.btnConfirmed)
    public void btnConfirmed(View view) {
        for (int i = 0; i < mMeetingModels.getParticipants().size(); i++) {
            if (mMeetingModels.getParticipants().get(i).getMember().equals(mMyPhoneNumber)) {
                String nameMeeting = mMeetingModels.getTopic().replaceAll("\\W", "");
                mDatabaseReference = mDatabase.getReference(URL_MEETING + nameMeeting);
                mMeetingModels.getParticipants().get(i).setStatus(STATUS_CONFIDMED);
                mDatabaseReference.setValue(mMeetingModels);
                scheduleNotificationApp(getApplicationContext(), mMeetingModels);
                finish();
            }
        }
    }

    private static void scheduleNotificationApp(Context context, MeetingModels meetingModels) {
        String nameMeeting = meetingModels.getTopic().replaceAll("\\W", "");
        if (System.currentTimeMillis() < meetingModels.getDate()) {
            final String timeString = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(meetingModels.getDate());
            String scheduledAt = meetingModels.getTopic() + "\n at -" + timeString;
            scheduleNotification(context, scheduledAt, nameMeeting, meetingModels.getDate(), Integer.parseInt(meetingModels.getReminder()));
        }
    }

    @OnClick(R.id.ivHome)
    public void onClickHome(View view) {
        onBackPressed();
    }
}

