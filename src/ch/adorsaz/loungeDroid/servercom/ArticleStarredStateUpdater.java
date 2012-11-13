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

public class ArticleStarredStateUpdater extends
        AsyncTask<Article, Object, Article> {
    private SessionManager mSessionManager = null;
    private ArticleDetailActivity mActivity = null;

    /* Some urls needed to get feeds */
    private final static String STARREDSTATE_PAGE_RSSLOUNGE = "/item/star";
    private final static String ID_GET_RSSLOUNGE = "id";

    public ArticleStarredStateUpdater(ArticleDetailActivity activity) {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        mActivity.setProgressBarVisibility(true);
        mSessionManager = SessionManager.getInstance(mActivity
                .getApplicationContext());
    }

    @Override
    protected Article doInBackground(Article... article) {
        try {
            article[0] = updateArticle(article[0]);
        } catch (StarredStateUpdateException e) {
            Log.e(SessionManager.LOG_SERVER,
                    "Error while updating starred state. Try again later.");
            e.printStackTrace();

            article[0] = null;
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            Log.e(SessionManager.LOG_SERVER,
                    "There was an error with network connection. Removing cookie to try again later. Using saved data if available.");
            e.printStackTrace();

            article[0] = null;
        }

        return article[0];
    }

    @Override
    protected void onPostExecute(Article article) {
        mActivity.setProgressBarVisibility(false);
        if (article != null) {
            mActivity.updateStarredButton();
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to update rss feed. Have you network connection ? Are your settings correct ?",
                    Toast.LENGTH_LONG).show();
        }
    }

    private Article updateArticle(Article article)
        throws StarredStateUpdateException,
        ConnectException {
        String httpParams = SessionManager.JSON_GET_RSSLOUNGE + "&"
                + ID_GET_RSSLOUNGE + "=" + article.getId();
        JSONObject jsonResponse = mSessionManager.serverRequest(
                STARREDSTATE_PAGE_RSSLOUNGE, httpParams);

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
