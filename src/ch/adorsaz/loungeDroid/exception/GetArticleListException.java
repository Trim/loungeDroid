package ch.adorsaz.loungeDroid.exception;

/**
 * GetArticleListException is sent when you can't correctly get and read
 * articles from server.
 * */
public class GetArticleListException extends Exception {

    /**
     * Auto-generated ID by Eclipse.
     */
    private static final long serialVersionUID = 8449004512651295034L;

    @Override
    public final String getMessage() {
        return "Unable to get article list.\n" + super.getMessage();
    }
}
