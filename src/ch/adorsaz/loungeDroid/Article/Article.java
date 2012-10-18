package ch.adorsaz.loungeDroid.Article;

public class Article {
    private Integer mId;
    private Integer mDay;
    private Integer mMonth;
    private String mSubject;
    private String mContent;
    private String mAuthor;
    private String mLink;
    private Boolean mIsRead = false;
    private Boolean mIsStarred = false;

    public Article(Integer id, Integer day, Integer month, String subject,
            String content, String author, String link, Boolean isRead,
            Boolean isStarred) {
        mId = id;
        mDay = day;
        mMonth = month;
        mSubject = subject;
        mContent = content;
        mAuthor = author;
        mLink = link;
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
}
