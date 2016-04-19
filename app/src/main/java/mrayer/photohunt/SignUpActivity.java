package mrayer.photohunt;

/**
 * Created by cjkim on 3/22/16.
 * Code retrieved from this tutorial: http://sourcey.com/beautiful-android-login-and-signup-screens-with-material-design/
 */

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameText;
    private EditText passwordText;
    private Button signupButton;
    private TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameText = (EditText) findViewById(R.id.signup_username);
        passwordText = (EditText) findViewById(R.id.signup_password);
        signupButton = (Button) findViewById(R.id.signup_button);
        loginLink = (TextView) findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i("SignUpActivity", "Enter pressed");
                    signup();
                }
                return false;
            }
        });
    }

    public void signup() {
        Log.d(Constants.SignUpTag, "Signup");

        if (!validate()) {
            onSignupFailed(null);
            return;
        }

        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    // Hooray! Let them use the app now.
                    onSignupSuccess();
                }
                else {
                    Log.d(Constants.SignUpTag, e.toString());
                    onSignupFailed(e.getMessage());
                }
            }
        });
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(String s) {
        if(s != null) {
            Toast.makeText(getBaseContext(), "Signup failed: " + s + ".", Toast.LENGTH_LONG).show();
        }

        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            usernameText.setError("at least 3 characters");
            valid = false;
        }
        else {
            usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        }
        else {
            passwordText.setError(null);
        }

        return valid;
    }
}