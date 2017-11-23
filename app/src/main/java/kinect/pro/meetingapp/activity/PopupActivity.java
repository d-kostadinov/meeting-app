package kinect.pro.meetingapp.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;

import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.other.Constants;

public class PopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.popup_layout);

        TextView textViewMeeting = findViewById(R.id.tvTitleMeeting);

        String stringExtra = getIntent().getStringExtra(Constants.POPUP_EXTRA);
        textViewMeeting.setText(stringExtra);

        SwipeButton enableButton = findViewById(R.id.swipe_btn);
        enableButton.setOnStateChangeListener(active -> finish());
    }
}