package gogather.framework.exception;

public class GoGatherFrameworkException extends RuntimeException {
    public GoGatherFrameworkException(String message) {
        super(message);
    }

    public GoGatherFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
