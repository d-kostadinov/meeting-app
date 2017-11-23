package kinect.pro.meetingapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.esafirm.imagepicker.features.ImagePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

import javax.inject.Inject;

import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.firebase.DatabaseManager;
import kinect.pro.meetingapp.model.Profile;

public class ProfileDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICKER = 1;
    EditText name;
    RadioGroup radioGroup;
    RadioButton radioButtonBusiness;
    RadioButton radioButtonPrivate;
    Spinner spinner;
    Button btnSubmit;
    SharedPreferences prefs;
    ImageView imageAvatar;

    @Inject
    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        ((App) getApplication()).getAppComponent().inject(this);

        databaseManager.initProfile();

        prefs = getPreferences(MODE_PRIVATE);

        name = findViewById(R.id.textName);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonBusiness = findViewById(R.id.radioButtonBusiness);
        radioButtonPrivate = findViewById(R.id.radioButtonPrivate);
        spinner = findViewById(R.id.spinner);
        btnSubmit = findViewById(R.id.btnSubmits);
        imageAvatar = findViewById(R.id.avatarImage);

        if (prefs.contains("profile_avatar")){
            imageAvatar.setImageURI(Uri.parse(prefs.getString("profile_avatar", "")));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonBusiness:
                    spinner.setEnabled(true);
                    break;
                case R.id.radioButtonPrivate:
                    spinner.setEnabled(false);
                    break;
            }
        });

        btnSubmit.setOnClickListener(view -> {
            saveProfile();
            startActivity(new Intent(ProfileDetailsActivity.this, CalendarActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        imageAvatar.setOnClickListener(view -> setAvatar());

        updateUI();
    }

    private void setAvatar(){
        ImagePicker.create(this)
                .single()
                .start(REQUEST_CODE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<com.esafirm.imagepicker.model.Image> images =  (ArrayList<com.esafirm.imagepicker.model.Image>) ImagePicker.getImages(data);
            prefs.edit().putString("profile_avatar", images.get(0).getPath()).apply();
            loadImage(images.get(0).getPath());
        }
    }

    private void  loadImage(String path){
        imageAvatar.setImageURI(Uri.parse(path));
    }


    private void updateUI() {
        if (databaseManager.getProfile() != null){
            name.setText(databaseManager.getProfile().getProfileName());
            if (databaseManager.getProfile().getProfileType().equals("private")) {
                radioButtonPrivate.setChecked(true);
            } else {
                radioButtonBusiness.setChecked(true);
            }
            spinner.setSelection(databaseManager.getProfile().getProfileBusinessType());
        }
    }

    private void saveProfile() {
        databaseManager.saveProfile(new Profile(name.getText().toString(),
                radioButtonPrivate.isChecked() ? "private" : "business",
                spinner.getSelectedItemPosition()));
    }
}
