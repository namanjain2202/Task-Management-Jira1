public class TaskManagementException extends RuntimeException {
    public TaskManagementException(String message) {
        super(message);
    }

    public TaskManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}