package ch.adorsaz.loungeDroid.servercom;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SessionManager {
    private String mLogin = null;
    private String mPassword = null;
    private String mServerUrl = null;
    private String mSessionCookie = null;
    private static SessionManager mSessionManager = null;

    private final static String LOGIN_PAGE_RSSLOUNGE = "/index/login";
    private final static String LOGIN_GET_RSSLOUNGE = "username";
    private final static String PASSWORD_GET_RSSLOUNGE = "password";
    protected final static String JSON_GET_RSSLOUNGE = "json=true";

    private final static String LOG_DEBUG_LOUNGE = "loungeDroid.server :";

    public final static SessionManager getInstance(Context context) {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager();
        }

        mSessionManager.getPreferences(context);

        return mSessionManager;
    }

    public Boolean loginLounge() throws LoginLoungeException {
        Boolean result = false;
        HttpURLConnection urlConnection = null;
        try {
            String urlParameters = LOGIN_GET_RSSLOUNGE
                    + URLEncoder.encode(mLogin, "UTF-8") + "&"
                    + PASSWORD_GET_RSSLOUNGE
                    + URLEncoder.encode(mPassword, "UTF-8") + "&"
                    + JSON_GET_RSSLOUNGE;

            urlConnection = (HttpURLConnection) new URL(mServerUrl
                    + LOGIN_PAGE_RSSLOUNGE).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            DataOutputStream out = new DataOutputStream(
                    urlConnection.getOutputStream());
            out.writeBytes(urlParameters);
            out.flush();
            out.close();

            if (urlConnection.getResponseCode() == 200) {
                JSONObject jsonResponse = new JSONObject(
                        streamToString(urlConnection.getInputStream()));
                result = jsonResponse.getBoolean("success") == true;
            }else{
                throw new LoginLoungeException();
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
        return result;
    }

    private SessionManager() {
    }

    private void getPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        mServerUrl = prefs.getString("urlPref", "");
        if (!mServerUrl.startsWith("http://")
                && !mServerUrl.startsWith("https://")) {
            mServerUrl = "http://" + mServerUrl;
        }

        mLogin = Uri.encode(prefs.getString("loginPref", ""));
        mPassword = Uri.encode(prefs.getString("passwordPref", ""));
    }

    static String streamToString(InputStream inputStream) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line = null;

        try {
            line = inputReader.readLine();
            while (line != null) {
                line += "\n" + inputReader.readLine();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return line;
    }
}
