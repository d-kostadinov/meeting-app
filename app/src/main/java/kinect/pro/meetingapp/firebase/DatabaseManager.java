package kinect.pro.meetingapp.firebase;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Module;
import kinect.pro.meetingapp.model.ContactsModels;
import kinect.pro.meetingapp.model.MeetingModels;
import kinect.pro.meetingapp.model.Profile;
import kinect.pro.meetingapp.other.Constants;

import static kinect.pro.meetingapp.other.Constants.DEFAULT;
import static kinect.pro.meetingapp.other.Constants.KEY_PHONE;
import static kinect.pro.meetingapp.other.Constants.KEY_PROVIDER_ID;
import static kinect.pro.meetingapp.other.Constants.KEY_UID;
import static kinect.pro.meetingapp.other.Constants.URL_CONTACTS;
import static kinect.pro.meetingapp.other.Constants.URL_MEETING;
import static kinect.pro.meetingapp.other.Constants.URL_PROFILE;


@Module
public class DatabaseManager {

    private static final String TAG = DatabaseManager.class.getSimpleName();
    private ArrayList<MeetingModels> mListMeetingModels;
    private ArrayList<String> mListMeetups;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMeetingReference;
    private DatabaseReference mContantsReference;
    private SharedPreferences mPreference;
    private OnDatabaseDataChanged mDatabaseListener;

    private Profile profile;

    private DatabaseReference mProfileReference;

    @Inject
    public DatabaseManager(SharedPreferences sharedPreferences) {
        mDatabase = FirebaseDatabase.getInstance();
        mListMeetups = new ArrayList<>();
        mListMeetingModels = new ArrayList<>();
        mPreference = sharedPreferences;
        mMeetingReference = mDatabase.getReference(URL_MEETING);
        mContantsReference = mDatabase.getReference(URL_CONTACTS);

        mProfileReference = mDatabase.getReference(URL_PROFILE);
    }

    public void setDatabaseManagerListener(OnDatabaseDataChanged listener) {
        mDatabaseListener = listener;
    }

    public void initProfile(){
        mProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                profile = dataSnapshot.getValue(Profile.class);

                if (mDatabaseListener != null) {
                    mDatabaseListener.onDataChanged(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (mDatabaseListener != null) {
                    mDatabaseListener.onCancelled(databaseError);
                }
            }
        });

        mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile = dataSnapshot.getValue(Profile.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void initMeeting() {
        mMeetingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                mListMeetingModels.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    MeetingModels meetingModels = noteDataSnapshot.getValue(MeetingModels.class);
                    if (meetingModels != null) {
                        mListMeetups.clear();
                        for (int i = 0; i < meetingModels.getParticipants().size(); i++) {
                            if (meetingModels.getParticipants().get(i).getMember()
                                    .equals(mPreference.getString(KEY_PHONE, DEFAULT))) {
                                if (!mListMeetups.contains(meetingModels.getTopic())) {
                                    mListMeetingModels.add(meetingModels);
                                    mListMeetups.add(meetingModels.getTopic());
                                    Log.d(TAG, "Update mDatabase");
                                }
                            }
                        }
                    }
                }
                if (mDatabaseListener != null) {
                    mDatabaseListener.onDataChanged(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, error.getMessage());
                if (mDatabaseListener != null) {
                    mDatabaseListener.onCancelled(error);
                }
            }
        });
    }

    public void saveCurrentUserDetails() {
        if (mPreference.contains(KEY_PHONE) &&
                mPreference.contains(Constants.KEY_PROVIDER_ID) &&
                mPreference.contains(Constants.KEY_UID)) {

            ContactsModels models = new ContactsModels();
            models.setPhone(mPreference.getString(KEY_PHONE, DEFAULT));
            models.setProviderId(mPreference.getString(KEY_PROVIDER_ID, DEFAULT));
            models.setUid(mPreference.getString(KEY_UID, DEFAULT));
            DatabaseReference variableReference = mDatabase.getReference(URL_CONTACTS + mPreference.getString(KEY_PHONE, DEFAULT));
            variableReference.setValue(models);
        }
    }

    public void subscribeToContactsUpdates(OnDatabaseDataChanged listener) {
        mContantsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (listener != null) {
                    listener.onDataChanged(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
                if (listener != null) {
                    listener.onCancelled(error);
                }
            }
        });
    }

    public void createNewMeeting(MeetingModels meetingModels, String nameMeeting) {
        DatabaseReference databaseReference = mDatabase.getReference(URL_MEETING + nameMeeting);
        databaseReference.setValue(meetingModels);
    }

    public void saveProfile(Profile profile){
        mProfileReference.setValue(profile);
    }

    public Profile getProfile(){
        return profile;
    }

    public DatabaseReference getCurrentMeetingReference(String topicUrl) {
        return mMeetingReference.child(topicUrl);
    }

    public ArrayList<MeetingModels> getMeetingModels() {
        return mListMeetingModels;
    }

    public interface OnDatabaseDataChanged {
        void onDataChanged(DataSnapshot dataSnapshot);

        void onCancelled(DatabaseError error);
    }

}
