package tech.github.storecore.exception;

public class ServiceCommunicationException extends RuntimeException {
    public ServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
