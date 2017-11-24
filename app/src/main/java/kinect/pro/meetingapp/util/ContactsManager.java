package kinect.pro.meetingapp.util;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import kinect.pro.meetingapp.model.Contact;

/**
 * Created by dobrikostadinov on 2/21/15.
 */
public class ContactsManager {

    public static ContactsManager instance;

    private List<Contact> mAllContacts;
    private List<Contact> mScheduledContacts;

    public static ContactsManager getInstance(Context context) {

        if (instance == null) {
            synchronized (ContactsManager.class) {
                if (instance == null) {
                    instance = new ContactsManager();

                    instance.queryAllContacts(context);
                }
            }
        }

        return instance;
    }

    private ContactsManager() {
        mScheduledContacts = new ArrayList<Contact>();
    }

    public void queryAllContacts(
            Context context) {

        mAllContacts = new ArrayList<Contact>();

        Cursor c = context.getContentResolver()
                .query(ContactsContract.Data.CONTENT_URI,
                        null,
                        (hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                                : ContactsContract.Contacts.DISPLAY_NAME)
                                + "<>''"
                                + " AND "
                                + ContactsContract.Contacts.IN_VISIBLE_GROUP
                                + "=1"
                                + " AND "
                                + ContactsContract.Data.MIMETYPE
                                + "=?",

                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}, ContactsContract.Data.CONTACT_ID);

        while (c.moveToNext()) {
            long id = -1;
            String name = "";
            String phone = "";
            String photo = "";

            try {
                id = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                name = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                phone = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                photo = c.getString(c.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
                String mimeType = c.getString(c.getColumnIndex(ContactsContract.Contacts.Data.MIMETYPE));

                Log.i("mimetType", mimeType);
            } catch (Exception e) {
            }

            Contact contact = new Contact();
            contact.setId(id);
            contact.setName(name);
            contact.setPhone(phone);
            contact.setImageThumb(photo);

            mAllContacts.add(contact);

            mAllContacts = new ArrayList<>(new HashSet<>(mAllContacts));

            Collections.sort(mAllContacts,
                    new Comparator<Contact>() {

                        @Override
                        public int compare(Contact obj1, Contact obj2) {
                            return obj1.getName().compareTo(obj2.getName());
                        }
                    });

        }
    }

    public List<Contact> getAllContacts() {
        return mAllContacts;
    }

    public List<Contact> getSortedContacts(){

        List<Contact> allContacts = new ArrayList<>(getAllContacts());
        List<Contact> scheduled = new ArrayList<>(mScheduledContacts);

        Collections.sort(scheduled,
                new Comparator<Contact>() {

                    @Override
                    public int compare(Contact obj1, Contact obj2) {
                        return obj1.getName().compareTo(obj2.getName());
                    }
                });


        allContacts.removeAll(scheduled);

        allContacts.addAll(0, scheduled);

        return allContacts;
    }

    public List<Contact> getAllScheduledContacts() {
        return mScheduledContacts;
    }

    public boolean isAlreadyScheduler(String phone) {

        for (int i = 0; i < mScheduledContacts.size(); i++) {
            if (mScheduledContacts.get(i).getPhone().equals(phone)) {
                return true;
            }
        }

        return false;
    }

    public void removeContactFromScheduler(Contact contactToRemove) {

        for (int i = 0; i < mScheduledContacts.size(); i++) {
            if (mScheduledContacts.get(i).getId() == contactToRemove.getId()) {
                getAllScheduledContacts().remove(i);

                break;
            }
        }
    }


    public Contact findPerson(String phone) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        for (int i = 0; i < getAllContacts().size(); i++) {

            PhoneNumberUtil.MatchType match = phoneUtil.isNumberMatch(phone, getAllContacts().get(i).getPhone());

            if (match != PhoneNumberUtil.MatchType.NO_MATCH && match != PhoneNumberUtil.MatchType.NOT_A_NUMBER && match != PhoneNumberUtil.MatchType.SHORT_NSN_MATCH) {
                return getAllContacts().get(i);
            }
        }

        if (phone == null) {
            phone = "";
        } else if (phone.length() == 0) {
            phone = " Private";
        }


        Contact noFoundContact = new Contact();
        noFoundContact.setName("Unknown: " + phone);

        return noFoundContact;
    }

    /**
     * Uses static final constants to detect if the device's platform version is
     * Honeycomb or later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }


}
