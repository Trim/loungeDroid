package ch.adorsaz.loungeDroid.gui;

import ch.adorsaz.loungeDroid.R;
import ch.adorsaz.loungeDroid.article.Article;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;

public class ArticleDetailActivity extends Activity implements OnClickListener {

    private Article mArticle = null;
    private TextView mTitle = null;
    private TextView mAuthor = null;
    private TextView mDate = null;
    private WebView mContent = null;
    private Button mReadButton = null;
    private Button mStarButton = null;
    private Button mBrowserButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articlelist_detail);
        mArticle = this.getIntent().getParcelableExtra(
                ArticleListActivity.ARTICLE_KEY);

        Log.d("ArticleDetail", "Article received : " + mArticle.getId());

        mTitle = (TextView) findViewById(R.id.newsTitle);
        mTitle.setText(mArticle.getSubject());

        mAuthor = (TextView) findViewById(R.id.newsAuthor);
        mAuthor.setText(mArticle.getAuthor());

        mDate = (TextView) findViewById(R.id.newsDate);
        mDate.setText(mArticle.getDate());

        mContent = (WebView) findViewById(R.id.newsContent);
        mContent.loadData(Uri.encode(mArticle.getContent()), "text/html; charset=UTF-8", "UTF-8");

        mReadButton = (Button) findViewById(R.id.newsRead);
        mReadButton.setOnClickListener(this);
        if(mArticle.isRead()){
            mReadButton.setText(R.string.notRead);
        }else{
            mReadButton.setText(R.string.markAsRead);
        }

        mStarButton = (Button) findViewById(R.id.newsStar);
        mStarButton.setOnClickListener(this);
        if(mArticle.isStarred()){
            mStarButton.setText(R.string.unStar);
        }else{
            mStarButton.setText(R.string.starIt);
        }

        mBrowserButton = (Button) findViewById(R.id.newsBrowser);
        mBrowserButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }
}
