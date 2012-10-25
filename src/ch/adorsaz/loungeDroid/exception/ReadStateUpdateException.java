package ch.adorsaz.loungeDroid.exception;

public class ReadStateUpdateException extends Exception {

    @Override
    public String getMessage() {
        return "Unable to update the read state of an article.\n"+super.getMessage();
    }
}
