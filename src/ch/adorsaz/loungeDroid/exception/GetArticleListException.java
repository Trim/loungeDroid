package ch.adorsaz.loungeDroid.exception;

public class GetArticleListException extends Exception {

    @Override
    public String getMessage() {
        return "Unable to get article list.\n"+super.getMessage();
    }
}
