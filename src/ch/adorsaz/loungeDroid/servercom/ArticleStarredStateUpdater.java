package ch.adorsaz.loungeDroid.servercom;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleDetailActivity;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
import ch.adorsaz.loungeDroid.exception.StarredStateUpdateException;
import android.os.AsyncTask;
import android.util.Log;

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
        mSessionManager = SessionManager.getInstance(mActivity
                .getApplicationContext());
    }

    @Override
    protected Article doInBackground(Article... article) {
        try {
            article[0] = updateArticle(article[0]);
        } catch (AuthenticationFailLoungeException e) {
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Cannot log in. Check your connection and try again to update the read state of this article.");
            article=null;
        } catch (StarredStateUpdateException e) {
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Error while updating starred state. Try again later.");
            article=null;
        }

        return article[0];
    }

    @Override
    protected void onPostExecute(Article article) {
        if(article!=null){
            mActivity.updateStarredButton();
        }
    }

    private Article updateArticle(Article article)
        throws StarredStateUpdateException,
        AuthenticationFailLoungeException {
        String httpParams = SessionManager.JSON_GET_RSSLOUNGE + "&"
                + ID_GET_RSSLOUNGE + "=" + article.getId();
        JSONObject jsonResponse = mSessionManager.serverRequest(
                STARREDSTATE_PAGE_RSSLOUNGE, httpParams);

        try {
            // TODO : check if we want to do something with starred number
            Integer nbStarred = jsonResponse.getInt("starred");
            article.updateStarredState();
        } catch (JSONException e) {
            throw new StarredStateUpdateException();
        }

        return article;
    }
}
