package kinect.pro.meetingapp.model;


public class Participant {

    private String member;
    private String status;

    public Participant(String member, String status) {
        this.member = member;
        this.status = status;
    }

    public Participant() {
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
