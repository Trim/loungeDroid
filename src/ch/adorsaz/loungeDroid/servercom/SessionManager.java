package ch.adorsaz.loungeDroid.servercom;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SessionManager {
    private static String mLogin = null;
    private static String mPassword = null;
    private static String mServerUrl = null;
    private static String mSessionCookie = null;
    private static SessionManager mSessionManager = null;
    private static Context mApplicationContext = null;

    private final static String LOGIN_PAGE_RSSLOUNGE = "/index/login";
    private final static String LOGIN_GET_RSSLOUNGE = "username";
    private final static String PASSWORD_GET_RSSLOUNGE = "password";
    protected final static String JSON_GET_RSSLOUNGE = "json=true";

    // Preferences :
    private static final String SHARED_PREFERENCES = "shared_preferences";
    private static final String URL_SERVER_PREF = "url_server_pref";
    private static final String USER_SERVER_PREF = "username_server_pref";
    private static final String PASSWORD_SERVER_PREF = "password_server_pref";

    private static final String SESSION_COOKIE_SETTINGS = "session_cookie_settings";

    protected final static String LOG_DEBUG_LOUNGE = "loungeDroid.server :";

    public final static SessionManager getInstance(Context context) {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager();
            mApplicationContext = context;
            mSessionManager.getPreferences();
        }

        return mSessionManager;
    }

    private void loginLounge() throws AuthenticationFailLoungeException {
        try {
            String urlParameters = LOGIN_GET_RSSLOUNGE + "="
                    + URLEncoder.encode(mLogin, "UTF-8") + "&"
                    + PASSWORD_GET_RSSLOUNGE + "=" + mPassword + "&"
                    + JSON_GET_RSSLOUNGE;

            JSONObject jsonResponse = doRequest(LOGIN_PAGE_RSSLOUNGE,
                    urlParameters);

            if (jsonResponse.getBoolean("success") == true) {
                Log.d(LOG_DEBUG_LOUNGE, "Logged to the server.");
                setCookiePref(mSessionCookie);
            } else {
                throw new AuthenticationFailLoungeException();
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private SessionManager() {
    }

    private void getPreferences() {
        if (mApplicationContext != null) {
            SharedPreferences pref = mApplicationContext.getSharedPreferences(
                    SHARED_PREFERENCES, Activity.MODE_PRIVATE);

            mServerUrl = pref.getString(URL_SERVER_PREF, "");

            if (!mServerUrl.startsWith("http://")
                    && !mServerUrl.startsWith("https://")) {
                mServerUrl = "http://" + mServerUrl;
            }

            mServerUrl = Uri.encode(mServerUrl);
            mLogin = Uri.encode(pref.getString(USER_SERVER_PREF, ""));
            mPassword = Uri.encode(pref.getString(PASSWORD_SERVER_PREF, ""));

            if (pref.contains(SESSION_COOKIE_SETTINGS)) {
                mSessionCookie = pref.getString(SESSION_COOKIE_SETTINGS, "");
            }
        }
    }

    private String streamToString(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                inputStream));

        String result = br.readLine();
        String line = result;

        while (line != null) {
            result = result + "\n" + line;
            line = br.readLine();
        }

        return result;
    }

    private synchronized JSONObject doRequest(String pageUrl,
            String httpParameters) throws AuthenticationFailLoungeException {
        JSONObject jsonResponse = null;
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) new URL(mServerUrl + pageUrl)
                    .openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            if (mSessionCookie != null && !(mSessionCookie.length() == 0)) {
                urlConnection.setRequestProperty("Cookie", mSessionCookie);
            }

            DataOutputStream out = new DataOutputStream(
                    urlConnection.getOutputStream());
            out.writeBytes(httpParameters);
            out.flush();
            out.close();

            if (urlConnection.getResponseCode() == 200) {
                if (urlConnection.getHeaderField("Set-Cookie") != null) {
                    mSessionCookie = urlConnection.getHeaderField("Set-Cookie");
                }

                InputStream responseInput = urlConnection.getInputStream();
                jsonResponse = new JSONObject(streamToString(responseInput));
                responseInput.close();
            } else {
                throw new ConnectException();
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        if (jsonResponse == null) {
            Log.e(LOG_DEBUG_LOUNGE,
                    "jsonResponse is null ! Cannot access page " + pageUrl);
        }
        return jsonResponse;
    }

    private void setCookiePref(String cookie) {
        Editor editor = mApplicationContext.getSharedPreferences(
                SHARED_PREFERENCES, Activity.MODE_PRIVATE).edit();
        editor.putString(SESSION_COOKIE_SETTINGS, mSessionCookie);
        // TODO : If make application for API >= 9, replace commit() by apply()
        editor.commit();
    }

    protected JSONObject serverRequest(String pageUrl, String httpParameters)
        throws AuthenticationFailLoungeException {
        // TODO : If make application for API >= 9 replace test on length() by isEmpty()
        if (mSessionCookie == null || mSessionCookie.length() == 0) {
            loginLounge();
        }

        return doRequest(pageUrl, httpParameters);
    }
}
