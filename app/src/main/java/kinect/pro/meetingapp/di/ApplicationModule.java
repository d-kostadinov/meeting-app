package kinect.pro.meetingapp.di;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import kinect.pro.meetingapp.auth.AuthManager;
import kinect.pro.meetingapp.firebase.DatabaseManager;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

@Module
public class ApplicationModule {

    private Context mContext;

    public ApplicationModule(Context context) {
        this.mContext = context;
    }

    @Provides
    @Singleton
    DatabaseManager provideDataModule(SharedPreferences preferences) {
        return new DatabaseManager(preferences);
    }

    @Provides
    SharedPreferences providesSharedPreferences() {
        return getDefaultSharedPreferences(mContext);
    }

    @Provides
    AuthManager provideAuthManager(SharedPreferences preferences) {
        return new AuthManager(preferences);
    }

}
