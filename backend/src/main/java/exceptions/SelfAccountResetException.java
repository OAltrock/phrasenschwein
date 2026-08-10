package exceptions;

public class SelfAccountResetException extends RuntimeException {
    public SelfAccountResetException(String message) {
        super(message);
    }
}
