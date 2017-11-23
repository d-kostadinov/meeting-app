package kinect.pro.meetingapp.model;


public class Profile {

    private String profileName;
    private String profileType;
    private int profileBusinessType;

    public Profile() {
    }

    public Profile(String profileName, String profileType, int profileBusinessType) {
        this.profileName = profileName;
        this.profileType = profileType;
        this.profileBusinessType = profileBusinessType;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public int getProfileBusinessType() {
        return profileBusinessType;
    }

    public void setProfileBusinessType(int profileBusinessType) {
        this.profileBusinessType = profileBusinessType;
    }
}
