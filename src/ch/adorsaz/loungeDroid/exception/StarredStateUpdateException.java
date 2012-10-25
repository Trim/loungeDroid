package ch.adorsaz.loungeDroid.exception;

public class StarredStateUpdateException extends Exception {

    @Override
    public String getMessage() {
        return "Unable to update the starred state of an article.\n"+super.getMessage();
    }
}
