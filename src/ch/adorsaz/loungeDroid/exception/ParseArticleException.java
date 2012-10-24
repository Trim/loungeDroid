package ch.adorsaz.loungeDroid.exception;

public class ParseArticleException extends Exception {

    @Override
    public String getMessage() {
        return "Unable parse correctly messages.\n"+super.getMessage();
    }
}
