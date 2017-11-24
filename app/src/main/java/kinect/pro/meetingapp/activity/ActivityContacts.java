package kinect.pro.meetingapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import butterknife.BindView;
import butterknife.OnClick;
import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.adapter.AdapterContacts;
import kinect.pro.meetingapp.util.ContactsManager;
import kinect.pro.meetingapp.util.ImageOptionsBuilder;

public class ActivityContacts extends BaseActivity {

    public static final String TAG = ActivityContacts.class.getSimpleName();

    public static ActivityContacts newInstance() {

        ActivityContacts fragmentContacts = new ActivityContacts();

        return fragmentContacts;
    }


    @BindView(R.id.act_contacts_recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.act_contacts_search)
    TextInputEditText mSearchEditText;

    @BindView(R.id.bntDone)
    View btnDone;

    private AdapterContacts mAdapter;

    @Override
    protected boolean isBackNavigationActivity() {
        return true;
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_contacts;
    }

    @Override
    protected String getScreenTitle() {
        return getString(R.string.compose_search_contacts);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageLoaderConfiguration imageLoaderConfiguration = ImageOptionsBuilder
                .createImageLoaderConfiguration(this);
        ImageLoader.getInstance().init(imageLoaderConfiguration);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mSearchEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);


        mSearchEditText.addTextChangedListener(new FilterSearchTextWatcher());

        mAdapter = new AdapterContacts(ContactsManager.getInstance(this).getSortedContacts(), this);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        hideSoftKeyboard();
    }

    @OnClick(R.id.bntDone)
    void btnDoneClickListener(View view) {

        setResult(Activity.RESULT_OK);

        finish();
    }

    private class FilterSearchTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAdapter.getFilter().filter(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void hideSoftKeyboard() {
        Context c = getBaseContext();
        View v = mSearchEditText.findFocus();
        if (v == null)
            return;
        InputMethodManager inputManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
