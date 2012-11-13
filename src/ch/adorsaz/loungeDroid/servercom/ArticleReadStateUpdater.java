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
public class ArticleReadStateUpdater extends
        AsyncTask<Article, Object, Article> {
    private SessionManager mSessionManager = null;
    private ArticleDetailActivity mActivity = null;

    /* Some urls needed to get feeds */
    private final static String READSTATE_PAGE_RSSLOUNGE = "/item/mark";
    private final static String ID_GET_RSSLOUNGE = "id";

    public ArticleReadStateUpdater(ArticleDetailActivity activity) {
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
        } catch (ReadStateUpdateException e) {
            // TODO Manage exception
            Log.e(SessionManager.LOG_SERVER,
                    "Error while updating. Try again later");
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
            mActivity.updateReadButton();
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to update rss feed. Have you network connection ? Are your settings correct ?",
                    Toast.LENGTH_LONG).show();
        }
    }

    private Article updateArticle(Article article)
        throws ReadStateUpdateException,
        ConnectException {
        String httpParams = SessionManager.JSON_GET_RSSLOUNGE + "&"
                + ID_GET_RSSLOUNGE + "=" + article.getId();
        JSONObject jsonResponse = mSessionManager.serverRequest(
                READSTATE_PAGE_RSSLOUNGE, httpParams);

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
