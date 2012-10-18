package ch.adorsaz.loungeDroid.servercom;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;

import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.article.ToDisplay;
import android.os.AsyncTask;
import android.util.Log;

/*
 * This interface implements minimum required to connect to a server.
 */
public class ServerGetter extends AsyncTask<ToDisplay, Object, List<Article>> {
    private String mLogin;
    private String mPassword;
    private String mServerUrl;

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

    private void login() {

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

            Class types[]={JSONObject.class};
            json = (JSONObject) urlConnection.getContent(types);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            Log.e("loungeDroid", "Malformed URL, you should check your settings.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("loungeDroid", "IOException, you should check your connection");
        } finally {
            urlConnection.disconnect();
        }
        return json;
    }
}
