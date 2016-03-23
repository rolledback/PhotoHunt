package mrayer.photohunt;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by cjkim on 3/23/16.
 */
public class ParseInitializeClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Keys.Parse_APP_ID, Keys.Parse_API_Key);
    }
}