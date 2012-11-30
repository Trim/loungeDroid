package ch.adorsaz.loungeDroid.servercom;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleDetailActivity;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.exception.StarredStateUpdateException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * ArticleStarredStateUpdate is an async task to update starred state of one
 * article and update ArticleDetailActivity.
 * */
public class ArticleStarredStateUpdater
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
     * It defines the page where will update article star state.
     * */
    private static final String STARREDSTATE_PAGE_RSSLOUNGE = "/item/star";
    /**
     * HttpGet parameter to give id article to update.
     * */
    private static final String ID_GET_RSSLOUNGE = "id";

    /**
     * Public Constructor.
     * @param activity Activity to update at the end of the task
     * */
    public ArticleStarredStateUpdater(final ArticleDetailActivity activity) {
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
        } catch (StarredStateUpdateException e) {
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
            } catch (StarredStateUpdateException e1) {
                Log.e(
                        SessionManager.LOG_SERVER,
                        "Error while updating starred state twice."
                                + " Try again later.");
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
            mActivity.updateStarredButton();
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to update starred state for an article."
                            + " Please check your network and settings.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to updates article starred state.
     * @return same article as in param, but with state updated.
     * @param article same article but with updated state if server made it.
     * @throws StarredStateUpdateException if response of the server doesn't
     *             contain "starred" JSON
     * @throws ConnectException if request on the server was bugged.
     * */
    private Article updateArticle(final Article article)
        throws StarredStateUpdateException,
        ConnectException {
        String httpParams =
                SessionManager.JSON_GET_RSSLOUNGE
                        + "&"
                        + ID_GET_RSSLOUNGE
                        + "="
                        + article.getId();
        JSONObject jsonResponse =
                mSessionManager.serverRequest(
                        STARREDSTATE_PAGE_RSSLOUNGE,
                        httpParams);

        try {
            // TODO : check if we want to do something with starred number. For
            // instance keep this getInt to be sure that there wasn't any error.
            jsonResponse.getInt("starred");
            article.updateStarredState();
        } catch (JSONException e) {
            throw new StarredStateUpdateException();
        }

        return article;
    }
}
