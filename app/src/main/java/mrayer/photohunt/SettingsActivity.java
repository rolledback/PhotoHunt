package mrayer.photohunt;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by Matthew on 4/7/2016.
 * http://developer.android.com/intl/zh-tw/reference/android/preference/PreferenceActivity.html
 * https://developer.android.com/intl/zh-tw/reference/android/preference/PreferenceFragment.html
 * http://stackoverflow.com/questions/26564400/creating-a-preference-screen-with-support-v21-toolbar
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new PrefFragment()).commit();
//        getPreferenceManager().setSharedPreferencesName("photo_hunt_prefs");
//        addPreferencesFromResource(R.xml.preferences);
//        final SharedPreferences prefs = getSharedPreferences("photo_hunt_prefs", MODE_PRIVATE);
//
//        final CheckBoxPreference accountPrivacyLevelPref = (CheckBoxPreference) findPreference("account_private");
//        accountPrivacyLevelPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                return true;
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
