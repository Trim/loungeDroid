package ch.adorsaz.loungeDroid.servercom;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.article.ToDisplay;
import android.os.AsyncTask;
import android.util.Log;

/*
 * This interface implements minimum required to connect to a server.
 */
public class ServerGetter extends AsyncTask<ToDisplay, Object, List<Article>> {
    
    /*Some urls needed to get feeds */

    @Override
    protected void onPreExecute(){
    }

    @Override
    protected List<Article> doInBackground(ToDisplay... toDisplay) {
        login();
        getArticles();
        return null;
    }

    @Override
    protected void onPostExecute(List<Article> allArticles) {

    }

    private void getArticles() {

    }

    private JSONObject httpPost(String url, String post) {
        HttpURLConnection urlConnection = null;
        JSONObject json = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            OutputStreamWriter out = new OutputStreamWriter(
                    urlConnection.getOutputStream());
            out.write(post);
            out.close();

            json=new JSONObject(SessionManager.streamToString(urlConnection.getInputStream()));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            Log.e("loungeDroid", "Malformed URL, you should check your settings.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("loungeDroid", "IOException, you should check your connection");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return json;
    }
}
