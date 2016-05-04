package mrayer.photohunt;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class HelpWebPageActivity extends AppCompatActivity {

    private WebView helpWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_web_page);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.help_web_page_toolbar);
        setSupportActionBar(myToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Help");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        helpWebView = (WebView) findViewById(R.id.help_web_view);
        helpWebView.getSettings().setJavaScriptEnabled(true);
        helpWebView.loadUrl("http://mrayermann.com/PhotoHunt/help.html");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return true;
    }
}
