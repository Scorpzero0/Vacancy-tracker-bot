package exception;

public class InvalidBotTokenException extends RuntimeException {
    public InvalidBotTokenException(String message) {
        super(message);
    }
}
