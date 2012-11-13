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
import ch.adorsaz.loungeDroid.exception.GetArticleListException;
import ch.adorsaz.loungeDroid.exception.ParseArticleException;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

/**
 * ArticleListGetter is an async task which will get all articles on server,
 * store it using own Article type and update ArticleListActivity data.
 * */
public class ArticleListGetter extends
        AsyncTask<ToDisplay, Object, List<Article>> {
    private SessionManager mSessionManager = null;
    private ArticleListActivity mActivity = null;

    /* Some urls needed to get specific articles */
    private final static String ARTICLELIST_PAGE_RSSLOUNGE = "/item/list";
    private final static String DISPLAY_ALL_PARAMS = "unread=0&starred=0";
    private final static String DISPLAY_UNREAD_PARAMS = "unread=1&starred=0";
    private final static String DISPLAY_STARRED_PARAMS = "unread=0&starred=1";

    public ArticleListGetter(ArticleListActivity activity) {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        mSessionManager = SessionManager.getInstance(mActivity);
        mActivity.setProgressBarVisibility(true);
    }

    @Override
    protected List<Article> doInBackground(ToDisplay... toDisplay) {
        List<Article> articles = null;

        try {
            articles = getArticles(toDisplay[0]);
        } catch (GetArticleListException e) {
            // TODO Pass to offline mode
            Log.e(SessionManager.LOG_SERVER,
                    "Cannot get article list, are you correctly logged ? Remove cookie and try again. Using saved data if available.");
            e.printStackTrace();

            Editor editor = mActivity.getSharedPreferences(
                    SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                    .edit();
            editor.remove(SessionManager.SESSION_COOKIE_SETTINGS);
            editor.commit();
            articles = null;
        } catch (ParseArticleException e) {
            // TODO Make Toast
            Log.e(SessionManager.LOG_SERVER,
                    "Cannot parse JSON response. Trying to display already parsed articles. Please contact developpers.");
            e.printStackTrace();
            articles = null;
        } catch (ConnectException e) {
            // TODO Pass to offline mode
            Log.e(SessionManager.LOG_SERVER,
                    "There was an error with network connection. Removing cookie to try again later. Using saved data if available.");
            setCookiePref(null);
            e.printStackTrace();

            articles = null;
        }
        return articles;
    }

    @Override
    protected void onPostExecute(List<Article> allArticles) {
        mActivity.setProgressBarVisibility(false);
        if (allArticles != null) {
            mActivity.updateArticleList(allArticles);
            Log.d(SessionManager.LOG_SERVER, "Finish to update Activity");
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to get rss feeds. Have you network connection ? Are your settings correct ?",
                    Toast.LENGTH_LONG).show();
        }
    }

    private List<Article> getArticles(ToDisplay toDisplay)
        throws GetArticleListException,
        ParseArticleException,
        ConnectException {
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
            case ALWAYS_PROMPT:
                break;
        }
        JSONObject jsonResponse = mSessionManager.serverRequest(
                ARTICLELIST_PAGE_RSSLOUNGE, httpParams);

        try {
            messages = jsonResponse.getJSONArray("messages");
        } catch (JSONException e) {
            throw new GetArticleListException();
        } catch (NullPointerException e) {
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

    private void setCookiePref(String cookie) {
        Editor editor = mActivity.getSharedPreferences(
                SettingsActivity.SHARED_PREFERENCES, Activity.MODE_PRIVATE)
                .edit();
        editor.putString(SessionManager.SESSION_COOKIE_SETTINGS, cookie);
        editor.commit();
    }
}
