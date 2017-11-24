package kinect.pro.meetingapp.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.firebase.DatabaseManager;
import kinect.pro.meetingapp.fragment.FraCalendar;

public class CalendarActivity extends BaseActivity implements DatabaseManager.OnDatabaseDataChanged {

    private static final String TAG = "CalendarActivity ==>> ";

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.activity_main_drawer)
    DrawerLayout mDrawer;

    @BindView(R.id.iv_app_name)
    View toolbarAppNameView;

    private FraCalendar fraCalendar;

    @Override
    protected boolean isBackNavigationActivity() {

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((App) getApplication()).getAppComponent().inject(this);

        fab.setOnClickListener(view -> {
            if (isPermissionGranted())
                startActivity(new Intent(CalendarActivity.this, CreateMeetingActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        fraCalendar = FraCalendar.newInstance();
        attachFragment(R.id.act_main_container, fraCalendar, FraCalendar.class.getSimpleName());

        requestPermission();


    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_calendar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseManager.setDatabaseManagerListener(this);
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseManager.setDatabaseManagerListener(null);
    }

    private void updateView() {
        databaseManager.initMeeting();
    }

    @OnClick(R.id.left_slider_menu_item2)
    void switchToOneDay() {
        fraCalendar.switchToOneDay();

        closeLeftDrawer();
    }

    @OnClick(R.id.left_slider_menu_item3)
    void switchToCalendar() {

        fraCalendar.switchToCalendar();

        closeLeftDrawer();
    }

    @OnClick(R.id.ivDehaze)
    public void onClickHome(View view) {

        mDrawer.openDrawer(Gravity.LEFT);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0) {

            public void onDrawerClosed(View view) {

                toolbarDrawerIcon.setVisibility(View.VISIBLE);
                toolbarAppNameView.setVisibility(View.GONE);

                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {

                toolbarDrawerIcon.setVisibility(View.GONE);
                toolbarAppNameView.setVisibility(View.VISIBLE);

                super.onDrawerOpened(drawerView);
            }
        };

        mDrawer.addDrawerListener(drawerToggle);
    }

    @OnClick(R.id.left_slider_profile_container)
    public void onClickProfile(View view) {
        startActivity(new Intent(CalendarActivity.this, ProfileDetailsActivity.class));
    }

    private void closeLeftDrawer() {
        if (mDrawer.isDrawerOpen(Gravity.LEFT)) {
            mDrawer.closeDrawer(Gravity.LEFT);
        }
    }

    //adding a meeting to the calendar


    @Override
    public void onDataChanged(DataSnapshot dataSnapshot) {

        fraCalendar.notifyDatasetChanged();

    }

    @Override
    public void onCancelled(DatabaseError error) {
        //ignore
    }


    private boolean isPermissionGranted() {
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
                new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
    }
}
