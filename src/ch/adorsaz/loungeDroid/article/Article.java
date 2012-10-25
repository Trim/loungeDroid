package ch.adorsaz.loungeDroid.article;

public class Article {
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
        mContent = content;
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
}
