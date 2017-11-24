package kinect.pro.meetingapp.fragment;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.activity.BaseActivity;
import kinect.pro.meetingapp.activity.InfoMeetingActivity;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.other.Utils;

import static kinect.pro.meetingapp.other.Constants.KEY_INFO_EVENT;
import static kinect.pro.meetingapp.other.Constants.TYPE_DAY_VIEW_ONE_DAY;
import static kinect.pro.meetingapp.other.Constants.TYPE_DAY_VIEW_ONE_WEEK;

/**
 * Created by dobrikostadinov on 11/24/17.
 */

public class FraCalendar extends Fragment implements MonthLoader.MonthChangeListener,
        WeekView.EventClickListener {

    public static final String TAG = FraCalendar.class.getSimpleName();

    public static FraCalendar newInstance() {
        return new FraCalendar();
    }

    @BindView(R.id.weekView)
    WeekView weekView;

    private Unbinder unBinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fra_calendar, container, false);

        unBinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();

        initWeekView();

        weekView.goToToday();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unBinder.unbind();
    }

    public void initWeekView() {

        weekView.setMonthChangeListener(this);
        weekView.setOnEventClickListener(this);

        switchToOneDay();
    }

    public void switchToOneDay() {
        weekView.setNumberOfVisibleDays(TYPE_DAY_VIEW_ONE_DAY);
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));

    }

    public void switchToCalendar() {
        weekView.setNumberOfVisibleDays(TYPE_DAY_VIEW_ONE_WEEK);
        weekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        weekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 6, getResources().getDisplayMetrics()));
        weekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));

    }

    public void notifyDatasetChanged() {
        weekView.notifyDatasetChanged();

    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> eventsMonth = new ArrayList<>();
        ArrayList<MeetingModels> meetingModels = ((BaseActivity) getActivity()).getDatabaseManager().getMeetingModels();
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
            event.setColor(getResources().getColor(Utils.getMeetingColor(getActivity(), meetingModels.get(i))));
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
        ((BaseActivity) getActivity()).getSharedPreferences().edit().putString(KEY_INFO_EVENT, event.getName()).apply();
        startActivity(new Intent(getActivity(), InfoMeetingActivity.class));
    }

}
