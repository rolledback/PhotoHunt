package mrayer.photohunt;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Matthew on 4/7/2016.
 */
public class PrefFragment  extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}