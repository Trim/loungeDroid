package ch.adorsaz.loungeDroid.servercom;

import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleListActivity;
import ch.adorsaz.loungeDroid.activities.SettingsActivity;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.article.ToDisplay;
import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
import ch.adorsaz.loungeDroid.exception.GetArticleListException;
import ch.adorsaz.loungeDroid.exception.ParseArticleException;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class ArticleListGetter extends
        AsyncTask<ToDisplay, Object, List<Article>> {
    private SessionManager mSessionManager = null;
    private ArticleListActivity mActivity = null;

    /* Some urls needed to get feeds */
    private final static String ARTICLES_PAGE_RSSLOUNGE = "/item/list";
    private final static String DISPLAY_ALL_PARAMS = "unread=0&starred=0";
    private final static String DISPLAY_UNREAD_PARAMS = "unread=1&starred=0";
    private final static String DISPLAY_STARRED_PARAMS = "unread=0&starred=1";

    public ArticleListGetter(ArticleListActivity activity) {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        mSessionManager = SessionManager.getInstance(mActivity
                .getApplicationContext());
    }

    @Override
    protected List<Article> doInBackground(ToDisplay... toDisplay) {
        List<Article> articles = null;

        try {
            articles = getArticles(toDisplay[0]);
        } catch (AuthenticationFailLoungeException e) {
            // TODO Pass to offline mode
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Cannot log in. Check your connection. We'll check if we have saved data before.");
            articles = null;
        } catch (GetArticleListException e) {
            // TODO Pass to offline mode
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Cannot get article list, are you correctly logged ? Remove cookie and try again. Using saved data if available.");
            Editor editor = mActivity.getSharedPreferences(
                    SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                    .edit();
            editor.remove(SessionManager.SESSION_COOKIE_SETTINGS);
            editor.commit();
            articles = null;
        } catch (ParseArticleException e) {
            // TODO Make Toast
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Cannot parse JSON response. Trying to display already parsed articles. Please contact developpers.");
            articles = null;
        } catch (ConnectException e) {
            // TODO Pass to offline mode
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "There were an error with network connection. Remove cookie and try again. Using saved data if available.");
            Editor editor = mActivity.getSharedPreferences(
                    SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                    .edit();
            editor.remove(SessionManager.SESSION_COOKIE_SETTINGS);
            editor.commit();
            articles = null;
        }
        return articles;
    }

    @Override
    protected void onPostExecute(List<Article> allArticles) {
        if (allArticles != null) {
            mActivity.updateArticleList(allArticles);
            Log.d("loungeDroid", "Finish to update Activity");
        } else {
            Toast.makeText(
                    mActivity,
                    "There were errors on log in. Please check your settings and refresh.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private List<Article> getArticles(ToDisplay toDisplay)
        throws GetArticleListException,
        AuthenticationFailLoungeException,
        ParseArticleException, ConnectException {
        List<Article> articleList = new LinkedList<Article>();
        JSONArray messages = null;

        String httpParams = SessionManager.JSON_GET_RSSLOUNGE;
        switch (toDisplay) {
            case ALL:
                httpParams += "&" + DISPLAY_ALL_PARAMS;
                break;
            case UNREAD:
                httpParams += "&" + DISPLAY_UNREAD_PARAMS;
                break;
            case STARRED:
                httpParams += "&" + DISPLAY_STARRED_PARAMS;
                break;
        }
        JSONObject jsonResponse = mSessionManager.serverRequest(
                ARTICLES_PAGE_RSSLOUNGE, httpParams);

        try {
            messages = jsonResponse.getJSONArray("messages");
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
                String author = Html.fromHtml(thisMessage.getString("name"))
                        .toString();
                String link = thisMessage.getString("link");
                String icon = thisMessage.getString("icon");
                Boolean isRead = thisMessage.getInt("unread") == 0;
                Boolean isStarred = thisMessage.getInt("starred") == 1;

                Article article = new Article(id, day, month, subject, content,
                        author, link, icon, isRead, isStarred);
                articleList.add(article);
            }
        } catch (JSONException e) {
            throw new ParseArticleException();
        }
        return articleList;
    }
}
