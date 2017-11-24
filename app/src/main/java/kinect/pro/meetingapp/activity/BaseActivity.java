package kinect.pro.meetingapp.activity;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import butterknife.Unbinder;
import kinect.pro.meetingapp.firebase.DatabaseManager;

/**
 * Created by dobrikostadinov on 11/24/17.
 */

public class BaseActivity extends AppCompatActivity {

    @Inject
    protected DatabaseManager databaseManager;

    @Inject
    SharedPreferences sharedPreferences;

    protected Unbinder unBinder;


    protected void attachFragment(int resId, Fragment fragment, String TAG) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(resId, fragment, TAG);
        t.commit();
    }

    protected void attachFragment(int resId, Fragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(resId, fragment, fragment.getClass().getSimpleName());
        t.commit();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (unBinder != null) {
            unBinder.unbind();
        }
    }
}
