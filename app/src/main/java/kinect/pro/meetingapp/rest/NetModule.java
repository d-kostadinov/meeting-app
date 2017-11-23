package kinect.pro.meetingapp.rest;

import dagger.Module;
import dagger.Provides;
import kinect.pro.meetingapp.rest.RestController;
import kinect.pro.meetingapp.rest.TwilioApi;
import retrofit2.Retrofit;

import static kinect.pro.meetingapp.other.Constants.BASE_URL;



@Module
public class NetModule {

    @Provides
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Provides
    TwilioApi provideTwillioApi(Retrofit retrofit) {
        return retrofit.create(TwilioApi.class);
    }

    @Provides
    RestController provideRestController(TwilioApi twilioApi) {
        return new RestController(twilioApi);
    }

}
