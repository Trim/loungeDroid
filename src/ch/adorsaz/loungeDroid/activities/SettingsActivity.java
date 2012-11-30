package ch.adorsaz.loungeDroid.activities;

import ch.adorsaz.loungeDroid.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * SettingsActivity prompt user to get his preferences. It's also the first
 * activity to be launched to be able to verify that settings are good (need a
 * server url).
 * */
public class SettingsActivity extends PreferenceActivity {

    /* Preference constants */
    /**
     * Key to know where are preferences.
     * */
    public static final String SHARED_PREFERENCES = "shared_preferences";
    /**
     * Key for url server preference.
     * */
    public static final String URL_SERVER_PREF = "url_server_pref";
    /**
     * Key for displaying preference.
     * */
    public static final String DISPLAY_BEHAVIOUR_PREF = "todisplay_pref";
    /**
     * Key for url user name preference.
     * */
    public static final String USER_SERVER_PREF = "username_server_pref";
    /**
     * Key for url user password preference.
     * */
    public static final String PASSWORD_SERVER_PREF = "password_server_pref";

    /**
     * Key to store if user want edit preferences.
     * */
    public static final String WANT_TO_EDIT_PREF = "want_to_edit_pref";
    /**
     * Boolean to know if user want to edit preference (if so, show this
     * activity, if not, pass to next activity).
     * */
    private static boolean mWantToEdit = true;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES);
        getPreferenceManager().setSharedPreferencesMode(Activity.MODE_PRIVATE);

        SharedPreferences pref =
                getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);

        Log.d(
                "Preferences",
                "URL Server : " + pref.getString(URL_SERVER_PREF, ""));

        if (!pref.getString(URL_SERVER_PREF, "").equals("") && !mWantToEdit) {
            Intent intent =
                    new Intent(SettingsActivity.this,
                               ArticleListActivity.class);
            startActivity(intent);
            finish();
        } else {
            Editor editor =
                    getSharedPreferences(
                            SettingsActivity.SHARED_PREFERENCES,
                            Activity.MODE_PRIVATE).edit();
            editor.remove(WANT_TO_EDIT_PREF);
            editor.commit();
            mWantToEdit = false;

            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public final void onBackPressed() {
        SharedPreferences pref =
                getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);

        if (!pref.getString(URL_SERVER_PREF, "").equals("") && !mWantToEdit) {
            Intent intent =
                    new Intent(SettingsActivity.this,
                               ArticleListActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "You should configure as least the server url to be able"
                            + " to use this application",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method permit to alert this class that we want to edit preferences.
     * */
    public static void setWantToEdit() {
        mWantToEdit = true;
    }
}
