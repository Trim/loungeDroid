package ch.adorsaz.loungeDroid.servercom;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleDetailActivity;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.exception.ReadStateUpdateException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * ArticleStarredStateUpdate is an async task to update read state of one
 * article and update ArticleDetailActivity.
 * */
public class ArticleReadStateUpdater
        extends
        AsyncTask<Article, Object, Article> {
    /**
     * Instance of the SessionManager used to communicate with server.
     * */
    private SessionManager mSessionManager = null;
    /**
     * The activity where will update the article.
     * */
    private ArticleDetailActivity mActivity = null;

    /* Some urls needed to get feeds */
    /**
     * It defines the page where will update article read state.
     * */
    private static final String READSTATE_PAGE_RSSLOUNGE = "/item/mark";
    /**
     * HttpGet parameter to give id article to update.
     * */
    private static final String ID_GET_RSSLOUNGE = "id";

    /**
     * Public Constructor.
     * @param activity Activity to update at the end of the task
     * */
    public ArticleReadStateUpdater(final ArticleDetailActivity activity) {
        mActivity = activity;
    }

    @Override
    protected final void onPreExecute() {
        mActivity.setProgressBarVisibility(true);
        mSessionManager =
                SessionManager.getInstance(mActivity.getApplicationContext());
    }

    @Override
    protected final Article doInBackground(final Article... article) {
        try {
            article[0] = updateArticle(article[0]);
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            Log.e(
                    SessionManager.LOG_SERVER,
                    "There was an error with network connection.");
            e.printStackTrace();

            article[0] = null;
        } catch (ReadStateUpdateException e) {
            // Try again login and request.
            SessionManager.deleteSessionCookie();

            try {
                article[0] = updateArticle(article[0]);
            } catch (ConnectException e1) {
                // TODO Auto-generated catch block
                Log.e(
                        SessionManager.LOG_SERVER,
                        "There was an error with network connection.");
                e1.printStackTrace();
                article[0] = null;
            } catch (ReadStateUpdateException e1) {
                Log.e(
                        SessionManager.LOG_SERVER,
                        "Error while updating read state twice."
                                + " Try again later");
                e1.printStackTrace();
                article[0] = null;
            }
        }

        return article[0];
    }

    @Override
    protected final void onPostExecute(final Article article) {
        mActivity.setProgressBarVisibility(false);
        if (article != null) {
            mActivity.updateReadButton();
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to update rss feed."
                            + " Have you network connection ?"
                            + " Are your settings correct ?",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to updates article read state.
     * @return same article as in param, but with state updated.
     * @param article same article but with updated state if server made it.
     * @throws ReadStateUpdateException if response of the server contains
     *             "error" JSON
     * @throws ConnectException if request on the server was bugged.
     * */
    private Article updateArticle(final Article article)
        throws ReadStateUpdateException,
        ConnectException {
        String httpParams =
                SessionManager.JSON_GET_RSSLOUNGE
                        + "&"
                        + ID_GET_RSSLOUNGE
                        + "="
                        + article.getId();
        JSONObject jsonResponse =
                mSessionManager.serverRequest(
                        READSTATE_PAGE_RSSLOUNGE,
                        httpParams);

        try {
            // Throw exception if there was an error
            jsonResponse.getJSONObject("error");
            throw new ReadStateUpdateException();
        } catch (JSONException e) {
            article.updateReadState();
            e.printStackTrace();
        }

        return article;
    }
}
