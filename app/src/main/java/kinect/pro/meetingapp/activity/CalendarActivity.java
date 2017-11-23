package kinect.pro.meetingapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.firebase.DatabaseManager;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.other.Utils;

import static kinect.pro.meetingapp.other.Constants.KEY_INFO_EVENT;
import static kinect.pro.meetingapp.other.Constants.TYPE_DAY_VIEW;

public class CalendarActivity extends AppCompatActivity implements MonthLoader.MonthChangeListener,
        WeekView.EventClickListener, DatabaseManager.OnDatabaseDataChanged {

    private static final String TAG = "CalendarActivity ==>> ";

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    DatabaseManager databaseManager;

    @BindView(R.id.weekView)
    WeekView weekView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        ((App) getApplication()).getAppComponent().inject(this);

        fab.setOnClickListener(view -> {
            if (isPermissionGranted())
            startActivity(new Intent(CalendarActivity.this, CreateMeetingActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        requestPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseManager.setDatabaseManagerListener(this);
        updateView();
        weekView.goToToday();
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseManager.setDatabaseManagerListener(null);
    }

    private void updateView() {
        databaseManager.initMeeting();
        initWeekView();
    }

    private void initWeekView() {
        weekView.setMonthChangeListener(this);
        weekView.setOnEventClickListener(this);
        weekView.setNumberOfVisibleDays(TYPE_DAY_VIEW);
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    @OnClick(R.id.ivDehaze)
    public void onClickHome(View view) {
        startActivity(new Intent(CalendarActivity.this, ProfileDetailsActivity.class));
    }

    //adding a meeting to the calendar
    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> eventsMonth = new ArrayList<>();
        ArrayList<MeetingModels> meetingModels = databaseManager.getMeetingModels();
        List<WeekViewEvent> mEvents = new ArrayList<>();
        for (int i = 0; i < meetingModels.size(); i++) {
            StringBuilder participants = new StringBuilder();
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(meetingModels.get(i).getDate());
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(meetingModels.get(i).getDuration());

            participants.append("\n");
            for (int j = 0; j < meetingModels.get(i).getParticipants().size(); j++) {
                participants.append(meetingModels.get(i).getParticipants().get(j).getMember()).append(" ");
            }

            WeekViewEvent event = new WeekViewEvent(10,
                    meetingModels.get(i).getTopic(),
                    participants.toString(),
                    startTime,
                    endTime);
            event.setColor(getResources().getColor(Utils.getMeetingColor(this, meetingModels.get(i))));
            mEvents.add(event);
        }

        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getStartTime().get(Calendar.MONTH) == newMonth - 1
                    && mEvents.get(i).getStartTime().get(Calendar.YEAR) == newYear) {
                eventsMonth.add(mEvents.get(i));
            }
        }
        return eventsMonth;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        sharedPreferences.edit().putString(KEY_INFO_EVENT, event.getName()).apply();
        startActivity(new Intent(CalendarActivity.this, InfoMeetingActivity.class));
    }

    @Override
    public void onDataChanged(DataSnapshot dataSnapshot) {
        weekView.notifyDatasetChanged();
    }

    @Override
    public void onCancelled(DatabaseError error) {
        //ignore
    }


    private boolean isPermissionGranted(){
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CalendarActivity.this, "Permission granted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CalendarActivity.this, "Permission denied!", Toast.LENGTH_LONG).show();
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_CONTACTS},1);
    }
}
