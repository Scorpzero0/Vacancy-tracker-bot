package exception;

public class InvalidCommandFormat extends RuntimeException {
    public InvalidCommandFormat(String message) {
        super(message);
    }
}
