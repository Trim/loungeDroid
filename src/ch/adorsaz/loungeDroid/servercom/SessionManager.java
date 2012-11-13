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

import ch.adorsaz.loungeDroid.activities.SettingsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

    protected static final String SESSION_COOKIE_NAME = "PHPSESSID";
    protected static final String SESSION_COOKIE_SETTINGS = "session_cookie_settings";

    protected final static String LOG_SERVER = "loungeDroid.server";
    protected final static String BAD_LOGIN_ERROR = "Authentication failed : bad login and/or password.";
    protected final static String MALFORMED_URL = "There's a typo in url. Please check url : ";
    protected final static String NO_RESPONSE_SERVER_ERROR = "No server response. Check your settings.";
    protected final static String UNEXPECTED_RESPONSE_ERROR = "There was a bug while connecting to server, please report it : ";

    public final static SessionManager getInstance(Context context) {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager();
            mApplicationContext = context;
            mSessionManager.getPreferences();
        }

        return mSessionManager;
    }

    private void loginLounge() throws ConnectException {
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
                    Log.d(LOG_SERVER, "Logged to the server.");
                    setCookiePref(mSessionCookie);
                } else {
                    errorDisplayAndSettings(BAD_LOGIN_ERROR);
                    setCookiePref(null);
                    throw new ConnectException(BAD_LOGIN_ERROR);
                }
            } else {
                throw new ConnectException(NO_RESPONSE_SERVER_ERROR);
            }
        } catch (UnsupportedEncodingException e) {
            throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                    + " unsupported encoding exception.");
        } catch (JSONException e) {
            throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                    + " server didn't response success on login.");
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
            String httpParameters) throws ConnectException {
        JSONObject jsonResponse = null;
        HttpURLConnection urlConnection = null;

        Log.d(LOG_SERVER, "Try to connect with cookie : " + mSessionCookie);
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
                    String cookie = urlConnection.getHeaderField("Set-Cookie");
                    if (cookie.startsWith(SESSION_COOKIE_NAME)) {
                        mSessionCookie = SESSION_COOKIE_NAME
                                + cookie.substring(cookie.indexOf('='),
                                        cookie.indexOf(';') + 1);
                        Log.d(LOG_SERVER, "Received cookie : " + mSessionCookie);
                    }
                }

                InputStream responseInput = urlConnection.getInputStream();
                jsonResponse = new JSONObject(streamToString(responseInput));
                responseInput.close();
            }

        } catch (MalformedURLException e) {
            errorDisplayAndSettings(MALFORMED_URL + pageUrl);
        } catch (IOException e) {
            throw new ConnectException(UNEXPECTED_RESPONSE_ERROR
                    + " input/output exception for page request : " + pageUrl);
        } catch (JSONException e) {
            throw new ConnectException(
                    UNEXPECTED_RESPONSE_ERROR
                            + " unable to read json response correctly for page request "
                            + pageUrl);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (jsonResponse == null) {
            Log.e(LOG_SERVER, "jsonResponse is null ! Cannot access page "
                    + pageUrl);
        }
        return jsonResponse;
    }

    private void setCookiePref(String cookie) {
        Editor editor = mApplicationContext.getSharedPreferences(
                SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                .edit();
        editor.putString(SESSION_COOKIE_SETTINGS, cookie);
        mSessionCookie = cookie;
        editor.commit();
    }

    private void errorDisplayAndSettings(String message) {
        Log.e(LOG_SERVER, message);
        Toast.makeText(mApplicationContext, message, Toast.LENGTH_LONG).show();

        SettingsActivity.setWantToEdit();

        Intent intent = new Intent(mApplicationContext, SettingsActivity.class);
        mApplicationContext.startActivity(intent);
    }

    protected JSONObject serverRequest(String pageUrl, String httpParameters)
        throws ConnectException {
        if (mSessionCookie == null || mSessionCookie.length() == 0) {
            loginLounge();
        }

        return doRequest(pageUrl, httpParameters);
    }
}
