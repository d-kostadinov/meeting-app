package kinect.pro.meetingapp.di;


import javax.inject.Singleton;

import dagger.Component;
import kinect.pro.meetingapp.activity.CalendarActivity;
import kinect.pro.meetingapp.activity.CreateMeetingActivity;
import kinect.pro.meetingapp.activity.InfoMeetingActivity;
import kinect.pro.meetingapp.activity.LoginActivity;
import kinect.pro.meetingapp.activity.ProfileDetailsActivity;
import kinect.pro.meetingapp.popup.PopupReceiver;
import kinect.pro.meetingapp.rest.NetModule;

@Singleton
@Component(modules = {ApplicationModule.class, NetModule.class})
public interface AppComponent {

    void inject(LoginActivity activity);

    void inject(CreateMeetingActivity activity);

    void inject(CalendarActivity activity);

    void inject(InfoMeetingActivity activity);

    void inject(PopupReceiver receiver);

    void inject(ProfileDetailsActivity activity);
}