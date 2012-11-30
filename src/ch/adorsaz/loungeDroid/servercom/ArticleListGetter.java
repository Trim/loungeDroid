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
import android.app.ProgressDialog;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

/**
 * ArticleListGetter is an async task which will get all articles on server,
 * store it using own Article type and update ArticleListActivity data.
 * */
public class ArticleListGetter
        extends
        AsyncTask<ToDisplay, Object, List<Article>> {
    /**
     * Instance of the SessionManager used to communicate with server.
     * */
    private SessionManager mSessionManager = null;
    /**
     * The activity where will put articles.
     * */
    private ArticleListActivity mActivity = null;
    /**
     * A simple progress dialog.
     * */
    private ProgressDialog mProgressDialog = null;

    /* Some urls needed to get specific articles */
    /**
     * It defines the page where will get the list of articles.
     * */
    private static final String ARTICLELIST_PAGE_RSSLOUNGE = "/item/list";
    /**
     * HttpGet parameters to get all articles.
     * */
    private static final String DISPLAY_ALL_PARAMS = "unread=0&starred=0";
    /**
     * HttpGet parameters to get unread articles.
     * */
    private static final String DISPLAY_UNREAD_PARAMS = "unread=1&starred=0";
    /**
     * HttpGet parameters to get starred articles.
     * */
    private static final String DISPLAY_STARRED_PARAMS = "unread=0&starred=1";

    /**
     * Public Constructor.
     * @param activity Activity to update at the end of the task
     * */
    public ArticleListGetter(final ArticleListActivity activity) {
        mActivity = activity;
    }

    @Override
    protected final void onPreExecute() {
        mSessionManager = SessionManager.getInstance(mActivity);

        mProgressDialog =
                new ProgressDialog(mActivity, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Fetching news, please wait...");
        mProgressDialog.show();
    }

    @Override
    protected final List<Article> doInBackground(final ToDisplay... toDisplay) {
        List<Article> articles = null;

        try {
            articles = getArticles(toDisplay[0]);
        } catch (ParseArticleException e) {
            // TODO Make Toast
            Log.e(
                    SessionManager.LOG_SERVER,
                    "Cannot parse JSON response when getting articles."
                            + "Please contact developpers.");
            e.printStackTrace();
            articles = null;
        } catch (ConnectException e) {
            // TODO Pass to offline mode
            Log.e(
                    SessionManager.LOG_SERVER,
                    "There was an error with network connection.");
            setCookiePref(null);
            e.printStackTrace();

            articles = null;
        } catch (GetArticleListException e) {
            // Try again login and request.
            SessionManager.deleteSessionCookie();

            try {
                articles = getArticles(toDisplay[0]);
            } catch (ConnectException e1) {
                // TODO Pass to offline mode
                Log.e(
                        SessionManager.LOG_SERVER,
                        "There was an error with network connection.");
                setCookiePref(null);
                e1.printStackTrace();
                articles = null;
            } catch (ParseArticleException e1) {
                // TODO Make Toast
                Log.e(
                        SessionManager.LOG_SERVER,
                        "Cannot parse JSON response when getting articles."
                                + "Please contact developpers.");
                e1.printStackTrace();
                articles = null;
            } catch (GetArticleListException e1) {
                // TODO Didn't resolved the error. Stop it.
                Log.e(
                        SessionManager.LOG_SERVER,
                        "Cannot get article list, always bad"
                                + " response from server (twice tested).");
                e1.printStackTrace();
                articles = null;
            }
        }
        return articles;
    }

    @Override
    protected final void onPostExecute(final List<Article> allArticles) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        if (allArticles != null) {
            mActivity.updateArticleList(allArticles);
            Log.d(SessionManager.LOG_SERVER, "Finish to update Activity");
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to get rss feeds."
                            + "Please check your network and settings.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Month index in datetime string with format is YYYY/MM/DD.
     * */
    private static final int MONTH_INDEX_TIME = 5;
    /**
     * Day index in datetime string with format is YYYY/MM/DD.
     * */
    private static final int DAY_INDEX_TIME = 8;
    /**
     * Multiplier to change unit for decimal base.
     * */
    private static final int UNIT_SIZE = 10;

    /**
     * It gets articles from server, read JSON response and transform it in an
     * Article List.
     * @param toDisplay which articles should be got from server.
     * @return a list of articles
     * @throws GetArticleListException thrown if a JSON error or a null response
     *             was received.
     * @throws ParseArticleException thrown if there was a JSON error while
     *             reading articles.
     * @throws ConnectException thrown if the server is unreachable or an
     *             unexpected situation while connecting (as not possible to be
     *             identified)
     * */
    private List<Article> getArticles(final ToDisplay toDisplay)
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
            default:
                break;
        }
        JSONObject jsonResponse =
                mSessionManager.serverRequest(
                        ARTICLELIST_PAGE_RSSLOUNGE,
                        httpParams);

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
                // Datetime format is YYYY/MM/DD
                String datetime = thisMessage.getString("datetime");
                int day =
                        Character.getNumericValue(datetime
                                .charAt(DAY_INDEX_TIME))
                                * UNIT_SIZE
                                + Character.getNumericValue(datetime
                                        .charAt(DAY_INDEX_TIME + 1));
                int month =
                        Character.getNumericValue(datetime
                                .charAt(MONTH_INDEX_TIME))
                                * UNIT_SIZE
                                + Character.getNumericValue(datetime
                                        .charAt(MONTH_INDEX_TIME + 1));
                String subject = thisMessage.getString("title");
                String content = thisMessage.getString("content");
                String author =
                        Html.fromHtml(thisMessage.getString("name")).toString();
                String link = thisMessage.getString("link");
                String icon = thisMessage.getString("icon");
                Boolean isRead = thisMessage.getInt("unread") == 0;
                Boolean isStarred = thisMessage.getInt("starred") == 1;

                Article article =
                        new Article(
                                id,
                                day,
                                month,
                                subject,
                                content,
                                author,
                                link,
                                icon,
                                isRead,
                                isStarred);
                articleList.add(article);
            }
        } catch (JSONException e) {
            throw new ParseArticleException();
        }
        return articleList;
    }

    /**
     * update cookie in the shared preferences.
     * @param cookie the new cookie to save
     * */
    private void setCookiePref(final String cookie) {
        Editor editor =
                mActivity.getSharedPreferences(
                        SettingsActivity.SHARED_PREFERENCES,
                        Activity.MODE_PRIVATE).edit();
        editor.putString(SessionManager.SESSION_COOKIE_SETTINGS, cookie);
        editor.commit();
    }
}
