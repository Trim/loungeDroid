package ch.adorsaz.loungeDroid.article;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Article is the type which implements all data received from the server for
 * one article.<br/>
 * This implements Parcelable to be able to pass it in android Intents.
 * */
public class Article implements Parcelable {
    /**
     * article id.
     * */
    private Integer mId;
    /**
     * article publication day.
     * */
    private Integer mDay;
    /**
     * article publication month.
     * */
    private Integer mMonth;
    /**
     * article subject.
     * */
    private String mSubject;
    /**
     * article content as html file.
     * */
    private String mContent;
    /**
     * article author.
     * */
    private String mAuthor;
    /**
     * article original link.
     * */
    private String mLink;
    /**
     * icon of the article feed.
     * */
    private String mIcon;
    /**
     * article read state.
     * */
    private Boolean mIsRead = false;
    /**
     * article starred state.
     * */
    private Boolean mIsStarred = false;

    /**
     * Complete constructor of an article.
     * @param id article id
     * @param day article publication day
     * @param month article publication month
     * @param subject article subject
     * @param content article content (html format)
     * @param author article author
     * @param link link of original article
     * @param icon icon of news feed
     * @param isRead read state
     * @param isStarred starred state
     * */
    public Article(
            final Integer id,
            final Integer day,
            final Integer month,
            final String subject,
            final String content,
            final String author,
            final String link,
            final String icon,
            final Boolean isRead,
            final Boolean isStarred) {
        mId = id;
        mDay = day;
        mMonth = month;
        mSubject = subject;
        mContent =
                "<?xml version='1.0' encoding='utf-8' ?>"
                        + "<html><body>"
                        + content
                        + " </body></html>";
        mAuthor = author;
        mLink = link;
        mIcon = icon;
        mIsRead = isRead;
        mIsStarred = isStarred;
    }

    /**
     * @return read state article
     * */
    public final Boolean isRead() {
        return mIsRead;
    }

    /**
     * @return starred state article
     * */
    public final Boolean isStarred() {
        return mIsStarred;
    }

    @Override
    public final String toString() {
        String result = "Article[\n";
        result += "\tId : " + mId + ",\n";
        result += "\tMonth / Day : " + mMonth + " / " + mDay + ",\n";
        result += "\tSubject : " + mSubject + ",\n";
        result += "\tAuthor : " + mAuthor + ",\n";
        result += "\tLink : " + mLink + ",\n";
        result += "\tIcon : " + mIcon + ",\n";
        result += "\tIsRead : " + mIsRead + ",\n";
        result += "\tIsStarred : " + mIsStarred + ",\n";
        result += "\tContent : " + mContent;
        return result;
    }

    /**
     * @return article id
     * */
    public final Integer getId() {
        return mId;
    }

    /**
     * toggle read state.
     * */
    public final void updateReadState() {
        mIsRead = !mIsRead;
    }

    /**
     * toggle starred state.
     * */
    public final void updateStarredState() {
        mIsStarred = !mIsStarred;
    }

    /**
     * @return article subject
     * */
    public final String getSubject() {
        return mSubject;
    }

    /**
     * @return article author
     * */
    public final String getAuthor() {
        return mAuthor;
    }

    /**
     * @return article date (dd/mm)
     * */
    public final String getDate() {
        return mDay + "." + mMonth;
    }

    /**
     * @return article content
     * */
    public final String getContent() {
        return mContent;
    }

    /**
     * @return article original link
     * */
    public final String getLink() {
        return mLink;
    }

    /*
     * Next field, methods and creator are needed to use Parcelabel. That's
     * needed to pass easier and faster Article from one activity to another.
     */
    /**
     * Implementation of Parcelable for articles.
     * */
    public static final Parcelable.Creator<Article> CREATOR =
            new Parcelable.Creator<Article>() {
                public Article createFromParcel(final Parcel in) {
                    return new Article(in);
                }

                public Article[] newArray(final int size) {
                    return new Article[size];
                }
            };

    @Override
    public final int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mId);
        dest.writeInt(mDay);
        dest.writeInt(mMonth);
        dest.writeString(mSubject);
        dest.writeString(mContent);
        dest.writeString(mAuthor);
        dest.writeString(mLink);
        dest.writeString(mIcon);
        dest.writeValue(mIsRead);
        dest.writeValue(mIsStarred);
    }

    /**
     * Private constructor to retrieve article from parcel.
     * @param parcel parcel which contains the article
     * */
    private Article(final Parcel parcel) {
        mId = parcel.readInt();
        mDay = parcel.readInt();
        mMonth = parcel.readInt();
        mSubject = parcel.readString();
        mContent = parcel.readString();
        mAuthor = parcel.readString();
        mLink = parcel.readString();
        mIcon = parcel.readString();
        // TODO : check how read Boolean
        mIsRead = (Boolean) parcel.readValue(null);
        mIsStarred = (Boolean) parcel.readValue(null);
    }
    /* End of tools for Parcelable */
}
