package kinect.pro.meetingapp.model;

/**
 * Created by dobrikostadinov on 2/19/15.
 */
public class Contact {

    private long id;
    private String mName;
    private String mPhone;
    private String mImageThumb;

    private boolean isPressed;

    public Contact() {
        mImageThumb = "";
    }

    public Contact(int id) {
        mImageThumb = "";
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setImageThumb(String imageThumb) {
        mImageThumb = imageThumb;
    }

    public String getImageThumb() {
        return mImageThumb;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Contact) {
            if (((Contact) o).mPhone.equals(this.mPhone)) {
                return true;
            }
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return 31 * mPhone.hashCode();
    }
}

