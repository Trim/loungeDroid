package ch.adorsaz.loungeDroid.servercom;

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
    private SessionManager mSessionManager = SessionManager.getInstance(null);

    /* Some urls needed to get feeds */
    private final static String ARTICLES_PAGE_RSSLOUNGE = "/item/list";

    public ServerGetter() {

    }

    @Override
    protected void onPreExecute() {
        mSessionManager = SessionManager.getInstance(null);
    }

    @Override
    protected List<Article> doInBackground(ToDisplay... toDisplay) {
        getArticles();
        return null;
    }

    @Override
    protected void onPostExecute(List<Article> allArticles) {
    }

    private void getArticles() {
        JSONObject jsonResponse = mSessionManager.applyHttpRequest(
                ARTICLES_PAGE_RSSLOUNGE, SessionManager.JSON_GET_RSSLOUNGE);

        Log.d("articles from rsslounge", jsonResponse.toString());
    }
}
