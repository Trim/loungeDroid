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

    private List<Article> getArticles()
        throws GetArticleListException,
        AuthenticationFailLoungeException,
        ParseArticleException {
        List<Article> articleList = new LinkedList<Article>();
        JSONArray messages = null;

        JSONObject jsonResponse = mSessionManager.applyHttpRequest(
                ARTICLES_PAGE_RSSLOUNGE, SessionManager.JSON_GET_RSSLOUNGE);

        // TODO : check if message object array exists if all feeds are read and
        // show only unread.
        try {
            messages = jsonResponse.getJSONArray("message");
        } catch (JSONException e) {
            throw new GetArticleListException();
        }

        try {
            for (int i = 0; i < messages.length(); i++) {
                JSONObject thisMessage = messages.getJSONObject(i);
                int id = thisMessage.getInt("id");
                String datetime = thisMessage.getString("datetime");
                int day = Character.getNumericValue(datetime.charAt(8)) * 10
                        + Character.getNumericValue(datetime.charAt(9));
                int month = Character.getNumericValue(datetime.charAt(5)) * 10
                        + Character.getNumericValue(datetime.charAt(6));
                String subject = thisMessage.getString("title");
                String content = thisMessage.getString("content");
                String author = thisMessage.getString("name");
                String link = thisMessage.getString("link");
                Boolean isRead = thisMessage.getInt("unread") == 1;
                Boolean isStarred = thisMessage.getInt("starred") == 1;

                Article article = new Article(id, day, month, subject, content,
                        author, link, isRead, isStarred);
                articleList.add(article);
            }
        } catch (JSONException e) {
            throw new ParseArticleException();
        }

        if (articleList.get(0) != null) {
            Log.d(SessionManager.LOG_DEBUG_LOUNGE, "First article : "
                    + articleList.get(0).toString());
        }
        return articleList;
    }
}
