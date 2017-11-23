package kinect.pro.meetingapp.rest;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import static kinect.pro.meetingapp.other.Constants.ACCOUNT_SID;
import static kinect.pro.meetingapp.other.Constants.AUTH_TOKEN;
import static kinect.pro.meetingapp.other.Constants.BASE_URL;
import static kinect.pro.meetingapp.other.Constants.NUMBER_FROM;



public class RestController {

    private static final String TAG = RestController.class.getSimpleName();

    private TwilioApi mTwillioApi;

    @Inject
    public RestController(TwilioApi twilioApi) {
        mTwillioApi = twilioApi;
    }


    public void inviteToMeeting(Context context, String phone, String event) {
        String body = "You are invited to meeting - " + event +
                "\n Please install Meeting App to accept invitation";

        String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(), Base64.NO_WRAP
        );

        Map<String, String> data = new HashMap<>();
        data.put("From", NUMBER_FROM);
        data.put("To", phone);
        data.put("Body", body);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        TwilioApi api = retrofit.create(TwilioApi.class);

        api.sendMessage(ACCOUNT_SID, base64EncodedCredentials, data).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse->success");
                    Toast.makeText(context, "Sent SMS!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, response.message());
                    Toast.makeText(context, "Error sent SMS!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure");
            }
        });
    }


}
