package kinect.pro.meetingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kinect.pro.meetingapp.App;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.auth.AuthManager;


public class LoginActivity extends AppCompatActivity implements AuthManager.OnVerificationListener, AuthManager.AuthStateListener {

    private static final String TAG = "LoginActivity ==>> ";

    @Inject
    AuthManager authManager;

    @BindView(R.id.etNumber)
    EditText etCodeNumber;
    @BindView(R.id.btnOk)
    Button btnClick;


    private String mVerificationId;

    @Override
    protected void onStart() {
        super.onStart();
        authManager.setVerificationListener(this);
        authManager.setAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        authManager.setVerificationListener(null);
        authManager.setAuthStateListener(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ((App) getApplication()).getAppComponent().inject(this);
    }

    @OnClick(R.id.btnOk)
    public void onClick(View view) {
        if (btnClick.getText().toString().equals(getResources().getString(R.string.registration))) {
           alertDialog();
        } else {
            try {
                String code = etCodeNumber.getText().toString();
                if (TextUtils.isEmpty(code))
                    return;
                authManager.signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId, code));
            } catch (Exception e) {
                e.printStackTrace();
            }
            changeTextButton();
        }
    }

    public void alertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        alertDialogBuilder
                .setTitle("Please confirm your phone number:")
                .setMessage(etCodeNumber.getText().toString())
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    try {
                        handleRequestCode();
                        changeTextButton();
                    } catch (FirebaseTooManyRequestsException e) {
                        e.getMessage();
                    }
                })
                .setNegativeButton("edit", (dialog, id) -> {
                    dialog.cancel();
                }).show();
    }


    @OnClick(R.id.terms_and_cond)
    public void onClickTermsAndCondition(View view){
        startActivity(new Intent(LoginActivity.this, TermsAndCondActivity.class));
    }

    public void changeTextButton() {
        if (btnClick.getText().toString().equals(getResources().getString(R.string.registration))) {
            btnClick.setText(getResources().getText(R.string.submit));
            etCodeNumber.setText("");
            etCodeNumber.setHint(R.string.validation_Code);
        } else {
            btnClick.setText(getResources().getText(R.string.registration));
            etCodeNumber.setText("");
            etCodeNumber.setHint(R.string.enter_phone_number);
        }
    }

    private void handleRequestCode() throws FirebaseTooManyRequestsException {
        String phoneNumber = etCodeNumber.getText().toString();
        if (phoneNumber.isEmpty()) {
            etCodeNumber.setError("Number not entered");
            return;
        }

        if (!phoneNumber.startsWith("+")) {
            etCodeNumber.setError("Number must begin with +");
            return;
        }
        authManager.requestCode(this, phoneNumber);
    }

    @Override
    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
        authManager.signInWithCredential(phoneAuthCredential);
    }

    @Override
    public void onVerificationFailed(FirebaseException e) {
        etCodeNumber.setError("Verification failed");
    }

    @Override
    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        mVerificationId = verificationId;
    }

    @Override
    public void onCodeAutoRetrievalTimeOut(String verificationId) {
        Toast.makeText(this,
                "verification failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, CalendarActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onSignIn(boolean isSuccessful) {
        if (isSuccessful) {
            etCodeNumber.setText("******");
//            startActivity(new Intent(LoginActivity.this, CalendarActivity.class)
            startActivity(new Intent(LoginActivity.this, ProfileDetailsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "failed to sign in with credential",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

