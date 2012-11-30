package ch.adorsaz.loungeDroid.exception;

/**
 * StartredSateUpdateException is sent when application is unable to update the
 * starred state of one article.
 * */
public class StarredStateUpdateException extends Exception {

    /**
     * Auto-generated ID by Eclipse.
     */
    private static final long serialVersionUID = -7137335428363481542L;

    @Override
    public final String getMessage() {
        return "Unable to update the starred state of an article.\n"
                + super.getMessage();
    }
}
