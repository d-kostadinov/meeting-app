package kinect.pro.meetingapp.auth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Module;

import static kinect.pro.meetingapp.other.Constants.KEY_PHONE;
import static kinect.pro.meetingapp.other.Constants.KEY_PROVIDER_ID;
import static kinect.pro.meetingapp.other.Constants.KEY_UID;


@Module
public class AuthManager implements FirebaseAuth.AuthStateListener {

    private static final String TAG = AuthManager.class.getSimpleName();

    private FirebaseAuth mAuth;
    private OnVerificationListener mListener;
    private AuthStateListener mAuthStateListener;
    private SharedPreferences mPreference;

    @Inject
    public AuthManager(SharedPreferences sharedPreferences) {
        mPreference = sharedPreferences;
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
    }


    public void setVerificationListener(OnVerificationListener listener) {
        mListener = listener;
    }

    public void setAuthStateListener(AuthStateListener listener) {
        mAuthStateListener = listener;
    }

    public void requestCode(Activity context, String authCodeNumber) throws FirebaseTooManyRequestsException {

        if (mListener == null) {
            Log.e(TAG, "Verification listener null");
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                authCodeNumber, 60, TimeUnit.SECONDS, context,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        mListener.onVerificationCompleted(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        mListener.onVerificationFailed(e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mListener.onCodeSent(verificationId, forceResendingToken);
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String verificationId) {
                        super.onCodeAutoRetrievalTimeOut(verificationId);
                        Log.d(TAG, verificationId);
                        mListener.onCodeAutoRetrievalTimeOut(verificationId);
                    }
                }
        );
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (mAuthStateListener != null) {
            mAuthStateListener.onAuthStateChanged(firebaseAuth);
        }
    }

    public void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        try {
            mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        saveMyContacts(task.getResult().getUser().getPhoneNumber(),
                                task.getResult().getUser().getProviderId(),
                                task.getResult().getUser().getUid());

                    }
                    if (mAuthStateListener != null) {
                        mAuthStateListener.onSignIn(task.isSuccessful());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMyContacts(String phone, String providerId, String uid) {
        mPreference.edit().putString(KEY_PHONE, phone).apply();
        mPreference.edit().putString(KEY_PROVIDER_ID, providerId).apply();
        mPreference.edit().putString(KEY_UID, uid).apply();

    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }


    public interface AuthStateListener {

        void onAuthStateChanged(FirebaseAuth firebaseAuth);

        void onSignIn(boolean isSuccessful);

    }

    public interface OnVerificationListener {

        void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential);

        void onVerificationFailed(FirebaseException e);

        void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken);

        void onCodeAutoRetrievalTimeOut(String verificationId);
    }

}
