package kinect.pro.meetingapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import kinect.pro.meetingapp.R;

public class TermsAndCondActivity extends AppCompatActivity {

    ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_cond);

        imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(view -> onBackPressed());

        WebView webView = findViewById(R.id.webViewTerms);
        webView.loadUrl("file:///android_asset/index.html");
    }
}
