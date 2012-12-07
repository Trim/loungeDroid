package ch.adorsaz.loungeDroid.activities;

import ch.adorsaz.loungeDroid.R;
import ch.adorsaz.loungeDroid.article.Article;
import ch.adorsaz.loungeDroid.servercom.ArticleReadStateUpdater;
import ch.adorsaz.loungeDroid.servercom.ArticleStarredStateUpdater;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

/**
 * ArticleDetailActivity display all needed informations from one Article in one
 * Activity. It can update read state, starred state and can call WebBrowser to
 * show article in his context.
 * */
public class ArticleDetailActivity extends Activity implements OnClickListener {

    /**
     * Currently shown article.
     * */
    private Article mArticle = null;
    /**
     * Textview where to display article title.
     */
    private TextView mTitle = null;
    /**
     * Textview where to display article author.
     */
    private TextView mAuthor = null;
    /**
     * Textview where to display article date.
     */
    private TextView mDate = null;
    /**
     * Webview where to display content.
     */
    private WebView mContent = null;
    /**
     * Button to switch read state.
     */
    private Button mReadButton = null;
    /**
     * Button to switch starred state.
     */
    private Button mStarButton = null;
    /**
     * Button to display article in the browser.
     */
    private Button mBrowserButton = null;

    /**
     * Boolean to know if pause is done from back button pressure or not.
     * */
    private boolean isBackButtonPressed = false;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articlelist_detail);

        mArticle =
                this.getIntent().getParcelableExtra(
                        ArticleListActivity.ARTICLE_KEY);

        Log.d("ArticleDetail", "Article received : " + mArticle.getId());

        mTitle = (TextView) findViewById(R.id.newsTitle);
        mTitle.setText(mArticle.getSubject());

        mAuthor = (TextView) findViewById(R.id.newsAuthor);
        mAuthor.setText(mArticle.getAuthor());

        mDate = (TextView) findViewById(R.id.newsDate);
        mDate.setText(mArticle.getDate());

        mContent = (WebView) findViewById(R.id.newsContent);
        mContent.loadData(
                Uri.encode(mArticle.getContent()),
                "text/html; charset=UTF-8",
                "UTF-8");

        mReadButton = (Button) findViewById(R.id.newsRead);
        mReadButton.setOnClickListener(this);
        if (mArticle.isRead()) {
            mReadButton.setText(R.string.notRead);
        } else {
            mReadButton.setText(R.string.markAsRead);
        }

        mStarButton = (Button) findViewById(R.id.newsStar);
        mStarButton.setOnClickListener(this);
        if (mArticle.isStarred()) {
            mStarButton.setText(R.string.unStar);
        } else {
            mStarButton.setText(R.string.starIt);
        }

        mBrowserButton = (Button) findViewById(R.id.newsBrowser);
        mBrowserButton.setOnClickListener(this);
    }

    @Override
    public final void onClick(final View v) {
        switch (v.getId()) {
            case R.id.newsRead:
                ArticleReadStateUpdater readUpdate =
                        new ArticleReadStateUpdater(this);
                readUpdate.execute(mArticle);
                break;
            case R.id.newsStar:
                ArticleStarredStateUpdater starUpdate =
                        new ArticleStarredStateUpdater(this);
                starUpdate.execute(mArticle);
                break;
            case R.id.newsBrowser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(mArticle.getLink()));
                startActivity(browserIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public final void onBackPressed() {
        // Here you get Back Key Press So make boolean false
        isBackButtonPressed = true;
        super.onBackPressed();
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (isBackButtonPressed) {
            goBackToArticleList();
        }
    }

    @Override
    protected final void onResume() {
        super.onResume();
        isBackButtonPressed = false;
    }

    /**
     * updateReadButton() should be called from the asynk task which updates the
     * read state of the displayed article.<br/>
     * It should be called only when the async task has finished his job.
     * */
    public final void updateReadButton() {
        if (mArticle.isRead()) {
            mReadButton.setText(R.string.notRead);
            mReadButton.refreshDrawableState();
            Toast.makeText(
                    getApplicationContext(),
                    "Marked as read.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mReadButton.setText(R.string.markAsRead);
            mReadButton.refreshDrawableState();
            Toast.makeText(
                    getApplicationContext(),
                    "Marked as unread.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * updateStarredButton() should be called from the asynk task which updates
     * the starred state of the displayed article.<br/>
     * It should be called only when the async task has finished his job.
     * */
    public final void updateStarredButton() {
        if (mArticle.isStarred()) {
            mStarButton.setText(R.string.unStar);
            mReadButton.refreshDrawableState();
            Toast.makeText(
                    getApplicationContext(),
                    "Starred.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mStarButton.setText(R.string.starIt);
            mReadButton.refreshDrawableState();
            Toast.makeText(
                    getApplicationContext(),
                    "Unstarred.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method save in the intent last states of the displayed articles and
     * show article list activity.
     * */
    private void goBackToArticleList() {
        Intent intent =
                new Intent(
                        ArticleDetailActivity.this,
                        ArticleListActivity.class);

        intent.putExtras(getIntent());
        intent.putExtra(ArticleListActivity.ARTICLE_KEY, mArticle);

        startActivity(intent);
        finish();
    }
}
