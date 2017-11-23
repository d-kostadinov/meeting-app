package kinect.pro.meetingapp.model;


import java.util.List;

public class MeetingModels {

    private String author;
    private String topic;
    private long date;
    private long duration;
    private float latitude;
    private float longitude;
    private String location;
    private String reminder;
    private List<Participant> participants = null;

    public MeetingModels(String author, String topic, long date, long duration,
                         String location, float latitude, float longitude,
                         String reminder, List<Participant> participants) {
        this.author = author;
        this.topic = topic;
        this.date = date;
        this.duration = duration;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reminder = reminder;
        this.participants = participants;
    }

    public MeetingModels() {

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
