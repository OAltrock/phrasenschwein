package exceptions;

public class SelfUserDeletionException extends RuntimeException {
    public SelfUserDeletionException(String message) {
        super(message);
    }
}
