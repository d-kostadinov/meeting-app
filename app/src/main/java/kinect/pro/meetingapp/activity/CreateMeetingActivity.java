package kinect.pro.meetingapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.fastaccess.datetimepicker.DatePickerFragmentDialog;
import com.fastaccess.datetimepicker.DateTimeBuilder;
import com.fastaccess.datetimepicker.callback.DatePickerCallback;
import com.fastaccess.datetimepicker.callback.TimePickerCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.firebase.DatabaseManager;
import kinect.pro.meetingapp.model.Contact;
import kinect.pro.meetingapp.model.ContactsModels;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.model.Participant;
import kinect.pro.meetingapp.other.Constants;
import kinect.pro.meetingapp.other.ContactsAdapter;
import kinect.pro.meetingapp.other.Utils;
import kinect.pro.meetingapp.rest.RestController;

import static java.util.Locale.ENGLISH;
import static kinect.pro.meetingapp.other.Constants.DATE_FORMAT;
import static kinect.pro.meetingapp.other.Constants.DEFAULT;
import static kinect.pro.meetingapp.other.Constants.KEY_PHONE;
import static kinect.pro.meetingapp.other.Constants.PLACE_PICKER_REQUEST;
import static kinect.pro.meetingapp.popup.PopupAlarmManager.scheduleNotification;


public class CreateMeetingActivity extends AppCompatActivity
        implements DatePickerCallback, TimePickerCallback,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        DatabaseManager.OnDatabaseDataChanged {

    private final static String TAG = "CreateMeetingActivity";
    private static GoogleApiClient mGoogleApiClient;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    RestController restController;

    @Inject
    DatabaseManager databaseManager;

    @BindView(R.id.etMeetingTopic)
    EditText etMeetingTopic;
    @BindView(R.id.etContacts)
    MultiAutoCompleteTextView etContacts;
    @BindView(R.id.etDateTime)
    EditText etDateTime;
    @BindView(R.id.etDuration)
    EditText etDuration;
    @BindView(R.id.etLocation)
    EditText etLocation;
    @BindView(R.id.etReminder)
    EditText etReminder;
    private ArrayList<ContactsModels> mListContactsModels;
    private ArrayList<Participant> mListParticipant;

    private ArrayList<Contact> mListContactsPhone;
    private ContactsAdapter mAdapterContacts;

    private int mFirstAlwaysDate = 0;
    private float mLatitude = 0L;
    private float mLongitude = 0L;
    private long mStartTime = 0;
    private long mStopTime = 0;

    private static final int CONTACT_PICKER_RESULT = 1001;

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((App) getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this);
        verificationInternet();

        initGoogleMapClient();
        initViewAndLists();
        databaseManager.saveCurrentUserDetails();
        databaseManager.subscribeToContactsUpdates(this);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void initGoogleMapClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void initViewAndLists() {
        mListContactsModels = new ArrayList<>();
        mListContactsPhone = new ArrayList<>();
        mListParticipant = new ArrayList<>();

        mAdapterContacts = new ContactsAdapter(this,
                android.R.layout.simple_spinner_item, mListContactsPhone);

        etContacts.setAdapter(mAdapterContacts);
        etContacts.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        initContactsList();
    }

    private boolean createGroupContacts() {
        Set<String> hashSet = new HashSet<>();
        String temp = etContacts.getText().toString();
        String[] numbers = temp.split(", ");
        hashSet.addAll(Arrays.asList(numbers));

        mListParticipant.clear();
        mListParticipant.add(new Participant(sharedPreferences.getString(KEY_PHONE, DEFAULT), Constants.STATUS_CONFIDMED));

        String currentPhoneNumber = sharedPreferences.getString(KEY_PHONE, DEFAULT);
        if (hashSet.contains(currentPhoneNumber)) {
            return true;
        }
        ArrayList<String> memberList = new ArrayList<>(hashSet);
        Phonenumber.PhoneNumber swissNumberProto = null;
        for (int i = 0; i < memberList.size(); i++) {
            try {
                swissNumberProto = PhoneNumberUtil.getInstance().parse(memberList.get(i), Constants.DEFAULT_REGION);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
            try {
                if (!PhoneNumberUtil.getInstance().isValidNumber(swissNumberProto)) {
                    return true;
                } else
                    mListParticipant.add(new Participant(memberList.get(i), Constants.STATUS_PENDING));
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    public void initContactsList(){

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds
                .Phone.CONTENT_URI, null,null,null, null);
        try{
            while (phones.moveToNext())
            {
                mListContactsPhone.add(new Contact(phones.getString(phones
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                        phones.getString(phones
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
            }
            phones.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        mAdapterContacts.notifyDataSetChanged();
    }

    public void createListContacts() {
//        mListContactsPhone.clear();
//        for (int i = 0; i < mListContactsModels.size(); i++) {
//            if (!mListContactsModels.get(i).getPhone().equals(sharedPreferences.getString(KEY_PHONE, DEFAULT)))
//                mListContactsPhone.add(mListContactsModels.get(i).getPhone());
//        }
//        mAdapterContacts.notifyDataSetChanged();
    }

    @OnFocusChange(R.id.etLocation)
    public void onClickLocation(View view, boolean isFocused) {
        if (isFocused) {
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(this);
                startActivityForResult(intent, 1);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.etLocation)
    public void onClickLocation(View view) {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.peekContacts)
    public void onClickPeekContacts(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
        etContacts.requestFocus();
        etContacts.setSelection(etContacts.getText().length());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case CONTACT_PICKER_RESULT: {
                        Uri contactUri =
                                data.getData();

                        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                        Cursor cursor = getContentResolver()
                                .query(contactUri, projection, null,
                                        null, null);
                        cursor.moveToFirst();

                        int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String number = cursor.getString(column);

                        Log.d(TAG, number);

                        cursor.close();

                        etContacts.append(number.replace(" ", "") + ", ");

                        break;
                    }
                    case PLACE_PICKER_REQUEST: {
                        Place place = PlacePicker.getPlace(data, this);
                        etLocation.setText(place.getAddress());
                        mLatitude = (float) place.getLatLng().latitude;
                        mLongitude = (float) place.getLatLng().longitude;
                        break;
                    }
                }
        } else {
                Log.d(TAG, "fgfg");
            }
    }

    private void showDatePickerFragment(long minDate) {
        if (minDate == 0)
            minDate = System.currentTimeMillis();
        DatePickerFragmentDialog.newInstance(
                DateTimeBuilder.get()
                        .withTime(true)
                        .with24Hours(true)
                        .withSelectedDate(System.currentTimeMillis())
                        .withMinDate(minDate)
                        .withTheme(R.style.PickersTheme))
                .show(getSupportFragmentManager(), "StartDatePickerFragmentDialog");
    }

    @OnFocusChange(R.id.etDateTime)
    public void onClickDataTime(View view, boolean isFocused) {
        if (isFocused) {
            mFirstAlwaysDate = 1;
            showDatePickerFragment(System.currentTimeMillis());
        }
    }

    @OnClick(R.id.etDateTime)
    public void onClickDataTime(View view) {
        mFirstAlwaysDate = 1;
        showDatePickerFragment(System.currentTimeMillis());
    }

    @OnFocusChange(R.id.etDuration)
    public void onClickDuration(View view, boolean isFocused) {
        if (isFocused) {
            mFirstAlwaysDate = 2;
            showDatePickerFragment(mStartTime);
        }
    }

    @OnClick(R.id.etDuration)
    public void onClickDuration(View view) {
        mFirstAlwaysDate = 2;
        showDatePickerFragment(mStartTime);
    }

    @OnClick(R.id.btnCreateMeeting)
    public void onClickOk(View view) {
        String meetingTopic = etMeetingTopic.getText().toString();
        if (meetingTopic.isEmpty()) {
            Toast.makeText(this, "The meeting field must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        String contacts = etContacts.getText().toString();
        if (contacts.isEmpty()) {
            Toast.makeText(this, "The contact field must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = etDateTime.getText().toString();
        if (date.isEmpty()) {
            Toast.makeText(this, "The Start date field must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        String duration = etDuration.getText().toString();
        if (duration.isEmpty()) {
            Toast.makeText(this, "The Stop date field must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mStartTime > mStopTime) {
            Toast.makeText(this, "Wrong time set!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mStopTime - mStartTime < 300000) { //5 min
            Toast.makeText(this, "The minimum time for a meeting is 5 minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        if (new Date().getTime() > mStartTime) {
            Toast.makeText(this, "Start time has passed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (createGroupContacts()) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.parseInt(etReminder.getText().toString()) > 60) {
            Toast.makeText(this, "Must be less then 60 mins", Toast.LENGTH_SHORT).show();
            return;
        }

        searchNewUsers();
        MeetingModels meetingModels = new MeetingModels(
                sharedPreferences.getString(KEY_PHONE, DEFAULT),
                etMeetingTopic.getText().toString(),
                mStartTime,
                mStopTime,
                etLocation.getText().toString(),
                mLatitude,
                mLongitude,
                (etReminder.getText().toString().equals("")) ? "0" : etReminder.getText().toString(),
                mListParticipant);

        String nameMeeting = etMeetingTopic.getText().toString().replaceAll("\\W", "");
        if (!etReminder.getText().toString().isEmpty()) {
            final String timeString = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(mStartTime);
            String scheduledAt = etMeetingTopic.getText().toString() + "\n at -" + timeString;
            scheduleNotification(this, scheduledAt, nameMeeting, mStartTime, Integer.parseInt(etReminder.getText().toString()));
        }
        //send
        databaseManager.createNewMeeting(meetingModels, nameMeeting);

        startActivity(new Intent(CreateMeetingActivity.this, CalendarActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        Toast.makeText(this, "Meeting create!", Toast.LENGTH_SHORT).show();
    }



    @OnClick(R.id.btnCancel)
    public void onClick(View view) {
        finish();
    }

    private void searchNewUsers() {
        for (int i = 0; i < mListParticipant.size(); i++) {
            int count = 0;
            for (int j = 0; j < mListContactsModels.size(); j++) {
                if (!mListParticipant.get(i).getMember().equals(mListContactsModels.get(j).getPhone())) {
                    count++;
                } else {
                    count--;
                }
                if (count == mListContactsModels.size()) {
                    restController.inviteToMeeting(this, mListParticipant.get(i).getMember(), etMeetingTopic.getText().toString());
                }
            }
        }
    }


    private void verificationInternet() {
        if (!Utils.isNetworkOnline(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(long date) {
    }

    @Override
    public void onTimeSet(long timeOnly, long dateWithTime) {
        if (mFirstAlwaysDate == 1) {
            mStartTime = dateWithTime;
            if (dateWithTime < System.currentTimeMillis()) {
                Toast.makeText(this, "Start time has passed", Toast.LENGTH_SHORT).show();
                return;
            }
            etDateTime.setText(new SimpleDateFormat(DATE_FORMAT, ENGLISH).format(dateWithTime));
            mFirstAlwaysDate = 0;
        }
        if (mFirstAlwaysDate == 2) {
            if (mStartTime < dateWithTime) {
                mStopTime = dateWithTime;
                if (dateWithTime < System.currentTimeMillis() || dateWithTime < mStartTime) {
                    Toast.makeText(this, "Start time has passed", Toast.LENGTH_SHORT).show();
                    return;
                }
                etDuration.setText(new SimpleDateFormat(DATE_FORMAT, ENGLISH).format(dateWithTime));
                mFirstAlwaysDate = 0;
            } else {
                Toast.makeText(this, "The start time can not be later than the end time", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, connectionResult.getErrorMessage());
    }

    @Override
    public void onDataChanged(DataSnapshot dataSnapshot) {
        // TODO: uncomment or delete
//        mListContactsModels.clear();
//        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
//            ContactsModels contactsModels = noteDataSnapshot.getValue(ContactsModels.class);
//            mListContactsModels.add(contactsModels);
//        }
//        createListContacts();
    }

    @Override
    public void onCancelled(DatabaseError error) {

    }
}