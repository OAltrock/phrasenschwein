package exceptions;

public class AdminCannotLikeException extends RuntimeException {
    public AdminCannotLikeException(String message) {
        super(message);
    }
}
