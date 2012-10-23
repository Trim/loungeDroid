package ch.adorsaz.loungeDroid.servercom;

public class LoginLoungeException extends Exception {

    @Override
    public String getMessage() {
        return "Unable to log in rsslounge server.\n"+super.getMessage();
    }
}
