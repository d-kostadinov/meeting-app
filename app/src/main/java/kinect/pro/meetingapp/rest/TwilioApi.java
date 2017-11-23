package kinect.pro.meetingapp.rest;

import java.util.Map;

import kinect.pro.meetingapp.other.Constants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TwilioApi {

    @FormUrlEncoded
    @POST(Constants.TWILLIO_ENDPOINT)
    Call<ResponseBody> sendMessage(
            @Path(Constants.ACCOUNT_SID_KEY) String accountSId,
            @Header(Constants.AUTHORIZATION_KEY) String signature,
            @FieldMap Map<String, String> metadata
    );
}