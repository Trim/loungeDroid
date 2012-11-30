package ch.adorsaz.loungeDroid.servercom;

import java.net.ConnectException;
import java.util.List;

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
public class ArticleMarkAllRead
        extends
        AsyncTask<List<Integer>, Object, Boolean> {
    /**
     * Instance of the SessionManager used to communicate with server.
     * */
    private SessionManager mSessionManager = null;
    /**
     * The activity where will put articles.
     * */
    private ArticleListActivity mActivity = null;
    /**
     * ids of articles to mark as read.
     * */
    private List<Integer> mArticleIdList = null;

    /* Some urls needed to get feeds */
    /**
     * It defines the page where will update read state for all articles.
     * */
    private static final String MARKALL_READ_RSSLOUNGE = "/item/markall";
    /**
     * HttpPost parameter to give list of items.
     * */
    private static final String ITEM_POST_ID = "items[]";

    /**
     * Public Constructor.
     * @param activity Activity to update at the end of the task
     * */
    public ArticleMarkAllRead(final ArticleListActivity activity) {
        mActivity = activity;
    }

    @Override
    protected final void onPreExecute() {
        mSessionManager =
                SessionManager.getInstance(mActivity.getApplicationContext());
    }

    @Override
    protected final Boolean doInBackground(final List<Integer>... articleId) {
        boolean hasWorked = false;
        try {
            mArticleIdList = articleId[0];
            hasWorked = updateArticles();
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            Log.e(
                    SessionManager.LOG_SERVER,
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
                Log.e(
                        SessionManager.LOG_SERVER,
                        "There was an error with network connection.");
                e1.printStackTrace();
                hasWorked = false;
            } catch (ReadStateUpdateException e1) {
                Log.e(
                        SessionManager.LOG_SERVER,
                        "Error while updating read state twice."
                                + " Try again later");
                e1.printStackTrace();
                hasWorked = false;
            }
        }

        return hasWorked;
    }

    @Override
    protected final void onPostExecute(final Boolean hasWorked) {
        if (hasWorked) {
            Toast.makeText(
                    mActivity,
                    "All articles mark as read.\n"
                            + "You can refresh to check if there's"
                            + " some more articles to mark as read.",
                    Toast.LENGTH_LONG).show();
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
     * Code to update read state of mArtileIdList.
     * @return if update as worked or not
     * @throws ReadStateUpdateException if response of the server doesn't
     *             contain "next" JSON
     * @throws ConnectException if request on the server was bugged.
     * */
    private boolean updateArticles()
        throws ReadStateUpdateException,
        ConnectException {
        Boolean hasWorked = false;
        String httpParams = SessionManager.JSON_GET_RSSLOUNGE;

        for (Integer articleId : mArticleIdList) {
            httpParams += "&" + Uri.encode(ITEM_POST_ID) + "=" + articleId;
        }

        JSONObject jsonResponse =
                mSessionManager.serverRequest(
                        MARKALL_READ_RSSLOUNGE,
                        httpParams);

        if (jsonResponse.has("next")) {
            hasWorked = true;
        } else {
            throw new ReadStateUpdateException();
        }

        return hasWorked;
    }
}
