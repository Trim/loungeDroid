package ch.adorsaz.loungeDroid.exception;

/**
 * ReadStateUpdateException is sent when application is unable to update the
 * read state of one article.
 * */
public class ReadStateUpdateException extends Exception {

    /**
     * Auto-generated ID by Eclpipse.
     */
    private static final long serialVersionUID = 859540452885060466L;

    @Override
    public final String getMessage() {
        return "Unable to update the read state of an article.\n"
                + super.getMessage();
    }
}
