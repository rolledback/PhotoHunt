package mrayer.photohunt;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.parse.ParseUser;

/**
 * Created by Matthew on 4/7/2016.
 */
public class PrefFragment  extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        PreferenceManager manager = this.getPreferenceManager();
        manager.setSharedPreferencesName(getString(R.string.current_album_pref) + "-" + ParseUser.getCurrentUser().getObjectId());
        manager.setSharedPreferencesMode(Context.MODE_PRIVATE);
        addPreferencesFromResource(R.xml.settings);
    }
}