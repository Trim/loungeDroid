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

import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleListActivity;
import ch.adorsaz.loungeDroid.activities.SettingsActivity;
import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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

    protected static final String SESSION_COOKIE_SETTINGS = "session_cookie_settings";

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
        /*
         * If we want to login, also mSessionCookie is null or invalid.
         * 
         * So, we try to do the login request on the server to get new Cookie.
         * If the request doesn't succeed, there's two possibilities : - if
         * there's no cookie, authentication is false - if there's cookie, it
         * was too old and we should try again (with lucky, we can connect again
         * with old cookie and receive a newer)
         */
        try {
            String urlParameters = LOGIN_GET_RSSLOUNGE + "="
                    + URLEncoder.encode(mLogin, "UTF-8") + "&"
                    + PASSWORD_GET_RSSLOUNGE + "="
                    + URLEncoder.encode(mPassword, "UTF-8") + "&"
                    + JSON_GET_RSSLOUNGE;

            JSONObject jsonResponse = doRequest(LOGIN_PAGE_RSSLOUNGE,
                    urlParameters);
            if (jsonResponse != null) {
                if (jsonResponse.getBoolean("success") == true) {
                    Log.d(LOG_DEBUG_LOUNGE, "Logged to the server.");
                    setCookiePref(mSessionCookie);
                } else {
                    throw new AuthenticationFailLoungeException();
                }
            } else {
                throw new AuthenticationFailLoungeException();
            }
        } catch (AuthenticationFailLoungeException e) {
            if (mSessionCookie == null) {
                throw new AuthenticationFailLoungeException();
            } else {
                mSessionCookie = null;
                setCookiePref(null);
                loginLounge();
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
                    SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE);

            mServerUrl = pref.getString(SettingsActivity.URL_SERVER_PREF, "");

            if (!mServerUrl.startsWith("http://")
                    && !mServerUrl.startsWith("https://")) {
                mServerUrl = "http://" + mServerUrl;
            }

            mLogin = pref.getString(SettingsActivity.USER_SERVER_PREF, "");
            mPassword = pref.getString(SettingsActivity.PASSWORD_SERVER_PREF,
                    "");

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
            String httpParameters) {
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
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_DEBUG_LOUNGE, "Malformed url : " + mServerUrl);
            Intent intent = new Intent(mApplicationContext,
                    SettingsActivity.class);
            mApplicationContext.startActivity(intent);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (jsonResponse == null) {
            Log.e(LOG_DEBUG_LOUNGE,
                    "jsonResponse is null ! Cannot access page " + pageUrl);
        }
        return jsonResponse;
    }

    private void setCookiePref(String cookie) {
        Editor editor = mApplicationContext.getSharedPreferences(
                SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                .edit();
        editor.putString(SESSION_COOKIE_SETTINGS, mSessionCookie);
        // TODO : If make application for API >= 9, replace commit() by apply()
        editor.commit();
    }

    protected JSONObject serverRequest(String pageUrl, String httpParameters)
        throws AuthenticationFailLoungeException {
        // TODO : If make application for API >= 9 replace test on length() by
        // isEmpty()
        if (mSessionCookie == null || mSessionCookie.length() == 0) {
            loginLounge();
        }

        return doRequest(pageUrl, httpParameters);
    }
}
