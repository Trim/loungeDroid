package ch.adorsaz.loungeDroid.servercom;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import ch.adorsaz.loungeDroid.activities.ArticleListActivity;
import ch.adorsaz.loungeDroid.exception.ReadStateUpdateException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * ArticleMarkAllRead is an async task to mark all articles read in
 * ArticleListActivity.
 * */
public class ArticleMarkAllRead extends
        AsyncTask<List<Integer>, Object, Boolean> {
    private SessionManager mSessionManager = null;
    private ArticleListActivity mActivity = null;
    private List<Integer> mArticleIdList = null;

    /* Some urls needed to get feeds */
    private final static String MARKALL_READ_RSSLOUNGE = "/item/markall";
    private final static String ITEM_POST_ID = "items[]";

    public ArticleMarkAllRead(ArticleListActivity activity) {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        mSessionManager = SessionManager.getInstance(mActivity
                .getApplicationContext());
    }

    @Override
    protected Boolean doInBackground(List<Integer>... articleId) {
        boolean hasWorked = false;
        try {
            mArticleIdList = articleId[0];
            hasWorked = updateArticles();
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            Log.e(SessionManager.LOG_SERVER,
                    "There was an error with network connection.");
            e.printStackTrace();
            hasWorked = false;
        } catch (ReadStateUpdateException e) {
            // Try again login and request.
            SessionManager.deleteSessionCookie();

            try {
                hasWorked = updateArticles();
            } catch (ConnectException e1) {
                // TODO Auto-generated catch block
                Log.e(SessionManager.LOG_SERVER,
                        "There was an error with network connection.");
                e1.printStackTrace();
                hasWorked = false;
            } catch (ReadStateUpdateException e1) {
                Log.e(SessionManager.LOG_SERVER,
                        "Error while updating read state twice. Try again later");
                e1.printStackTrace();
                hasWorked = false;
            }
        }

        return hasWorked;
    }

    @Override
    protected void onPostExecute(Boolean hasWorked) {
        if (hasWorked) {
        } else {
            Toast.makeText(
                    mActivity,
                    "Unable to update rss feed. Have you network connection ? Are your settings correct ?",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean updateArticles()
        throws ReadStateUpdateException,
        ConnectException {
        Boolean hasWorked = false;
        String httpParams = SessionManager.JSON_GET_RSSLOUNGE;

        for (Integer articleId : mArticleIdList) {
            httpParams += "&" + Uri.encode(ITEM_POST_ID) + "=" + articleId;
        }

        JSONObject jsonResponse = mSessionManager.serverRequest(
                MARKALL_READ_RSSLOUNGE, httpParams);

        if (jsonResponse.has("next")) {
            hasWorked = true;
        } else {
            throw new ReadStateUpdateException();
        }

        return hasWorked;
    }
}
