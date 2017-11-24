package kinect.pro.meetingapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.firebase.DatabaseManager;

/**
 * Created by dobrikostadinov on 11/24/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract boolean isBackNavigationActivity();

    protected abstract int getActivityLayout();

    protected abstract String getScreenTitle();


    @Inject
    protected DatabaseManager databaseManager;

    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.ivDehaze)
    View toolbarDrawerIcon;

    @BindView(R.id.navigation_back)
    View backNavigationView;

    @BindView(R.id.header_title)
    TextView headerTitle;

    @OnClick(R.id.navigation_back)
    void onBack() {
        finish();
    }

    protected Unbinder unBinder;


    protected void attachFragment(int resId, Fragment fragment, String TAG) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(resId, fragment, TAG);
        t.addToBackStack(TAG);
        t.commit();
    }

    protected void showFragment(String tag) {
        getSupportFragmentManager().popBackStackImmediate(tag, 0);
    }

    protected boolean isFragmentAttached(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag) != null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getActivityLayout());

        unBinder = ButterKnife.bind(this);

        if (isBackNavigationActivity()) {
            backNavigationView.setVisibility(View.VISIBLE);
            toolbarDrawerIcon.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(getScreenTitle())) {
            headerTitle.setVisibility(View.VISIBLE);
            headerTitle.setText(getScreenTitle());
        }
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
