package ch.adorsaz.loungeDroid.exception;

public class AuthenticationFailLoungeException extends Exception {

    @Override
    public String getMessage() {
        return "Unable to log in rsslounge server.\n"+super.getMessage();
    }
}
