package ch.adorsaz.loungeDroid.article;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Article is the type which implements all data received from the server for
 * one article.
 * 
 * This implements Parcelable to be able to pass it in android Intents.
 * */
public class Article implements Parcelable {
    private Integer mId;
    private Integer mDay;
    private Integer mMonth;
    private String mSubject;
    private String mContent;
    private String mAuthor;
    private String mLink;
    private String mIcon;
    private Boolean mIsRead = false;
    private Boolean mIsStarred = false;

    public Article(Integer id, Integer day, Integer month, String subject,
            String content, String author, String link, String icon,
            Boolean isRead, Boolean isStarred) {
        mId = id;
        mDay = day;
        mMonth = month;
        mSubject = subject;
        mContent = "<?xml version='1.0' encoding='utf-8' ?>" + "<html><body>"
                + content + " </body></html>";
        mAuthor = author;
        mLink = link;
        mIcon = icon;
        mIsRead = isRead;
        mIsStarred = isStarred;
    }

    public Boolean isRead() {
        return mIsRead;
    }

    public void setRead(Boolean isRead) {
        this.mIsRead = isRead;
    }

    public Boolean isStarred() {
        return mIsStarred;
    }

    public void setStarred(Boolean isStarred) {
        this.mIsStarred = isStarred;
    }

    public String toString() {
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

    public Integer getId() {
        return mId;
    }

    public void updateReadState() {
        mIsRead = !mIsRead;
    }

    public void updateStarredState() {
        mIsStarred = !mIsStarred;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDate() {
        return mDay + "." + mMonth;
    }

    public String getContent() {
        return mContent;
    }

    public String getLink() {
        return mLink;
    }

    /*
     * Next field, methods and creator are needed to use Parcelabel. That's
     * needed to pass easier and faster Article from one activity to another.
     */
    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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

    private Article(Parcel parcel) {
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
