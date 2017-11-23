package kinect.pro.meetingapp;


import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import kinect.pro.meetingapp.di.AppComponent;
import kinect.pro.meetingapp.di.ApplicationModule;
import kinect.pro.meetingapp.di.DaggerAppComponent;

public class App extends Application {

    private AppComponent sAppComponent;

    public AppComponent getAppComponent() {
        if (sAppComponent == null) {
            buildAppComponent();
        }
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildAppComponent();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private void buildAppComponent() {
        sAppComponent = DaggerAppComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }
}
