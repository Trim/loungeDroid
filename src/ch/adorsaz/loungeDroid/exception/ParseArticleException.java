package ch.adorsaz.loungeDroid.exception;

/**
 * ParseArticleException is sent when it's impossible to parse correctly an
 * Article in a response.
 * */
public class ParseArticleException extends Exception {

    /**
     * Auto-generated ID by Eclpipse.
     */
    private static final long serialVersionUID = -1916018652604709645L;

    @Override
    public String getMessage() {
        return "Unable parse correctly messages.\n" + super.getMessage();
    }
}
