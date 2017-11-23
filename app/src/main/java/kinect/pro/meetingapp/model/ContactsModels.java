package kinect.pro.meetingapp.model;


public class ContactsModels {

    private String phone;
    private String providerId;
    private String uid;

    public ContactsModels() {

    }

    public ContactsModels(String phone, String providerId, String uid) {
        this.phone = phone;
        this.providerId = providerId;
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
