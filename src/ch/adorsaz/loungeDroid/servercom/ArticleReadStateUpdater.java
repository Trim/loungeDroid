package ch.adorsaz.loungeDroid.servercom;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleDetailActivity;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
import ch.adorsaz.loungeDroid.exception.ReadStateUpdateException;
import android.os.AsyncTask;
import android.util.Log;

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
        } catch (ReadStateUpdateException e) {
            Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                    "Error while updating. Try again later");
            article=null;
        }

        return article[0];
    }

    @Override
    protected void onPostExecute(Article article) {
        if(article!=null){
            mActivity.updateReadButton();
        }
    }

    private Article updateArticle(Article article)
        throws ReadStateUpdateException,
        AuthenticationFailLoungeException {
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
        }

        return article;
    }
}
